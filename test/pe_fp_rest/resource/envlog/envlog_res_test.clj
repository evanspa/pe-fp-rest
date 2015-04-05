(ns pe-fp-rest.resource.envlog.envlog-res-test
  (:require [clojure.test :refer :all]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [clojure.pprint :refer (pprint)]
            [datomic.api :refer [q db] :as d]
            [compojure.core :refer [defroutes ANY]]
            [ring.middleware.cookies :refer [wrap-cookies]]
            [compojure.handler :as handler]
            [ring.mock.request :as mock]
            [pe-datomic-utils.core :as ducore]
            [pe-apptxn-core.core :as apptxncore]
            [pe-apptxn-restsupport.version.resource-support-v001]
            [pe-user-rest.resource.users-res :as userres]
            [pe-user-rest.resource.version.users-res-v001]
            [pe-fp-rest.resource.vehicle.vehicles-res :as vehsres]
            [pe-fp-rest.resource.vehicle.version.vehicles-res-v001]
            [pe-fp-rest.resource.envlog.envlogs-res :as envlogsres]
            [pe-fp-rest.resource.envlog.version.envlogs-res-v001]
            [pe-fp-rest.resource.envlog.envlog-res :as envlogres]
            [pe-fp-rest.resource.envlog.version.envlog-res-v001]
            [pe-fp-rest.meta :as meta]
            [pe-fp-core.core :as fpcore]
            [pe-core-testutils.core :as tucore]
            [pe-rest-testutils.core :as rtucore]
            [pe-core-utils.core :as ucore]
            [pe-rest-utils.core :as rucore]
            [pe-rest-utils.meta :as rumeta]
            [pe-user-core.core :as usercore]
            [pe-user-rest.meta :as usermeta]
            [pe-user-rest.resource.users-res :as userres]
            [pe-fp-rest.test-utils :refer [db-uri
                                           fp-partition
                                           fpmt-subtype-prefix
                                           fp-auth-scheme
                                           fp-auth-scheme-param-name
                                           fp-schema-files
                                           user-schema-files
                                           apptxn-logging-schema-files
                                           base-url
                                           fphdr-auth-token
                                           fphdr-error-mask
                                           fphdr-apptxn-id
                                           fphdr-useragent-device-make
                                           fphdr-useragent-device-os
                                           fphdr-useragent-device-os-version
                                           fphdr-establish-session
                                           entity-uri-prefix
                                           fphdr-establish-session
                                           users-uri-template
                                           vehicles-uri-template
                                           envlogs-uri-template
                                           envlog-uri-template]]))
(def conn (atom nil))

(defn empty-embedded-resources-fn
  [version
   base-url
   entity-uri-prefix
   entity-uri
   conn
   accept-format-ind
   user-entid]
  {})

(defn empty-links-fn
  [version
   base-url
   entity-uri-prefix
   entity-uri
   user-entid]
  {})

