(ns pe-fp-rest.resource.fplog.fplog-res-test
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
            [pe-fp-rest.resource.fuelstation.fuelstations-res :as fssres]
            [pe-fp-rest.resource.fuelstation.version.fuelstations-res-v001]
            [pe-fp-rest.resource.fplog.fplogs-res :as fplogsres]
            [pe-fp-rest.resource.fplog.version.fplogs-res-v001]
            [pe-fp-rest.resource.fplog.fplog-res :as fplogres]
            [pe-fp-rest.resource.fplog.version.fplog-res-v001]
            [pe-fp-rest.meta :as meta]
            [pe-fp-core.core :as fpcore]
            [pe-rest-testutils.core :as rtucore]
            [pe-core-utils.core :as ucore]
            [pe-rest-utils.core :as rucore]
            [pe-jdbc-utils.core :as jcore]
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
                                           fplogs-uri-template
                                           fplog-uri-template
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
  (ANY fuelstations-uri-template
       [user-id]
       (fssres/fuelstations-res db-spec
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
  (ANY fplogs-uri-template
       [user-id]
       (fplogsres/fplogs-res db-spec
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
  (ANY fplog-uri-template
       [user-id fplog-id]
       (fplogres/fplog-res db-spec
                           fpmt-subtype-prefix
                           fphdr-auth-token
                           fphdr-error-mask
                           fp-auth-scheme
                           fp-auth-scheme-param-name
                           base-url
                           entity-uri-prefix
                           (Long. user-id)
                           (Long. fplog-id)
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
            user-location-str (get hdrs "location")
            resp-user-id-str (rtucore/last-url-part user-location-str)
            auth-token (get hdrs fphdr-auth-token)
            [loaded-user-id loaded-user-ent] (usercore/load-user-by-authtoken db-spec
                                                                                 (Long. resp-user-id-str)
                                                                                 auth-token)]
        ;; Create 1st vehicle
        (is (empty? (fpcore/vehicles-for-user db-spec loaded-user-id)))
        (let [vehicle {"fpvehicle/name" "300Z"
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
          (let [veh-300zx-location-str (get (:headers resp) "location")]
            (let [loaded-vehicles (fpcore/vehicles-for-user db-spec loaded-user-id)]
              (is (= 1 (count loaded-vehicles)))
              ;; Create 2nd Vehicle
              (let [vehicle {"fpvehicle/name" "Mazda CX-9"
                             "fpvehicle/default-octane" 87}
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
                                                         resp-user-id-str
                                                         "/"
                                                         meta/pathcomp-vehicles))
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
                      loaded-vehicles (fpcore/vehicles-for-user db-spec loaded-user-id)]
                  (is (= 2 (count loaded-vehicles)))
                  (let [[[loaded-veh-mazda-id loaded-veh-mazda] _] loaded-vehicles]
                    ;; Create 1st fuel station
                    (is (empty? (fpcore/fuelstations-for-user db-spec loaded-user-id)))
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
                                                resp-user-id-str
                                                "/"
                                                meta/pathcomp-fuelstations)
                          req (-> (rtucore/req-w-std-hdrs rumeta/mt-type
                                                          (meta/mt-subtype-fuelstation fpmt-subtype-prefix)
                                                          meta/v001
                                                          "UTF-8;q=1,ISO-8859-1;q=0"
                                                          "json"
                                                          "en-US"
                                                          :post
                                                          fuelstations-uri)
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
                            loaded-fuelstations (fpcore/fuelstations-for-user db-spec loaded-user-id)]
                        (is (= 1 (count loaded-fuelstations)))
                        (let [[[loaded-fs-joes-id loaded-fs-joes]] loaded-fuelstations]
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
                                                                     resp-user-id-str
                                                                     "/"
                                                                     meta/pathcomp-fuelstations))
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
                                  loaded-fuelstations (fpcore/fuelstations-for-user db-spec loaded-user-id)]
                              (is (= 2 (count loaded-fuelstations)))
                              (let [[[loaded-fs-eds-id loaded-fs-eds] _] loaded-fuelstations]
                                ;; Create 1st fuel purchase log (veh=Z, fs=Joes)
                                (is (empty? (fpcore/fplogs-for-user db-spec loaded-user-id)))
                                (let [purchased-at (t/now)
                                      fplog {"fplog/vehicle" veh-300zx-location-str
                                             "fplog/fuelstation" fs-joes-location-str
                                             "fplog/purchased-at" (c/to-long purchased-at)
                                             "fplog/got-car-wash" true
                                             "fplog/car-wash-per-gal-discount" 0.08
                                             "fplog/num-gallons" 14.3
                                             "fplog/octane" 87
                                             "fplog/gallon-price" 2.29}
                                      fplogs-uri (str base-url
                                                      entity-uri-prefix
                                                      usermeta/pathcomp-users
                                                      "/"
                                                      resp-user-id-str
                                                      "/"
                                                      meta/pathcomp-fuelpurchase-logs)
                                      req (-> (rtucore/req-w-std-hdrs rumeta/mt-type
                                                                      (meta/mt-subtype-fplog fpmt-subtype-prefix)
                                                                      meta/v001
                                                                      "UTF-8;q=1,ISO-8859-1;q=0"
                                                                      "json"
                                                                      "en-US"
                                                                      :post
                                                                      fplogs-uri)
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
                                          fplog-location-str (get hdrs "location")]
                                      (is (= "Accept, Accept-Charset, Accept-Language" (get hdrs "Vary")))
                                      (is (not (nil? resp-body-stream)))
                                      (is (not (nil? fplog-location-str)))
                                      (let [resp-fplog-id-str (rtucore/last-url-part fplog-location-str)
                                            pct (rucore/parse-media-type (get hdrs "Content-Type"))
                                            charset (get rumeta/char-sets (:charset pct))
                                            resp-fplog (rucore/read-res pct resp-body-stream charset)
                                            resp-fplog-veh-link (get resp-fplog "fplog/vehicle")
                                            resp-fplog-veh-id (Long/parseLong (rtucore/last-url-part resp-fplog-veh-link))
                                            resp-fplog-fs-link (get resp-fplog "fplog/fuelstation")
                                            resp-fplog-fs-id (Long/parseLong (rtucore/last-url-part resp-fplog-fs-link))]
                                        (is (not (nil? resp-fplog-id-str)))
                                        (is (not (nil? resp-fplog)))
                                        (is (= veh-300zx-location-str resp-fplog-veh-link))
                                        (is (= fs-joes-location-str resp-fplog-fs-link))
                                        (is (= purchased-at (c/from-long (Long. (get resp-fplog "fplog/purchased-at")))))
                                        (is (= true (get resp-fplog "fplog/got-car-wash")))
                                        (is (= 0.08 (get resp-fplog "fplog/car-wash-per-gal-discount")))
                                        (is (= 14.3 (get resp-fplog "fplog/num-gallons")))
                                        (is (= 87 (get resp-fplog "fplog/octane")))
                                        (is (= 2.29 (get resp-fplog "fplog/gallon-price")))
                                        (let [loaded-fplogs (fpcore/fplogs-for-user db-spec loaded-user-id)]
                                          (is (= 1 (count loaded-fplogs)))
                                          (let [[[loaded-fplog-id loaded-fplog]] loaded-fplogs]
                                            (is (= (Long/parseLong resp-fplog-id-str) loaded-fplog-id))
                                            (is (= resp-fplog-veh-id (:fplog/vehicle-id loaded-fplog)))
                                            (is (= resp-fplog-fs-id (:fplog/fuelstation-id loaded-fplog)))
                                            (is (= true (:fplog/got-car-wash loaded-fplog)))
                                            (is (= 0.08M (:fplog/car-wash-per-gal-discount loaded-fplog)))
                                            (is (= 14.3M (:fplog/num-gallons loaded-fplog)))
                                            (is (= 87 (:fplog/octane loaded-fplog)))
                                            (is (= 2.29M (:fplog/gallon-price loaded-fplog)))
                                            ;; Update 1st fuel purchase log
                                            (let [purchased-at (t/now)
                                                  fplog {"fplog/vehicle" veh-cx9-location-str
                                                         "fplog/fuelstation" fs-eds-location-str
                                                         "fplog/purchased-at" (c/to-long purchased-at)
                                                         "fplog/got-car-wash" false
                                                         "fplog/car-wash-per-gal-discount" 0.15
                                                         "fplog/num-gallons" 15.3
                                                         "fplog/octane" 88
                                                         "fplog/gallon-price" 2.31}
                                                  req (-> (rtucore/req-w-std-hdrs rumeta/mt-type
                                                                                  (meta/mt-subtype-fplog fpmt-subtype-prefix)
                                                                                  meta/v001
                                                                                  "UTF-8;q=1,ISO-8859-1;q=0"
                                                                                  "json"
                                                                                  "en-US"
                                                                                  :put
                                                                                  fplog-location-str)
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
                                                      resp-body-stream (:body resp)]
                                                  (is (= "Accept, Accept-Charset, Accept-Language" (get hdrs "Vary")))
                                                  (is (not (nil? resp-body-stream)))
                                                  (is (not (nil? fplog-location-str)))
                                                  (let [resp-fplog-id-str (rtucore/last-url-part fplog-location-str)
                                                        pct (rucore/parse-media-type (get hdrs "Content-Type"))
                                                        charset (get rumeta/char-sets (:charset pct))
                                                        resp-fplog (rucore/read-res pct resp-body-stream charset)
                                                        resp-fplog-veh-link (get resp-fplog "fplog/vehicle")
                                                        resp-fplog-veh-id (Long/parseLong (rtucore/last-url-part resp-fplog-veh-link))
                                                        resp-fplog-fs-link (get resp-fplog "fplog/fuelstation")
                                                        resp-fplog-fs-id (Long/parseLong (rtucore/last-url-part resp-fplog-fs-link))]
                                                    (is (not (nil? resp-fplog-id-str)))
                                                    (is (not (nil? resp-fplog)))
                                                    (is (= veh-cx9-location-str resp-fplog-veh-link))
                                                    (is (= fs-eds-location-str resp-fplog-fs-link))
                                                    (is (= purchased-at (c/from-long (Long. (get resp-fplog "fplog/purchased-at")))))
                                                    (is (= false (get resp-fplog "fplog/got-car-wash")))
                                                    (is (= 0.15 (get resp-fplog "fplog/car-wash-per-gal-discount")))
                                                    (is (= 15.3 (get resp-fplog "fplog/num-gallons")))
                                                    (is (= 88 (get resp-fplog "fplog/octane")))
                                                    (is (= 2.31 (get resp-fplog "fplog/gallon-price")))
                                                    (let [loaded-fplogs (fpcore/fplogs-for-user db-spec loaded-user-id)]
                                                      (is (= 1 (count loaded-fplogs)))
                                                      (let [[[loaded-fplog-id loaded-fplog]] loaded-fplogs]
                                                        (is (= (Long/parseLong resp-fplog-id-str) loaded-fplog-id))
                                                        (is (= resp-fplog-veh-id (:fplog/vehicle-id loaded-fplog)))
                                                        (is (= resp-fplog-fs-id (:fplog/fuelstation-id loaded-fplog)))
                                                        (is (= false (:fplog/got-car-wash loaded-fplog)))
                                                        (is (= 0.15M (:fplog/car-wash-per-gal-discount loaded-fplog)))
                                                        (is (= 15.3M (:fplog/num-gallons loaded-fplog)))
                                                        (is (= 88 (:fplog/octane loaded-fplog)))
                                                        (is (= 2.31M (:fplog/gallon-price loaded-fplog)))))))))))))))))))))))))))))))
