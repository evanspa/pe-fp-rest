(ns pe-fp-rest.resource.envlog.envlogs-res-test
  (:require [clojure.test :refer :all]
            [clojure.data.json :as json]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [clojure.tools.logging :as log]
            [compojure.core :refer [defroutes ANY]]
            [ring.middleware.cookies :refer [wrap-cookies]]
            [compojure.handler :as handler]
            [ring.mock.request :as mock]
            [pe-user-rest.resource.users-res :as userres]
            [pe-user-rest.resource.version.users-res-v001]
            [pe-fp-rest.resource.vehicle.vehicles-res :as vehsres]
            [pe-fp-rest.resource.vehicle.version.vehicles-res-v001]
            [pe-fp-rest.resource.envlog.envlogs-res :as envlogsres]
            [pe-fp-rest.resource.envlog.version.envlogs-res-v001]
            [pe-fp-rest.meta :as meta]
            [pe-fp-core.core :as fpcore]
            [pe-rest-testutils.core :as rtucore]
            [pe-core-utils.core :as ucore]
            [pe-rest-utils.core :as rucore]
            [pe-rest-utils.meta :as rumeta]
            [pe-user-core.core :as usercore]
            [pe-user-rest.meta :as usermeta]
            [pe-user-rest.resource.users-res :as userres]
            [pe-fp-rest.test-utils :refer [fpmt-subtype-prefix
                                           fp-auth-scheme
                                           fp-auth-scheme-param-name
                                           base-url
                                           fphdr-auth-token
                                           fphdr-error-mask
                                           fphdr-establish-session
                                           entity-uri-prefix
                                           fphdr-establish-session
                                           users-uri-template
                                           vehicles-uri-template
                                           fuelstations-uri-template
                                           envlogs-uri-template
                                           envlog-uri-template
                                           db-spec
                                           fixture-maker]]))

(defn empty-embedded-resources-fn
  [version
   base-url
   entity-uri-prefix
   entity-uri
   conn
   accept-format-ind
   user-id]
  {})

(defn empty-links-fn
  [version
   base-url
   entity-uri-prefix
   entity-uri
   user-id]
  {})