(defroutes routes
  (ANY users-uri-template
       []
       (userres/users-res @conn
                          fp-partition
                          fp-partition
                          fpmt-subtype-prefix
                          fphdr-auth-token
                          fphdr-error-mask
                          base-url
                          entity-uri-prefix
                          fphdr-apptxn-id
                          fphdr-useragent-device-make
                          fphdr-useragent-device-os
                          fphdr-useragent-device-os-version
                          fphdr-establish-session
                          empty-embedded-resources-fn
                          empty-links-fn))
  (ANY vehicles-uri-template
       [user-entid]
       (vehsres/vehicles-res @conn
                            fp-partition
                            fp-partition
                            fpmt-subtype-prefix
                            fphdr-auth-token
                            fphdr-error-mask
                            fp-auth-scheme
                            fp-auth-scheme-param-name
                            base-url
                            entity-uri-prefix
                            fphdr-apptxn-id
                            fphdr-useragent-device-make
                            fphdr-useragent-device-os
                            fphdr-useragent-device-os-version
                            (Long. user-entid)
                            empty-embedded-resources-fn
                            empty-links-fn))
  (ANY envlogs-uri-template
       [user-entid]
       (envlogsres/envlogs-res @conn
                               fp-partition
                               fp-partition
                               fpmt-subtype-prefix
                               fphdr-auth-token
                               fphdr-error-mask
                               fp-auth-scheme
                               fp-auth-scheme-param-name
                               base-url
                               entity-uri-prefix
                               fphdr-apptxn-id
                               fphdr-useragent-device-make
                               fphdr-useragent-device-os
                               fphdr-useragent-device-os-version
                               (Long. user-entid)
                               empty-embedded-resources-fn
                               empty-links-fn))
  (ANY envlog-uri-template
       [user-entid envlog-entid]
       (envlogres/envlog-res @conn
                             fp-partition
                             fp-partition
                             fpmt-subtype-prefix
                             fphdr-auth-token
                             fphdr-error-mask
                             fp-auth-scheme
                             fp-auth-scheme-param-name
                             base-url
                             entity-uri-prefix
                             fphdr-apptxn-id
                             fphdr-useragent-device-make
                             fphdr-useragent-device-os
                             fphdr-useragent-device-os-version
                             (Long. user-entid)
                             (Long. envlog-entid)
                             empty-embedded-resources-fn
                             empty-links-fn)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Middleware-decorated app
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def app
  (-> routes
      (handler/api)
      (wrap-cookies)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Fixtures
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(use-fixtures :each (tucore/make-db-refresher-fixture-fn db-uri
                                                         conn
                                                         fp-partition
                                                         (concat fp-schema-files
                                                                 user-schema-files
                                                                 apptxn-logging-schema-files)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Tests
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(deftest integration-tests-1
  (testing "Successful creation of user, app txn logs and vehicles."
    (is (nil? (usercore/load-user-by-email @conn "smithka@testing.com")))
    (is (nil? (usercore/load-user-by-username @conn "smithk")))
    (let [user {"user/name" "Karen Smith"
                "user/email" "smithka@testing.com"
                "user/username" "smithk"
                "user/password" "insecure"}
          req (-> (rtucore/req-w-std-hdrs rumeta/mt-type
                                          (usermeta/mt-subtype-user fpmt-subtype-prefix)
                                          usermeta/v001
                                          "UTF-8;q=1,ISO-8859-1;q=0"
                                          "json"
                                          "en-US"
                                          :post
                                          users-uri-template
                                          fphdr-apptxn-id
                                          fphdr-useragent-device-make
                                          fphdr-useragent-device-os
                                          fphdr-useragent-device-os-version)
                  (rtucore/header fphdr-establish-session "true")
                  (mock/body (json/write-str user))
                  (mock/content-type (rucore/content-type rumeta/mt-type
                                                          (usermeta/mt-subtype-user fpmt-subtype-prefix)
                                                          usermeta/v001
                                                          "json"
                                                          "UTF-8")))
          resp (app req)]
      (let [hdrs (:headers resp)
            resp-body-stream (:body resp)
            user-location-str (get hdrs "location")
            resp-user-entid-str (rtucore/last-url-part user-location-str)
            pct (rucore/parse-media-type (get hdrs "Content-Type"))
            charset (get rumeta/char-sets (:charset pct))
            resp-user (rucore/read-res pct resp-body-stream charset)
            auth-token (get hdrs fphdr-auth-token)
            [loaded-user-entid loaded-user-ent] (usercore/load-user-by-authtoken @conn
                                                                                 (Long. resp-user-entid-str)
                                                                                 auth-token)]
        ;; Create 1st vehicle
        (is (empty? (fpcore/vehicles-for-user @conn loaded-user-entid)))
        (let [vehicle {"fpvehicle/name" "300Z"
                       "fpvehicle/fuel-capacity" 19.0
                       "fpvehicle/min-reqd-octane" 93}
              vehicles-uri (str base-url
                                entity-uri-prefix
                                usermeta/pathcomp-users
                                "/"
                                resp-user-entid-str
                                "/"
                                meta/pathcomp-vehicles)
              req (-> (rtucore/req-w-std-hdrs rumeta/mt-type
                                              (meta/mt-subtype-vehicle fpmt-subtype-prefix)
                                              meta/v001
                                              "UTF-8;q=1,ISO-8859-1;q=0"
                                              "json"
                                              "en-US"
                                              :post
                                              vehicles-uri
                                              fphdr-apptxn-id
                                              fphdr-useragent-device-make
                                              fphdr-useragent-device-os
                                              fphdr-useragent-device-os-version)
                      (mock/body (json/write-str vehicle))
                      (mock/content-type (rucore/content-type rumeta/mt-type
                                                              (meta/mt-subtype-vehicle fpmt-subtype-prefix)
                                                              meta/v001
                                                              "json"
                                                              "UTF-8"))
                      (rtucore/header "Authorization" (rtucore/authorization-req-hdr-val fp-auth-scheme
                                                                                         fp-auth-scheme-param-name
                                                                                         auth-token)))
              resp (app req)]
          (testing "status code" (is (= 201 (:status resp))))
          (let [hdrs (:headers resp)
                resp-body-stream (:body resp)
                veh-location-str (get hdrs "location")]
            (let [loaded-vehicles (fpcore/vehicles-for-user @conn loaded-user-entid)]
              (is (= 1 (count loaded-vehicles)))
              ; Create 1st environment log
              (is (empty? (fpcore/envlogs-for-user @conn loaded-user-entid)))
              (let [envlog {"fpenvironmentlog/vehicle" veh-location-str
                            "fpenvironmentlog/log-date" "Sun, 31 Oct 2014 11:25:57 GMT"
                            "fpenvironmentlog/reported-avg-mpg" 24
                            "fpenvironmentlog/reported-avg-mph" 22.1
                            "fpenvironmentlog/odometer" 25001.2
                            "fpenvironmentlog/outside-temp" 46.4
                            "fpenvironmentlog/dte" 168}
                    envlogs-uri (str base-url
                                     entity-uri-prefix
                                     usermeta/pathcomp-users
                                     "/"
                                     resp-user-entid-str
                                     "/"
                                     meta/pathcomp-environment-logs)
                    req (-> (rtucore/req-w-std-hdrs rumeta/mt-type
                                                    (meta/mt-subtype-envlog fpmt-subtype-prefix)
                                                    meta/v001
                                                    "UTF-8;q=1,ISO-8859-1;q=0"
                                                    "json"
                                                    "en-US"
                                                    :post
                                                    envlogs-uri
                                                    fphdr-apptxn-id
                                                    fphdr-useragent-device-make
                                                    fphdr-useragent-device-os
                                                    fphdr-useragent-device-os-version)
                            (mock/body (json/write-str envlog))
                            (mock/content-type (rucore/content-type rumeta/mt-type
                                                                    (meta/mt-subtype-envlog fpmt-subtype-prefix)
                                                                    meta/v001
                                                                    "json"
                                                                    "UTF-8"))
                            (rtucore/header "Authorization" (rtucore/authorization-req-hdr-val fp-auth-scheme
                                                                                               fp-auth-scheme-param-name
                                                                                               auth-token)))
                    resp (app req)
                    hdrs (:headers resp)
                    envlog-location-str (get hdrs "location")
                    resp-envlog-entid-str (rtucore/last-url-part envlog-location-str)
                    pct (rucore/parse-media-type (get hdrs "Content-Type"))
                    charset (get rumeta/char-sets (:charset pct))
                    resp-body-stream (:body resp)
                    resp-envlog (rucore/read-res pct resp-body-stream charset)
                    resp-envlog-veh-link (get resp-envlog "fpenvironmentlog/vehicle")
                    resp-envlog-veh-entid (Long/parseLong (rtucore/last-url-part resp-envlog-veh-link))]
                (testing "status code" (is (= 201 (:status resp))))
                (let [loaded-envlogs (fpcore/envlogs-for-user @conn loaded-user-entid)]
                  (is (= 1 (count loaded-envlogs)))
                  ;; Update 1st environment log
                  (let [[[loaded-envlog-entid loaded-envlog]] loaded-envlogs]
                    (is (= (Long/parseLong resp-envlog-entid-str) loaded-envlog-entid))
                    (is (= resp-envlog-veh-entid (-> loaded-envlog
                                                     :fpenvironmentlog/vehicle
                                                     :db/id)))
                    (is (= (ucore/rfc7231str->instant "Sun, 31 Oct 2014 11:25:57 GMT") (:fpenvironmentlog/log-date loaded-envlog)))
                    (is (= 24.0 (:fpenvironmentlog/reported-avg-mpg loaded-envlog)))
                    (is (= 22.1 (:fpenvironmentlog/reported-avg-mph loaded-envlog)))
                    (is (= 25001.2 (:fpenvironmentlog/odometer loaded-envlog)))
                    (is (= 46.4 (:fpenvironmentlog/outside-temp loaded-envlog)))
                    (is (= 168 (:fpenvironmentlog/dte loaded-envlog)))
                    (let [envlog {"fpenvironmentlog/vehicle" veh-location-str
                                  "fpenvironmentlog/log-date" "Sat, 30 Oct 2014 11:25:57 GMT"
                                  "fpenvironmentlog/reported-avg-mpg" 23
                                  "fpenvironmentlog/reported-avg-mph" 21.1
                                  "fpenvironmentlog/odometer" 25000.2
                                  "fpenvironmentlog/outside-temp" 45.4
                                  "fpenvironmentlog/dte" 167}
                          req (-> (rtucore/req-w-std-hdrs rumeta/mt-type
                                                          (meta/mt-subtype-envlog fpmt-subtype-prefix)
                                                          meta/v001
                                                          "UTF-8;q=1,ISO-8859-1;q=0"
                                                          "json"
                                                          "en-US"
                                                          :put
                                                          envlog-location-str
                                                          fphdr-apptxn-id
                                                          fphdr-useragent-device-make
                                                          fphdr-useragent-device-os
                                                          fphdr-useragent-device-os-version)
                                  (mock/body (json/write-str envlog))
                                  (mock/content-type (rucore/content-type rumeta/mt-type
                                                                          (meta/mt-subtype-envlog fpmt-subtype-prefix)
                                                                          meta/v001
                                                                          "json"
                                                                          "UTF-8"))
                                  (rtucore/header "Authorization" (rtucore/authorization-req-hdr-val fp-auth-scheme
                                                                                                     fp-auth-scheme-param-name
                                                                                                     auth-token)))
                          resp (app req)]
                      (testing "status code" (is (= 200 (:status resp))))
                      (let [loaded-envlogs (fpcore/envlogs-for-user @conn loaded-user-entid)]
                        (is (= 1 (count loaded-envlogs)))
                        (let [[[_ loaded-envlog]] loaded-envlogs]
                          (is (= (Long/parseLong resp-envlog-entid-str) loaded-envlog-entid))
                          (is (= resp-envlog-veh-entid (-> loaded-envlog
                                                           :fpenvironmentlog/vehicle
                                                           :db/id)))
                          (is (= (ucore/rfc7231str->instant "Sat, 30 Oct 2014 11:25:57 GMT") (:fpenvironmentlog/log-date loaded-envlog)))
                          (is (= 23.0 (:fpenvironmentlog/reported-avg-mpg loaded-envlog)))
                          (is (= 21.1 (:fpenvironmentlog/reported-avg-mph loaded-envlog)))
                          (is (= 25000.2 (:fpenvironmentlog/odometer loaded-envlog)))
                          (is (= 45.4 (:fpenvironmentlog/outside-temp loaded-envlog)))
                          (is (= 167 (:fpenvironmentlog/dte loaded-envlog))))))))))))))))
