(ns graphql-test-source.schema
  (:require
   [clojure.test :as t]
   [clojure.java.io :as io]
   [com.walmartlabs.lacinia.util :as util]
   [com.walmartlabs.lacinia.schema :as schema]
   [clojure.edn :as edn])
  (:import (java.time Instant ZoneId LocalDateTime ZonedDateTime)
           (java.time.temporal ChronoUnit)
           (java.time.format DateTimeParseException)))

(defn timestamp->instant
  "Parses timestamp to java.time.Instant instance.
  Handles:

  - epoch seconds
  - epoch millis
  - zoned ISO8601
  - unzoned ISO8601, assumes local time (default, override with :zone)

  Will throw error for unexpected data.
  "
  [ts & {:keys [zone] :or {zone (ZoneId/systemDefault)}}]
  (Instant/from
   (cond
     (number? ts)
     (if (< ts 10000000000)
       (Instant/ofEpochSecond ts)
       (Instant/ofEpochMilli ts))
     (string? ts)
     (try
       (ZonedDateTime/parse ts)
       (catch DateTimeParseException e
         (.atZone (LocalDateTime/parse ts)
                  (cond
                    (instance? ZoneId zone) zone
                    (string? zone) (ZoneId/of zone)
                    :else (ZoneId/systemDefault))))))))

(defn step [interval {:keys [timestamp value]}]
  {:timestamp (.plusMillis timestamp interval)
   :value (+ value (rand) -0.5)})

(defn resolve-simple-series
  [context args value]
  (let [{:keys [from to interval_ms]} args
        from (timestamp->instant from)
        to (timestamp->instant to)
        span (.until from to ChronoUnit/MILLIS)]
    (map #(assoc % :timestamp (.toString (:timestamp %)))
         (take (/ span interval_ms)
               (iterate (partial step interval_ms)
                        {:timestamp from :value (* (rand) 100.0)})))))

(defn resolve-complex-series
  [context args value]
  (let [{:keys [from to interval_ms]} args
        from (timestamp->instant from)
        to (timestamp->instant to)
        span (.until from to ChronoUnit/MILLIS)]
    (map (fn [x]
           (assoc {}
                  :group "foo"
                  :value (:value x)
                  :value_list (repeatedly 5 #(assoc {} :value (rand)))
                  :time {:timestamp (.toString (:timestamp x))}))
           (take (/ span interval_ms)
                 (iterate (partial step interval_ms)
                          {:timestamp from :value (* (rand) 100.0)})))))

(defn resolver-map
  []
  {:query/simple-series resolve-simple-series
   :query/complex-series resolve-complex-series})

(defn load-schema
  []
  (-> (io/resource "schema.edn")
      slurp
      edn/read-string
      (util/attach-resolvers (resolver-map))
      schema/compile))
