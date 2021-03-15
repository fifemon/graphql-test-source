(ns graphql-test-source.schema
  (:require
   [clojure.test :as t]
   [clojure.java.io :as io]
   [com.walmartlabs.lacinia.util :as util]
   [com.walmartlabs.lacinia.schema :as schema]
   [clojure.edn :as edn])
  (:import (java.time Instant ZoneId LocalDateTime ZonedDateTime)
           (java.time.temporal ChronoUnit)
           (java.time.format DateTimeParseException DateTimeFormatter)))

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

(defn rand-instant
  [after before]
  (Instant/ofEpochSecond
   (+ (.getEpochSecond after)
      (rand-int (- (.getEpochSecond before)
                   (.getEpochSecond after))))))

(defn step [interval {:keys [timestamp value]}]
  {:timestamp (.plusMillis timestamp interval)
   :value (+ value (rand) -0.5)})

(defn resolve-simple-series
  [context args value]
  (let [{:keys [from to interval_ms format]} args
        from (timestamp->instant from)
        to (timestamp->instant to)
        formatter (if format
                    (let [f (.withZone (DateTimeFormatter/ofPattern format)
                                       (ZoneId/systemDefault))]
                      (fn [t] (.format f t)))
                    (fn [t] (.toString t)))
        span (.until from to ChronoUnit/MILLIS)]
    (map #(assoc % :timestamp (formatter (:timestamp %)))
         (take (/ span interval_ms)
               (iterate (partial step interval_ms)
                        {:timestamp from :value (* (rand) 100.0)})))))

(def groups [{:id "a" :name "foo"}
             {:id "b" :name "bar"}
             {:id "c" :name "baz"}])

(defn resolve-complex-series
  [context args value]
  (let [{:keys [from to interval_ms]} args
        from (timestamp->instant from)
        to (timestamp->instant to)
        span (.until from to ChronoUnit/MILLIS)]
    (map (fn [x]
           (assoc {}
                  :group (rand-nth groups)
                  :value (:value x)
                  :value_list (repeatedly 5 #(assoc {} :value (rand)))
                  :time {:timestamp (.toString (:timestamp x))}))
           (take (/ span interval_ms)
                 (iterate (partial step interval_ms)
                          {:timestamp from :value (* (rand) 100.0)})))))

(defn make-event
  []
  (let [nouns ["datacenter" "computer" "toaster" "turtle" "container"]
        verbs ["exploded" "restarted" "scrammed" "was updated" "rolled"]
        quotes ["The only good bug is a dead bug!"
                "What we've got here is a failure to communicate."
                "When a body catches a body, comin' through the rye."
                "Specialization is for the insects."
                "Why oh why didn't I take the red pill?"
                "Test event, please ignore."]
        tags [nil "oops" "critical" "act-of-god"]]
    {:name (str "The " (rand-nth nouns) " " (rand-nth verbs))
     :description (rand-nth quotes)
     :tags [(rand-nth tags)]}))

(defn resolve-events
  [context args value]
  (let [{:keys [from to format count end]} args
        from (timestamp->instant from)
        to (timestamp->instant to)
        formatter (if format
                    (let [f (.withZone (DateTimeFormatter/ofPattern format)
                                       (ZoneId/systemDefault))]
                      (fn [t] (.format f t)))
                    (fn [t] (.toString t)))]
    (repeatedly
     count
     (fn []
       (let [t (rand-instant from to)
             te (.plusSeconds t (rand-int 3600))]
         (merge
          (make-event)
          {:timestamp (formatter t)}
          (when end
            {:end_timestamp (formatter te)})))))))

(defn resolver-map
  []
  {:query/simple-series resolve-simple-series
   :query/complex-series resolve-complex-series
   :query/events resolve-events})

(defn load-schema
  []
  (-> (io/resource "schema.edn")
      slurp
      edn/read-string
      (util/attach-resolvers (resolver-map))
      schema/compile))
