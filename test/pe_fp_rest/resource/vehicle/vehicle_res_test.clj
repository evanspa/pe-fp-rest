(ns pe-fp-rest.resource.vehicle.vehicle-res-test
  (:require [clojure.test :refer :all]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [compojure.core :refer [defroutes ANY]]
            [ring.middleware.cookies :refer [wrap-cookies]]
            [compojure.handler :as handler]
            [ring.mock.request :as mock]
            [pe-user-rest.resource.users-res :as userres]
            [pe-user-rest.resource.version.users-res-v001]
            [pe-fp-rest.resource.vehicle.vehicles-res :as vehsres]
            [pe-fp-rest.resource.vehicle.version.vehicles-res-v001]
            [pe-fp-rest.resource.vehicle.vehicle-res :as vehres]
            [pe-fp-rest.resource.vehicle.version.vehicle-res-v001]
            [pe-fp-rest.meta :as meta]
            [pe-user-core.ddl :as uddl]
            [pe-fp-core.ddl :as fpddl]
            [pe-jdbc-utils.core :as jcore]
            [pe-fp-core.core :as fpcore]
            [pe-fp-core.validation :as fpval]
            [pe-rest-testutils.core :as rtucore]
            [pe-core-utils.core :as ucore]
            [pe-rest-utils.core :as rucore]
            [pe-rest-utils.meta :as rumeta]
            [pe-user-core.core :as usercore]
            [pe-user-rest.meta :as usermeta]
            [pe-fp-rest.test-utils :refer [fpmt-subtype-prefix
                                           fp-auth-scheme
                                           fp-auth-scheme-param-name
                                           base-url
                                           fphdr-auth-token
                                           fphdr-error-mask
                                           fphdr-establish-session
                                           entity-uri-prefix
                                           users-uri-template
                                           vehicles-uri-template
                                           vehicle-uri-template
                                           db-spec
                                           fixture-maker]]))

