(ns pe-fp-rest.resource.fplog.fplog-res-test
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
            [pe-fp-rest.resource.fuelstation.fuelstations-res :as fssres]
            [pe-fp-rest.resource.fuelstation.version.fuelstations-res-v001]
            [pe-fp-rest.resource.fplog.fplogs-res :as fplogsres]
            [pe-fp-rest.resource.fplog.version.fplogs-res-v001]
            [pe-fp-rest.resource.fplog.fplog-res :as fplogres]
            [pe-fp-rest.resource.fplog.version.fplog-res-v001]
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
                                           vehicles-uri-template
                                           fuelstations-uri-template
                                           fplogs-uri-template
                                           fplog-uri-template]]))
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
  (ANY fuelstations-uri-template
       [user-entid]
       (fssres/fuelstations-res @conn
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
  (ANY fplogs-uri-template
       [user-entid]
       (fplogsres/fplogs-res @conn
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
  (ANY fplog-uri-template
       [user-entid fplog-entid]
       (fplogres/fplog-res @conn
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
                           (Long. fplog-entid)
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
            user-location-str (get hdrs "location")
            resp-user-entid-str (rtucore/last-url-part user-location-str)
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
          (let [veh-300zx-location-str (get (:headers resp) "location")]
            (let [loaded-vehicles (fpcore/vehicles-for-user @conn loaded-user-entid)]
              (is (= 1 (count loaded-vehicles)))
              ;; Create 2nd Vehicle
              (let [vehicle {"fpvehicle/name" "Mazda CX-9"
                             "fpvehicle/fuel-capacity" 24.5
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
                (let [veh-cx9-location-str (get (:headers resp) "location")
                      loaded-vehicles (fpcore/vehicles-for-user @conn loaded-user-entid)]
                  (is (= 2 (count loaded-vehicles)))
                  (let [[[loaded-veh-mazda-entid loaded-veh-mazda] _] loaded-vehicles]
                    ;; Create 1st fuel station
                    (is (empty? (fpcore/fuelstations-for-user @conn loaded-user-entid)))
                    (let [fuelstation {"fpfuelstation/name" "Joe's"
                                       "fpfuelstation/street" "101 Main Street"
                                       "fpfuelstation/city" "Charlotte"
                                       "fpfuelstation/state" "NC"
                                       "fpfuelstation/zip" "28277"
                                       "fpfuelstation/longitude" 80.29103
                                       "fpfuelstation/latitude" -13.7002}
                          fuelstations-uri (str base-url
                                                entity-uri-prefix
                                                usermeta/pathcomp-users
                                                "/"
                                                resp-user-entid-str
                                                "/"
                                                meta/pathcomp-fuelstations)
                          req (-> (rtucore/req-w-std-hdrs rumeta/mt-type
                                                          (meta/mt-subtype-fuelstation fpmt-subtype-prefix)
                                                          meta/v001
                                                          "UTF-8;q=1,ISO-8859-1;q=0"
                                                          "json"
                                                          "en-US"
                                                          :post
                                                          fuelstations-uri
                                                          fphdr-apptxn-id
                                                          fphdr-useragent-device-make
                                                          fphdr-useragent-device-os
                                                          fphdr-useragent-device-os-version)
                                  (mock/body (json/write-str fuelstation))
                                  (mock/content-type (rucore/content-type rumeta/mt-type
                                                                          (meta/mt-subtype-fuelstation fpmt-subtype-prefix)
                                                                          meta/v001
                                                                          "json"
                                                                          "UTF-8"))
                                  (rtucore/header "Authorization" (rtucore/authorization-req-hdr-val fp-auth-scheme
                                                                                                     fp-auth-scheme-param-name
                                                                                                     auth-token)))
                          resp (app req)]
                      (testing "status code" (is (= 201 (:status resp))))
                      (let [hdrs (:headers resp)
                            fs-joes-location-str (get hdrs "location")
                            loaded-fuelstations (fpcore/fuelstations-for-user @conn loaded-user-entid)]
                        (is (= 1 (count loaded-fuelstations)))
                        (let [[[loaded-fs-joes-entid loaded-fs-joes]] loaded-fuelstations]
                          ;; Create 2nd fuelstation
                          (let [fuelstation {"fpfuelstation/name" "Ed's"
                                             "fpfuelstation/street" "103 Main Street"
                                             "fpfuelstation/city" "Providence"
                                             "fpfuelstation/state" "NC"
                                             "fpfuelstation/zip" "28278"
                                             "fpfuelstation/longitude" 82.29103
                                             "fpfuelstation/latitude" -14.7002}
                                req (-> (rtucore/req-w-std-hdrs rumeta/mt-type
                                                                (meta/mt-subtype-fuelstation fpmt-subtype-prefix)
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
                                                                     meta/pathcomp-fuelstations)
                                                                fphdr-apptxn-id
                                                                fphdr-useragent-device-make
                                                                fphdr-useragent-device-os
                                                                fphdr-useragent-device-os-version)
                                        (mock/body (json/write-str fuelstation))
                                        (mock/content-type (rucore/content-type rumeta/mt-type
                                                                                (meta/mt-subtype-fuelstation fpmt-subtype-prefix)
                                                                                meta/v001
                                                                                "json"
                                                                                "UTF-8"))
                                        (rtucore/header "Authorization" (rtucore/authorization-req-hdr-val fp-auth-scheme
                                                                                                           fp-auth-scheme-param-name
                                                                                                           auth-token)))
                                resp (app req)]
                            (testing "status code" (is (= 201 (:status resp))))
                            (let [hdrs (:headers resp)
                                  fs-eds-location-str (get hdrs "location")
                                  loaded-fuelstations (fpcore/fuelstations-for-user @conn loaded-user-entid)]
                              (is (= 2 (count loaded-fuelstations)))
                              (let [[[loaded-fs-eds-entid loaded-fs-eds] _] loaded-fuelstations]
                                ;; Create 1st fuel purchase log (veh=Z, fs=Joes)
                                (is (empty? (fpcore/fplogs-for-user @conn loaded-user-entid)))
                                (let [fplog {"fpfuelpurchaselog/vehicle" veh-300zx-location-str
                                             "fpfuelpurchaselog/fuelstation" fs-joes-location-str
                                             "fpfuelpurchaselog/purchase-date" "Mon, 01 Sep 2014 11:25:57 GMT"
                                             "fpfuelpurchaselog/got-car-wash" true
                                             "fpfuelpurchaselog/carwash-per-gal-discount" 0.08
                                             "fpfuelpurchaselog/num-gallons" 14.3
                                             "fpfuelpurchaselog/octane" 87
                                             "fpfuelpurchaselog/gallon-price" 2.29}
                                      fplogs-uri (str base-url
                                                      entity-uri-prefix
                                                      usermeta/pathcomp-users
                                                      "/"
                                                      resp-user-entid-str
                                                      "/"
                                                      meta/pathcomp-fuelpurchase-logs)
                                      req (-> (rtucore/req-w-std-hdrs rumeta/mt-type
                                                                      (meta/mt-subtype-fplog fpmt-subtype-prefix)
                                                                      meta/v001
                                                                      "UTF-8;q=1,ISO-8859-1;q=0"
                                                                      "json"
                                                                      "en-US"
                                                                      :post
                                                                      fplogs-uri
                                                                      fphdr-apptxn-id
                                                                      fphdr-useragent-device-make
                                                                      fphdr-useragent-device-os
                                                                      fphdr-useragent-device-os-version)
                                              (mock/body (json/write-str fplog))
                                              (mock/content-type (rucore/content-type rumeta/mt-type
                                                                                      (meta/mt-subtype-fplog fpmt-subtype-prefix)
                                                                                      meta/v001
                                                                                      "json"
                                                                                      "UTF-8"))
                                              (rtucore/header "Authorization" (rtucore/authorization-req-hdr-val fp-auth-scheme
                                                                                                                 fp-auth-scheme-param-name
                                                                                                                 auth-token)))
                                      resp (app req)]
                                  (testing "status code" (is (= 201 (:status resp))))
                                  (testing "headers and body of created fplog"
                                    (let [hdrs (:headers resp)
                                          resp-body-stream (:body resp)
                                          fplog-location-str (get hdrs "location")
                                          fplog-last-modified-str (get hdrs "last-modified")]
                                      (is (= "Accept, Accept-Charset, Accept-Language" (get hdrs "Vary")))
                                      (is (not (nil? resp-body-stream)))
                                      (is (not (nil? fplog-location-str)))
                                      (is (not (nil? fplog-last-modified-str)))
                                      (let [last-modified (ucore/rfc7231str->instant fplog-last-modified-str)
                                            resp-fplog-entid-str (rtucore/last-url-part fplog-location-str)
                                            pct (rucore/parse-media-type (get hdrs "Content-Type"))
                                            charset (get rumeta/char-sets (:charset pct))
                                            resp-fplog (rucore/read-res pct resp-body-stream charset)
                                            resp-fplog-veh-link (get resp-fplog "fpfuelpurchaselog/vehicle")
                                            resp-fplog-veh-entid (Long/parseLong (rtucore/last-url-part resp-fplog-veh-link))
                                            resp-fplog-fs-link (get resp-fplog "fpfuelpurchaselog/fuelstation")
                                            resp-fplog-fs-entid (Long/parseLong (rtucore/last-url-part resp-fplog-fs-link))]
                                        (is (not (nil? last-modified)))
                                        (is (not (nil? resp-fplog-entid-str)))
                                        (is (not (nil? resp-fplog)))
                                        (is (= veh-300zx-location-str resp-fplog-veh-link))
                                        (is (= fs-joes-location-str resp-fplog-fs-link))
                                        (is (= (ucore/rfc7231str->instant "Mon, 01 Sep 2014 11:25:57 GMT") (get resp-fplog "fpfuelpurchaselog/purchase-date")))
                                        (is (= true (get resp-fplog "fpfuelpurchaselog/got-car-wash")))
                                        (is (= 0.08 (get resp-fplog "fpfuelpurchaselog/carwash-per-gal-discount")))
                                        (is (= 14.3 (get resp-fplog "fpfuelpurchaselog/num-gallons")))
                                        (is (= 87 (get resp-fplog "fpfuelpurchaselog/octane")))
                                        (is (= 2.29 (get resp-fplog "fpfuelpurchaselog/gallon-price")))
                                        (let [loaded-fplogs (fpcore/fplogs-for-user @conn loaded-user-entid)]
                                          (is (= 1 (count loaded-fplogs)))
                                          (let [[[loaded-fplog-entid loaded-fplog]] loaded-fplogs]
                                            (is (= (Long/parseLong resp-fplog-entid-str) loaded-fplog-entid))
                                            (is (= resp-fplog-veh-entid (-> loaded-fplog
                                                                            :fpfuelpurchaselog/vehicle
                                                                            :db/id)))
                                            (is (= resp-fplog-fs-entid (-> loaded-fplog
                                                                           :fpfuelpurchaselog/fuelstation
                                                                           :db/id)))
                                            (is (= true (:fpfuelpurchaselog/got-car-wash loaded-fplog)))
                                            (is (= 0.08 (:fpfuelpurchaselog/carwash-per-gal-discount loaded-fplog)))
                                            (is (= 14.3 (:fpfuelpurchaselog/num-gallons loaded-fplog)))
                                            (is (= 87 (:fpfuelpurchaselog/octane loaded-fplog)))
                                            (is (= 2.29 (:fpfuelpurchaselog/gallon-price loaded-fplog)))
                                            ;; Update 1st fuel purchase log
                                            (let [fplog {"fpfuelpurchaselog/vehicle" veh-cx9-location-str
                                                         "fpfuelpurchaselog/fuelstation" fs-eds-location-str
                                                         "fpfuelpurchaselog/purchase-date" "Tue, 02 Sep 2014 11:25:57 GMT"
                                                         "fpfuelpurchaselog/got-car-wash" false
                                                         "fpfuelpurchaselog/carwash-per-gal-discount" 0.15
                                                         "fpfuelpurchaselog/num-gallons" 15.3
                                                         "fpfuelpurchaselog/octane" 88
                                                         "fpfuelpurchaselog/gallon-price" 2.31}
                                                  req (-> (rtucore/req-w-std-hdrs rumeta/mt-type
                                                                                  (meta/mt-subtype-fplog fpmt-subtype-prefix)
                                                                                  meta/v001
                                                                                  "UTF-8;q=1,ISO-8859-1;q=0"
                                                                                  "json"
                                                                                  "en-US"
                                                                                  :put
                                                                                  fplog-location-str
                                                                                  fphdr-apptxn-id
                                                                                  fphdr-useragent-device-make
                                                                                  fphdr-useragent-device-os
                                                                                  fphdr-useragent-device-os-version)
                                                          (mock/body (json/write-str fplog))
                                                          (mock/content-type (rucore/content-type rumeta/mt-type
                                                                                                  (meta/mt-subtype-fplog fpmt-subtype-prefix)
                                                                                                  meta/v001
                                                                                                  "json"
                                                                                                  "UTF-8"))
                                                          (rtucore/header "Authorization" (rtucore/authorization-req-hdr-val fp-auth-scheme
                                                                                                                             fp-auth-scheme-param-name
                                                                                                                             auth-token)))
                                                  resp (app req)]
                                              (testing "status code" (is (= 200 (:status resp))))
                                              (testing "headers and body of created fplog"
                                                (let [hdrs (:headers resp)
                                                      resp-body-stream (:body resp)
                                                      fplog-last-modified-str (get hdrs "last-modified")]
                                                  (is (= "Accept, Accept-Charset, Accept-Language" (get hdrs "Vary")))
                                                  (is (not (nil? resp-body-stream)))
                                                  (is (not (nil? fplog-location-str)))
                                                  (is (not (nil? fplog-last-modified-str)))
                                                  (let [last-modified (ucore/rfc7231str->instant fplog-last-modified-str)
                                                        resp-fplog-entid-str (rtucore/last-url-part fplog-location-str)
                                                        pct (rucore/parse-media-type (get hdrs "Content-Type"))
                                                        charset (get rumeta/char-sets (:charset pct))
                                                        resp-fplog (rucore/read-res pct resp-body-stream charset)
                                                        resp-fplog-veh-link (get resp-fplog "fpfuelpurchaselog/vehicle")
                                                        resp-fplog-veh-entid (Long/parseLong (rtucore/last-url-part resp-fplog-veh-link))
                                                        resp-fplog-fs-link (get resp-fplog "fpfuelpurchaselog/fuelstation")
                                                        resp-fplog-fs-entid (Long/parseLong (rtucore/last-url-part resp-fplog-fs-link))]
                                                    (is (not (nil? last-modified)))
                                                    (is (not (nil? resp-fplog-entid-str)))
                                                    (is (not (nil? resp-fplog)))
                                                    (is (= veh-cx9-location-str resp-fplog-veh-link))
                                                    (is (= fs-eds-location-str resp-fplog-fs-link))
                                                    (is (= (ucore/rfc7231str->instant "Tue, 02 Sep 2014 11:25:57 GMT") (get resp-fplog "fpfuelpurchaselog/purchase-date")))
                                                    (is (= false (get resp-fplog "fpfuelpurchaselog/got-car-wash")))
                                                    (is (= 0.15 (get resp-fplog "fpfuelpurchaselog/carwash-per-gal-discount")))
                                                    (is (= 15.3 (get resp-fplog "fpfuelpurchaselog/num-gallons")))
                                                    (is (= 88 (get resp-fplog "fpfuelpurchaselog/octane")))
                                                    (is (= 2.31 (get resp-fplog "fpfuelpurchaselog/gallon-price")))
                                                    (let [loaded-fplogs (fpcore/fplogs-for-user @conn loaded-user-entid)]
                                                      (is (= 1 (count loaded-fplogs)))
                                                      (let [[[loaded-fplog-entid loaded-fplog]] loaded-fplogs]
                                                        (is (= (Long/parseLong resp-fplog-entid-str) loaded-fplog-entid))
                                                        (is (= resp-fplog-veh-entid (-> loaded-fplog
                                                                                        :fpfuelpurchaselog/vehicle
                                                                                        :db/id)))
                                                        (is (= resp-fplog-fs-entid (-> loaded-fplog
                                                                                       :fpfuelpurchaselog/fuelstation
                                                                                       :db/id)))
                                                        (is (= false (:fpfuelpurchaselog/got-car-wash loaded-fplog)))
                                                        (is (= 0.15 (:fpfuelpurchaselog/carwash-per-gal-discount loaded-fplog)))
                                                        (is (= 15.3 (:fpfuelpurchaselog/num-gallons loaded-fplog)))
                                                        (is (= 88 (:fpfuelpurchaselog/octane loaded-fplog)))
                                                        (is (= 2.31 (:fpfuelpurchaselog/gallon-price loaded-fplog)))))))))))))))))))))))))))))))
