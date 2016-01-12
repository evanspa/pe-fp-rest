(ns pe-fp-rest.resource.price-stream.price-stream-res-test
  (:require [clojure.test :refer :all]
            [clojure.data.json :as json]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [clojure.tools.logging :as log]
            [compojure.core :refer [defroutes ANY]]
            [ring.middleware.cookies :refer [wrap-cookies]]
            [compojure.handler :as handler]
            [ring.mock.request :as mock]
            [pe-fp-rest.resource.vehicle.vehicles-res :as vehsres]
            [pe-fp-rest.resource.vehicle.version.vehicles-res-v001]
            [pe-fp-rest.resource.fuelstation.fuelstations-res :as fssres]
            [pe-fp-rest.resource.fuelstation.version.fuelstations-res-v001]
            [pe-fp-rest.resource.fplog.fplogs-res :as fplogsres]
            [pe-fp-rest.resource.fplog.version.fplogs-res-v001]
            [pe-fp-rest.resource.fplog.fplog-res :as fplogres]
            [pe-fp-rest.resource.fplog.version.fplog-res-v001]
            [pe-fp-rest.resource.price-stream.price-stream-res :as pricestreamres]
            [pe-fp-rest.resource.price-stream.version.price-stream-res-v001]
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
                                           fphdr-if-unmodified-since
                                           fphdr-if-modified-since
                                           users-uri-template
                                           vehicles-uri-template
                                           fuelstations-uri-template
                                           fplogs-uri-template
                                           fplog-uri-template
                                           price-stream-uri-template
                                           db-spec
                                           fixture-maker
                                           users-route
                                           empty-embedded-resources-fn
                                           empty-links-fn
                                           err-notification-mustache-template
                                           err-subject
                                           err-from-email
                                           err-to-email]]))

