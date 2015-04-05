(defproject pe-fp-rest "0.0.1-SNAPSHOT"
  :description "A Clojure library providing a REST API interface on top of the fuel purchase abstractions."
  :url "https://github.com/evanspa/pe-fp-rest"
  :license {:name "MIT"
            :url "http://opensource.org/licenses/MIT"}
  :plugins [[lein-pprint "1.1.2"]
            [lein-marginalia "0.8.0"]
            [codox "0.8.10"]]
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/data.codec "0.1.0"]
                 [org.clojure/data.json "0.2.5"]
                 [compojure "1.2.1"]
                 [liberator "0.12.2"]
                 [com.datomic/datomic-free "0.9.5130"
                  :exclusions [org.slf4j/slf4j-nop
                               joda-time
                               org.slf4j/slf4j-log4j12]]
                 [pe-apptxn-restsupport "0.0.1"]
                 [pe-rest-utils "0.0.1"]
                 [pe-datomic-utils "0.0.1"]
                 [pe-fp-core "0.0.1-SNAPSHOT"]
                 [pe-core-utils "0.0.6"]
                 [ch.qos.logback/logback-classic "1.0.13"]
                 [org.slf4j/slf4j-api "1.7.5"]
                 [clj-time "0.8.0"]
                 [org.clojure/tools.nrepl "0.2.7"]]
  :resource-paths ["resources"]
  :profiles {:dev {:source-paths ["dev"]  ;ensures 'user.clj' gets auto-loaded
                   :plugins [[cider/cider-nrepl "0.9.0-SNAPSHOT"]
                             [lein-ring "0.8.13"]]
                   :resource-paths ["test-resources"]
                   :dependencies [[org.clojure/tools.namespace "0.2.7"]
                                  [org.clojure/java.classpath "0.2.2"]
                                  [org.clojure/data.json "0.2.5"]
                                  [pe-apptxn-core "0.0.1"] ;for its schema file
                                  [pe-user-core "0.0.1-SNAPSHOT"]
                                  [pe-user-rest "0.0.1-SNAPSHOT"]
                                  [pe-rest-testutils "0.0.1"]
                                  [pe-user-testutils "0.0.1-SNAPSHOT"]
                                  [ring-server "0.3.1"]
                                  [ring-mock "0.1.5"]]}
             :test {:resource-paths ["test-resources"]}})
