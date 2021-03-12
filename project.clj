(defproject graphql-test-source "0.1.0-SNAPSHOT"
  :description "GraphQL server for testing the Grafana GraphQL datasource plugin"
  :url "http://github.com/fifemon/graphql-test-source"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [com.walmartlabs/lacinia-pedestal "0.15.0"]
                 [io.aviso/logging "0.2.0"]]
  :repl-options {:init-ns graphql-test-source.core}
  :main graphql-test-source.server)