(defn empty-embedded-resources-fn
  [version
   base-url
   entity-uri-prefix
   entity-uri
   db-spec
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
  (ANY vehicle-uri-template
       [user-id vehicle-id]
       (vehres/vehicle-res db-spec
                           fpmt-subtype-prefix
                           fphdr-auth-token
                           fphdr-error-mask
                           fp-auth-scheme
                           fp-auth-scheme-param-name
                           base-url
                           entity-uri-prefix
                           (Long. user-id)
                           (Long. vehicle-id)
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
            resp-body-stream (:body resp)
            user-location-str (get hdrs "location")
            resp-user-id-str (rtucore/last-url-part user-location-str)
            pct (rucore/parse-media-type (get hdrs "Content-Type"))
            charset (get rumeta/char-sets (:charset pct))
            resp-user (rucore/read-res pct resp-body-stream charset)
            auth-token (get hdrs fphdr-auth-token)
            [loaded-user-id loaded-user] (usercore/load-user-by-authtoken db-spec
                                                                          (Long. resp-user-id-str)
                                                                          auth-token)]
        ;; Create 1st vehicle
        (let [vehicle {"fpvehicle/name" "Jeep"
                       "fpvehicle/default-octane" 87}
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
              resp (app req)
              hdrs (:headers resp)
              veh-location-str (get hdrs "location")]
          (testing "status code" (is (= 201 (:status resp)))))

        ;; Create 2nd vehicle
        (is (= 1 (count (fpcore/vehicles-for-user db-spec loaded-user-id))))
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
              resp (app req)
              hdrs (:headers resp)
              veh-location-str (get hdrs "location")]
          (testing "status code" (is (= 201 (:status resp))))
          (testing "body of created vehicle"
            (let [hdrs (:headers resp)
              resp-body-stream (:body resp)
              veh-location-str (get hdrs "location")]
              (is (= "Accept, Accept-Charset, Accept-Language" (get hdrs "Vary")))
              (is (not (nil? resp-body-stream)))
              (is (not (nil? veh-location-str)))
              (let [resp-veh-id-str (rtucore/last-url-part veh-location-str)
                    pct (rucore/parse-media-type (get hdrs "Content-Type"))
                    charset (get rumeta/char-sets (:charset pct))
                    resp-veh (rucore/read-res pct resp-body-stream charset)]
                (is (not (nil? resp-veh-id-str)))
                (is (not (nil? resp-veh)))
                (is (= "300Z" (get resp-veh "fpvehicle/name")))
                (let [loaded-vehicles (fpcore/vehicles-for-user db-spec loaded-user-id)]
                  (is (= 2 (count loaded-vehicles)))
                  (let [[[_ loaded-veh-300z]] loaded-vehicles]
                    (is (= "300Z" (:fpvehicle/name loaded-veh-300z)))
                    (is (= 93 (:fpvehicle/default-octane loaded-veh-300z))))
                  ;; Update 2nd vehicle
                  (let [vehicle {"fpvehicle/name" "Fairlady Z"
                                 "fpvehicle/updated-at" (c/to-long (t/now))
                                 "fpvehicle/default-octane" 94
                                 "fpvehicle/fuel-capacity" 18.4}
                        req (-> (rtucore/req-w-std-hdrs rumeta/mt-type
                                                        (meta/mt-subtype-vehicle fpmt-subtype-prefix)
                                                        meta/v001
                                                        "UTF-8;q=1,ISO-8859-1;q=0"
                                                        "json"
                                                        "en-US"
                                                        :put
                                                        veh-location-str)
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
                    (testing "status code" (is (= 200 (:status resp))))
                    (testing "body of created vehicle"
                      (let [hdrs (:headers resp)
                            resp-body-stream (:body resp)]
                        (is (= "Accept, Accept-Charset, Accept-Language" (get hdrs "Vary")))
                        (is (not (nil? resp-body-stream)))
                        (let [pct (rucore/parse-media-type (get hdrs "Content-Type"))
                              charset (get rumeta/char-sets (:charset pct))
                              resp-veh (rucore/read-res pct resp-body-stream charset)]
                          (is (not (nil? resp-veh-id-str)))
                          (is (not (nil? resp-veh)))
                          (is (= "Fairlady Z" (get resp-veh "fpvehicle/name")))
                          (is (= 18.4 (get resp-veh "fpvehicle/fuel-capacity"))))))
                    (let [loaded-vehicles (fpcore/vehicles-for-user db-spec loaded-user-id)]
                      (is (= 2 (count loaded-vehicles)))
                      (let [[[_ loaded-veh-300z]] loaded-vehicles]
                        (is (= "Fairlady Z" (:fpvehicle/name loaded-veh-300z)))
                        (is (= 94 (:fpvehicle/default-octane loaded-veh-300z))))))

                  ;; Update the 2nd vehicle again, but giving it a non-unique name
                  (let [vehicle {"fpvehicle/name" "Jeep"}
                        req (-> (rtucore/req-w-std-hdrs rumeta/mt-type
                                                        (meta/mt-subtype-vehicle fpmt-subtype-prefix)
                                                        meta/v001
                                                        "UTF-8;q=1,ISO-8859-1;q=0"
                                                        "json"
                                                        "en-US"
                                                        :put
                                                        veh-location-str)
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
                    (testing "status code" (is (= 422 (:status resp))))
                    (testing "headers and body of created user"
                      (let [hdrs (:headers resp)
                            user-location-str (get hdrs "location")]
                        (is (= "Accept, Accept-Charset, Accept-Language" (get hdrs "Vary")))
                        (is (nil? user-location-str))
                        (let [error-mask-str (get hdrs fphdr-error-mask)]
                          (is (nil? (get hdrs fphdr-auth-token)))
                          (is (not (nil? error-mask-str)))
                          (let [error-mask (Long/parseLong error-mask-str)]
                            (is (pos? (bit-and error-mask fpval/sv-any-issues)))
                            (is (pos? (bit-and error-mask fpval/sv-vehicle-already-exists)))
                            (is (zero? (bit-and error-mask fpval/sv-name-not-provided)))))))))))))))))