(defroutes routes
  users-route
  (ANY price-stream-uri-template
       []
       (pricestreamres/price-stream-res db-spec
                                        fpmt-subtype-prefix
                                        fphdr-auth-token
                                        fphdr-error-mask
                                        base-url
                                        entity-uri-prefix
                                        err-notification-mustache-template
                                        err-subject
                                        err-from-email
                                        err-to-email))
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
                             empty-links-fn
                             err-notification-mustache-template
                             err-subject
                             err-from-email
                             err-to-email))
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
                                empty-links-fn
                                err-notification-mustache-template
                                err-subject
                                err-from-email
                                err-to-email))
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
                             empty-links-fn
                             err-notification-mustache-template
                             err-subject
                             err-from-email
                             err-to-email))
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
                           empty-links-fn
                           fphdr-if-unmodified-since
                           fphdr-if-modified-since
                           err-notification-mustache-template
                           err-subject
                           err-from-email
                           err-to-email)))

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
  (testing "Fetch price event stream"
    (let [;; Create User
          user {"user/name" "Karen Smith"
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
          resp (app req)
          hdrs (:headers resp)
          user-location-str (get hdrs "location")
          resp-user-id-str (rtucore/last-url-part user-location-str)
          auth-token (get hdrs fphdr-auth-token)
          ;; Create Vehicle
          vehicle {"fpvehicle/name" "Mazda CX-9"
                   "fpvehicle/default-octane" 87}
          req (-> (rtucore/req-w-std-hdrs rumeta/mt-type (meta/mt-subtype-vehicle fpmt-subtype-prefix)
                                          meta/v001 "UTF-8;q=1,ISO-8859-1;q=0" "json" "en-US" :post
                                          (str base-url entity-uri-prefix usermeta/pathcomp-users
                                               "/" resp-user-id-str "/" meta/pathcomp-vehicles))
                  (mock/body (json/write-str vehicle))
                  (mock/content-type (rucore/content-type rumeta/mt-type (meta/mt-subtype-vehicle fpmt-subtype-prefix)
                                                          meta/v001 "json" "UTF-8"))
                  (rtucore/header "Authorization" (rtucore/authorization-req-hdr-val fp-auth-scheme
                                                                                     fp-auth-scheme-param-name
                                                                                     auth-token)))
          resp (app req)
          veh-300zx-location-str (get (:headers resp) "location")
          ;; Create Sacramento, CA fuel station
          fuelstation {"fpfuelstation/name" "Joe's"
                       "fpfuelstation/street" "101 Main Street"
                       "fpfuelstation/city" "Charlotte"
                       "fpfuelstation/state" "NC"
                       "fpfuelstation/zip" "28277"
                       "fpfuelstation/longitude" -121.4944
                       "fpfuelstation/latitude" 38.581572}
          fuelstations-uri (str base-url entity-uri-prefix usermeta/pathcomp-users "/"
                                resp-user-id-str "/" meta/pathcomp-fuelstations)
          req (-> (rtucore/req-w-std-hdrs rumeta/mt-type (meta/mt-subtype-fuelstation fpmt-subtype-prefix)
                                          meta/v001 "UTF-8;q=1,ISO-8859-1;q=0" "json" "en-US" :post fuelstations-uri)
                  (mock/body (json/write-str fuelstation))
                  (mock/content-type (rucore/content-type rumeta/mt-type
                                                          (meta/mt-subtype-fuelstation fpmt-subtype-prefix)
                                                          meta/v001 "json" "UTF-8"))
                  (rtucore/header "Authorization" (rtucore/authorization-req-hdr-val fp-auth-scheme
                                                                                     fp-auth-scheme-param-name
                                                                                     auth-token)))
          resp (app req)
          hdrs (:headers resp)
          fs-ca-location-str (get hdrs "location")
          ;; Create Albany, NY fuel station
          fuelstation {"fpfuelstation/name" "Andy's"
                       "fpfuelstation/street" "101 Main Street"
                       "fpfuelstation/city" "Albany"
                       "fpfuelstation/state" "NY"
                       "fpfuelstation/zip" "12207"
                       "fpfuelstation/longitude" -73.756232
                       "fpfuelstation/latitude" 42.652579}
          fuelstations-uri (str base-url entity-uri-prefix usermeta/pathcomp-users "/"
                                resp-user-id-str "/" meta/pathcomp-fuelstations)
          req (-> (rtucore/req-w-std-hdrs rumeta/mt-type (meta/mt-subtype-fuelstation fpmt-subtype-prefix)
                                          meta/v001 "UTF-8;q=1,ISO-8859-1;q=0" "json" "en-US" :post fuelstations-uri)
                  (mock/body (json/write-str fuelstation))
                  (mock/content-type (rucore/content-type rumeta/mt-type
                                                          (meta/mt-subtype-fuelstation fpmt-subtype-prefix)
                                                          meta/v001 "json" "UTF-8"))
                  (rtucore/header "Authorization" (rtucore/authorization-req-hdr-val fp-auth-scheme
                                                                                     fp-auth-scheme-param-name
                                                                                     auth-token)))
          resp (app req)
          hdrs (:headers resp)
          fs-ny-location-str (get hdrs "location")
          ;; Create Houston, TX fuelstation
          fuelstation {"fpfuelstation/name" "Ed's"
                       "fpfuelstation/street" "103 Main Street"
                       "fpfuelstation/city" "Providence"
                       "fpfuelstation/state" "NC"
                       "fpfuelstation/zip" "28278"
                       "fpfuelstation/longitude" -95.369803
                       "fpfuelstation/latitude" 29.760}
          req (-> (rtucore/req-w-std-hdrs rumeta/mt-type (meta/mt-subtype-fuelstation fpmt-subtype-prefix)
                                          meta/v001 "UTF-8;q=1,ISO-8859-1;q=0" "json" "en-US" :post
                                          (str base-url entity-uri-prefix usermeta/pathcomp-users "/"
                                               resp-user-id-str "/" meta/pathcomp-fuelstations))
                  (mock/body (json/write-str fuelstation))
                  (mock/content-type (rucore/content-type rumeta/mt-type
                                                          (meta/mt-subtype-fuelstation fpmt-subtype-prefix)
                                                          meta/v001 "json" "UTF-8"))
                  (rtucore/header "Authorization" (rtucore/authorization-req-hdr-val fp-auth-scheme
                                                                                     fp-auth-scheme-param-name
                                                                                     auth-token)))
          resp (app req)
          hdrs (:headers resp)
          fs-tx-location-str (get hdrs "location")]
      (letfn [(new-fplog [fs purchased-at gallon-price octane]
                (let [fplog {"fplog/vehicle" veh-300zx-location-str
                             "fplog/fuelstation" fs
                             "fplog/purchased-at" (c/to-long purchased-at)
                             "fplog/got-car-wash" true
                             "fplog/car-wash-per-gal-discount" 0.08
                             "fplog/num-gallons" 14.3
                             "fplog/octane" octane
                             "fplog/is-diesel" false
                             "fplog/odometer" 15518
                             "fplog/gallon-price" gallon-price}
                      fplogs-uri (str base-url entity-uri-prefix usermeta/pathcomp-users "/" resp-user-id-str "/" meta/pathcomp-fuelpurchase-logs)
                      req (-> (rtucore/req-w-std-hdrs rumeta/mt-type (meta/mt-subtype-fplog fpmt-subtype-prefix)
                                                      meta/v001 "UTF-8;q=1,ISO-8859-1;q=0" "json" "en-US" :post fplogs-uri)
                              (mock/body (json/write-str fplog))
                              (mock/content-type (rucore/content-type rumeta/mt-type (meta/mt-subtype-fplog fpmt-subtype-prefix)
                                                                      meta/v001 "json" "UTF-8"))
                              (rtucore/header "Authorization" (rtucore/authorization-req-hdr-val fp-auth-scheme
                                                                                                 fp-auth-scheme-param-name
                                                                                                 auth-token)))]
                  (app req)))]
        (let [t1 (t/now)]
          (new-fplog fs-ca-location-str t1 4.99 87)
          (new-fplog fs-tx-location-str t1 2.19 87)
          (new-fplog fs-ny-location-str t1 3.39 87))
        (let [price-stream-filter {"price-stream-filter/fs-latitude" 42.814
                                   "price-stream-filter/fs-longitude" -73.939
                                   "price-stream-filter/fs-distance-within" 10000000
                                   "price-stream-filter/max-results" 20
                                   "price-stream-filter/sort-by" [["f.gallon_price" "asc"] ["distance" "asc"] ["f.purchased_at" "desc"]]}
              price-stream-uri (str base-url entity-uri-prefix meta/pathcomp-price-stream)
              req (-> (rtucore/req-w-std-hdrs rumeta/mt-type (meta/mt-subtype-price-stream fpmt-subtype-prefix)
                                              meta/v001 "UTF-8;q=1,ISO-8859-1;q=0" "json" "en-US" :post price-stream-uri)
                      (mock/body (json/write-str price-stream-filter))
                      (mock/content-type (rucore/content-type rumeta/mt-type (meta/mt-subtype-price-stream fpmt-subtype-prefix)
                                                              meta/v001 "json" "UTF-8")))
              resp (app req)]
          (testing "status code" (is (= 200 (:status resp))))
          (let [hdrs (:headers resp)
                resp-body-stream (:body resp)
                pct (rucore/parse-media-type (get hdrs "Content-Type"))
                charset (get rumeta/char-sets (:charset pct))
                price-stream (rucore/read-res pct resp-body-stream charset)]
            (is (not (nil? price-stream)))
            (let [price-events (get price-stream "price-event-stream")]
              (is (= 3 (count price-events)))
              (let [[event-1 event-2 event-3] price-events]
                (is (= 2.19 (get event-1 "price-event/price")))
                (is (= 87 (get event-1 "price-event/octane")))
                (is (= false (get event-1 "price-event/is-diesel")))
                (is (= 29.760 (get event-1 "price-event/fs-latitude")))
                (is (= -95.369803 (get event-1 "price-event/fs-longitude")))

                (is (= 3.39 (get event-2 "price-event/price")))
                (is (= 4.99 (get event-3 "price-event/price")))))))))))
