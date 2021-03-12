(ns user
  (:require
   [graphql-test-source.schema :as s]
   [com.walmartlabs.lacinia :as lacinia]
   [com.walmartlabs.lacinia.pedestal2 :as lp]
   [io.pedestal.http :as http]
   [clojure.java.browse :refer [browse-url]]))

(def schema (s/load-schema))

(defn q
  [query-string]
  (lacinia/execute schema query-string nil nil))

(comment
  (q "query {simple_series(from:\"2021-01-01T00:00:00Z\",to:\"2021-01-02T00:00:00Z\"){timestamp value}}")
  )

;; Use default options:
(def service (lp/default-service schema nil))
(defonce runnable-service (http/create-server service))

(comment
  (http/start runnable-service)
  (http/stop runnable-service)
  )
