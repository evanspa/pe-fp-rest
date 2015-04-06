(ns pe-fp-rest.resource.vehicle.vehicles-res-test
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
            [pe-user-rest.resource.users-res :as userres]
            [pe-user-rest.resource.version.users-res-v001]
            [pe-fp-rest.resource.vehicle.vehicles-res :as vehsres]
            [pe-fp-rest.resource.vehicle.version.vehicles-res-v001]
            [pe-apptxn-restsupport.version.resource-support-v001]
            [pe-fp-rest.meta :as meta]
            [pe-fp-core.core :as fpcore]
            [pe-datomic-testutils.core :as dtucore]
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
                                           vehicles-uri-template]]))
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
(use-fixtures :each (dtucore/make-db-refresher-fixture-fn db-uri
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
            user-last-modified-str (get hdrs "last-modified")
            last-modified (ucore/rfc7231str->instant user-last-modified-str)
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
                       "fpvehicle/date-added" "Mon, 01 Sep 2014 11:25:57 GMT"
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
          (testing "headers and body of created 300Z vehicle"
            (let [hdrs (:headers resp)
                  resp-body-stream (:body resp)
                  veh-location-str (get hdrs "location")
                  veh-last-modified-str (get hdrs "last-modified")]
              (is (= "Accept, Accept-Charset, Accept-Language" (get hdrs "Vary")))
              (is (not (nil? resp-body-stream)))
              (is (not (nil? veh-location-str)))
              (is (not (nil? veh-last-modified-str)))
              (let [last-modified (ucore/rfc7231str->instant veh-last-modified-str)
                    resp-veh-entid-str (rtucore/last-url-part veh-location-str)
                    pct (rucore/parse-media-type (get hdrs "Content-Type"))
                    charset (get rumeta/char-sets (:charset pct))
                    resp-veh (rucore/read-res pct resp-body-stream charset)]
                (is (not (nil? last-modified)))
                (is (not (nil? resp-veh-entid-str)))
                (is (not (nil? resp-veh)))
                (is (= "300Z" (get resp-veh "fpvehicle/name")))
                (is (= 19.0 (get resp-veh "fpvehicle/fuel-capacity")))
                (is (= 93 (get resp-veh "fpvehicle/min-reqd-octane")))
                (let [loaded-vehicles (fpcore/vehicles-for-user @conn loaded-user-entid)]
                  (is (= 1 (count loaded-vehicles)))
                  (let [[[loaded-veh-300z-entid loaded-veh-300z]] loaded-vehicles]
                    (is (= (Long/parseLong resp-veh-entid-str) loaded-veh-300z-entid))
                    (is (= "300Z" (:fpvehicle/name loaded-veh-300z)))
                    (is (= 19.0 (:fpvehicle/fuel-capacity loaded-veh-300z)))
                    (is (= 93 (:fpvehicle/min-reqd-octane loaded-veh-300z)))

                    ;; Create 2nd vehicle
                    (let [vehicle {"fpvehicle/name" "Mazda CX-9"
                                   "fpvehicle/fuel-capacity" 24.5
                                   "fpvehicle/date-added" "Tue, 02 Sep 2014 11:25:57 GMT"
                                   "fpvehicle/min-reqd-octane" 87}
                          req (-> (rtucore/req-w-std-hdrs rumeta/mt-type
                                                          (meta/mt-subtype-vehicle fpmt-subtype-prefix)
                                                          meta/v001
                                                          "UTF-8;q=1,ISO-8859-1;q=0"
                                                          "json"
                                                          "en-US"
                                                          :post
                                                          (str base-url
                                                               entity-uri-prefix
                                                               usermeta/pathcomp-users
                                                               "/"
                                                               resp-user-entid-str
                                                               "/"
                                                               meta/pathcomp-vehicles)
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
                      (testing "headers and body of created mazda vehicle"
                        (let [hdrs (:headers resp)
                              resp-body-stream (:body resp)
                              veh-location-str (get hdrs "location")
                              veh-last-modified-str (get hdrs "last-modified")]
                          (is (= "Accept, Accept-Charset, Accept-Language" (get hdrs "Vary")))
                          (is (not (nil? resp-body-stream)))
                          (is (not (nil? veh-location-str)))
                          (is (not (nil? veh-last-modified-str)))
                          (let [last-modified (ucore/rfc7231str->instant veh-last-modified-str)
                                resp-veh-entid-str (rtucore/last-url-part veh-location-str)
                                pct (rucore/parse-media-type (get hdrs "Content-Type"))
                                charset (get rumeta/char-sets (:charset pct))
                                resp-veh (rucore/read-res pct resp-body-stream charset)]
                            (is (not (nil? last-modified)))
                            (is (not (nil? resp-veh-entid-str)))
                            (is (not (nil? resp-veh)))
                            (is (= "Mazda CX-9" (get resp-veh "fpvehicle/name")))
                            (is (= 24.5 (get resp-veh "fpvehicle/fuel-capacity")))
                            (is (= 87 (get resp-veh "fpvehicle/min-reqd-octane")))
                            (let [loaded-vehicles (sort-by :fpvehicle/date-added (vec (fpcore/vehicles-for-user @conn loaded-user-entid)))]
                              (is (= 2 (count loaded-vehicles)))
                              (let [[[loaded-veh-mazda-entid loaded-veh-mazda] _] loaded-vehicles]
                                (is (= (Long/parseLong resp-veh-entid-str) loaded-veh-mazda-entid))
                                (is (= "Mazda CX-9" (:fpvehicle/name loaded-veh-mazda)))
                                (is (= 24.5 (:fpvehicle/fuel-capacity loaded-veh-mazda)))
                                (is (= 87 (:fpvehicle/min-reqd-octane loaded-veh-mazda)))))))))))))))))))
