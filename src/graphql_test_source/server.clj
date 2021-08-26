(ns graphql-test-source.server
  (:require
   [graphql-test-source.schema :as s]
   [io.pedestal.http :as http]
   [com.walmartlabs.lacinia.pedestal2 :as lp]
   [com.walmartlabs.lacinia.schema :as schema])
  (:gen-class))

(def schema (s/load-schema))

;; Use default options:
(def service (lp/default-service schema {:host "0.0.0.0"}))

;; This is an adapted service map, that can be started and stopped
;; From the REPL you can call server/start and server/stop on this service
(defonce runnable-service (http/create-server service))

(defn -main
  "The entry-point for 'lein run'"
  [& args]
  (println "\nCreating server listening on 0.0.0.0:8888...\n    GraphiQL at http://localhost:8888/ide\n    Query at http://localhost:8888/api")
  (http/start runnable-service))
