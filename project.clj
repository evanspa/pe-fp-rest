(defproject pe-fp-rest "0.0.40-SNAPSHOT"
  :description "A Clojure library providing a REST API interface on top of the Gas Jot abstractions."
  :url "https://github.com/evanspa/pe-fp-rest"
  :license {:name "MIT"
            :url "http://opensource.org/licenses/MIT"}
  :plugins [[lein-pprint "1.1.2"]
            [codox "0.8.10"]]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/data.codec "0.1.0"]
                 [org.clojure/data.json "0.2.5"]
                 [compojure "1.2.1"]
                 [liberator "0.12.2"]
                 [ch.qos.logback/logback-classic "1.0.13"]
                 [org.slf4j/slf4j-api "1.7.5"]
                 [clj-time "0.8.0"]
                 [org.postgis/postgis-jdbc "1.3.3"]
                 [pe-core-utils "0.0.12"]
                 [pe-jdbc-utils "0.0.19"]
                 [pe-user-core "0.1.39"]
                 [pe-rest-utils "0.0.35"]
                 [pe-user-rest "0.0.52"]
                 [pe-fp-core "0.0.46"]]
  :resource-paths ["resources"]
  :codox {:exclude [user]
          :src-dir-uri "https://github.com/evanspa/pe-fp-rest/blob/0.0.40/"
          :src-linenum-anchor-prefix "L"}
  :profiles {:dev {:source-paths ["dev"]  ;ensures 'user.clj' gets auto-loaded
                   :plugins [[cider/cider-nrepl "0.12.0"]
                             [lein-ring "0.8.13"]]
                   :resource-paths ["test-resources"]
                   :dependencies [[org.clojure/tools.namespace "0.2.7"]
                                  [org.clojure/java.classpath "0.2.2"]
                                  [org.clojure/tools.nrepl "0.2.7"]
                                  [org.clojure/data.json "0.2.5"]
                                  [pe-rest-testutils "0.0.7"]
                                  [clojurewerkz/mailer "1.2.0"]
                                  [de.ubercode.clostache/clostache "1.4.0"]
                                  [org.postgresql/postgresql "9.4-1201-jdbc41"]
                                  [org.postgis/postgis-jdbc "1.3.3"]
                                  [ring/ring-codec "1.0.0"]
                                  [ring-server "0.3.1"]
                                  [ring-mock "0.1.5"]]}
             :test {:resource-paths ["test-resources"]}}
  :jvm-opts ["-Xmx1g" "-DFP_LOGS_DIR=logs"]
  :repositories [["releases" {:url "https://clojars.org/repo"
                              :creds :gpg}]])