(defroutes routes
  (ANY users-uri-template
       []
       (userres/users-res db-spec
                          fpmt-subtype-prefix
                          fphdr-auth-token
                          fphdr-error-mask
                          base-url
                          entity-uri-prefix
                          fphdr-establish-session
                          empty-embedded-resources-fn
                          empty-links-fn))
  (ANY vehicles-uri-template
       [user-id]
       (vehsres/vehicles-res db-spec
                             fpmt-subtype-prefix
                             fphdr-auth-token
                             fphdr-error-mask
                             fp-auth-scheme
                             fp-auth-scheme-param-name
                             base-url
                             entity-uri-prefix
                             (Long. user-id)
                             empty-embedded-resources-fn
                             empty-links-fn))
  (ANY envlogs-uri-template
       [user-id]
       (envlogsres/envlogs-res db-spec
                               fpmt-subtype-prefix
                               fphdr-auth-token
                               fphdr-error-mask
                               fp-auth-scheme
                               fp-auth-scheme-param-name
                               base-url
                               entity-uri-prefix
                               (Long. user-id)
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
(use-fixtures :each (fixture-maker))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Tests
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(deftest integration-tests-1
  (testing "Successful creation of user, app txn logs and vehicles."
    (is (nil? (usercore/load-user-by-email db-spec "smithka@testing.com")))
    (is (nil? (usercore/load-user-by-username db-spec "smithk")))
    (let [user {"user/name" "Karen Smith"
                "user/created-at" (c/to-long (t/now))
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
                                          users-uri-template)
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
            resp-user-id-str (rtucore/last-url-part user-location-str)
            pct (rucore/parse-media-type (get hdrs "Content-Type"))
            charset (get rumeta/char-sets (:charset pct))
            resp-user (rucore/read-res pct resp-body-stream charset)
            auth-token (get hdrs fphdr-auth-token)
            [loaded-user-id loaded-user-ent] (usercore/load-user-by-authtoken db-spec
                                                                              (Long. resp-user-id-str)
                                                                              auth-token)]
        ;; Create 1st vehicle
        (is (empty? (fpcore/vehicles-for-user db-spec loaded-user-id)))
        (let [vehicle {"fpvehicle/name" "300Z"
                       "fpvehicle/created-at" (c/to-long (t/now))
                       "fpvehicle/default-octane" 93}
              vehicles-uri (str base-url
                                entity-uri-prefix
                                usermeta/pathcomp-users
                                "/"
                                resp-user-id-str
                                "/"
                                meta/pathcomp-vehicles)
              req (-> (rtucore/req-w-std-hdrs rumeta/mt-type
                                              (meta/mt-subtype-vehicle fpmt-subtype-prefix)
                                              meta/v001
                                              "UTF-8;q=1,ISO-8859-1;q=0"
                                              "json"
                                              "en-US"
                                              :post
                                              vehicles-uri)
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
            (let [loaded-vehicles (fpcore/vehicles-for-user db-spec loaded-user-id)]
              (is (= 1 (count loaded-vehicles)))
                                        ; Create 1st environment log
              (is (empty? (fpcore/envlogs-for-user db-spec loaded-user-id)))
              (let [logged-at (t/now)
                    envlog {"envlog/vehicle" veh-location-str
                            "envlog/created-at" (c/to-long (t/now))
                            "envlog/logged-at" (c/to-long logged-at)
                            "envlog/reported-avg-mpg" 24
                            "envlog/reported-avg-mph" 22.1
                            "envlog/odometer" 25001.2
                            "envlog/reported-outside-temp" 46.4
                            "envlog/dte" 168}
                    envlogs-uri (str base-url
                                     entity-uri-prefix
                                     usermeta/pathcomp-users
                                     "/"
                                     resp-user-id-str
                                     "/"
                                     meta/pathcomp-environment-logs)
                    req (-> (rtucore/req-w-std-hdrs rumeta/mt-type
                                                    (meta/mt-subtype-envlog fpmt-subtype-prefix)
                                                    meta/v001
                                                    "UTF-8;q=1,ISO-8859-1;q=0"
                                                    "json"
                                                    "en-US"
                                                    :post
                                                    envlogs-uri)
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
                (testing "status code" (is (= 201 (:status resp))))
                (testing "headers and body of created envlog"
                  (let [hdrs (:headers resp)
                        resp-body-stream (:body resp)
                        envlog-location-str (get hdrs "location")]
                    (is (= "Accept, Accept-Charset, Accept-Language" (get hdrs "Vary")))
                    (is (not (nil? resp-body-stream)))
                    (is (not (nil? envlog-location-str)))
                    (let [resp-envlog-id-str (rtucore/last-url-part envlog-location-str)
                          pct (rucore/parse-media-type (get hdrs "Content-Type"))
                          charset (get rumeta/char-sets (:charset pct))
                          resp-envlog (rucore/read-res pct resp-body-stream charset)
                          resp-envlog-veh-link (get resp-envlog "envlog/vehicle")
                          resp-envlog-veh-id (Long/parseLong (rtucore/last-url-part resp-envlog-veh-link))]
                      (is (not (nil? resp-envlog-id-str)))
                      (is (not (nil? resp-envlog)))
                      (is (= veh-location-str resp-envlog-veh-link))
                      (is (= logged-at (c/from-long (Long. (get resp-envlog "envlog/logged-at")))))
                      (is (= 24 (get resp-envlog "envlog/reported-avg-mpg")))
                      (is (= 22.1 (get resp-envlog "envlog/reported-avg-mph")))
                      (is (= 25001.2 (get resp-envlog "envlog/odometer")))
                      (is (= 46.4 (get resp-envlog "envlog/reported-outside-temp")))
                      (is (= 168 (get resp-envlog "envlog/dte")))
                      (let [loaded-envlogs (fpcore/envlogs-for-user db-spec loaded-user-id)]
                        (is (= 1 (count loaded-envlogs)))
                        (let [[[loaded-envlog-id loaded-envlog]] loaded-envlogs]
                          (is (= (Long/parseLong resp-envlog-id-str) loaded-envlog-id))
                          (is (= resp-envlog-veh-id (:envlog/vehicle-id loaded-envlog)))
                          (is (= logged-at (:envlog/logged-at loaded-envlog)))
                          (is (= 24.0M (:envlog/reported-avg-mpg loaded-envlog)))
                          (is (= 22.1M (:envlog/reported-avg-mph loaded-envlog)))
                          (is (= 25001.2M (:envlog/odometer loaded-envlog)))
                          (is (= 46.4M (:envlog/reported-outside-temp loaded-envlog)))
                          (is (= 168M (:envlog/dte loaded-envlog))))))))))))))))
