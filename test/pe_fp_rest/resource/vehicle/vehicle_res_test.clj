(ns pe-fp-rest.resource.vehicle.vehicle-res-test
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
            [pe-fp-rest.resource.vehicle.vehicle-res :as vehres]
            [pe-fp-rest.resource.vehicle.version.vehicle-res-v001]
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
            [pe-fp-rest.resource.vehicle.apptxn :as vehapptxn]
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
                                           vehicle-uri-template]]))
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
  (ANY vehicle-uri-template
       [user-entid vehicle-entid]
       (vehres/vehicle-res @conn
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
                           (Long. vehicle-entid)
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
              (let [resp-veh-entid-str (rtucore/last-url-part veh-location-str)
                    pct (rucore/parse-media-type (get hdrs "Content-Type"))
                    charset (get rumeta/char-sets (:charset pct))
                    resp-veh (rucore/read-res pct resp-body-stream charset)
                    veh-last-modified-str (get resp-veh "last-modified")]
                (is (not (nil? resp-veh-entid-str)))
                (is (not (nil? resp-veh)))
                (is (not (nil? veh-last-modified-str)))
                (is (= "300Z" (get resp-veh "fpvehicle/name")))
                (let [veh-create-last-modified (Long. veh-last-modified-str)
                      loaded-vehicles (fpcore/vehicles-for-user @conn loaded-user-entid)]
                  (is (= 1 (count loaded-vehicles)))
                  (let [[[_ loaded-veh-300z]] loaded-vehicles]
                    (is (= "300Z" (:fpvehicle/name loaded-veh-300z)))
                    (is (= 19.0 (:fpvehicle/fuel-capacity loaded-veh-300z)))
                    (is (= 93 (:fpvehicle/min-reqd-octane loaded-veh-300z))))
                  ;; Update 1st vehicle
                  (let [vehicle {"fpvehicle/name" "Fairlady Z"
                                 "fpvehicle/fuel-capacity" 21.2
                                 "fpvehicle/min-reqd-octane" 94}
                        req (-> (rtucore/req-w-std-hdrs rumeta/mt-type
                                                        (meta/mt-subtype-vehicle fpmt-subtype-prefix)
                                                        meta/v001
                                                        "UTF-8;q=1,ISO-8859-1;q=0"
                                                        "json"
                                                        "en-US"
                                                        :put
                                                        veh-location-str
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
                    (testing "status code" (is (= 200 (:status resp))))
                    (testing "body of created vehicle"
                      (let [hdrs (:headers resp)
                            resp-body-stream (:body resp)]
                        (is (= "Accept, Accept-Charset, Accept-Language" (get hdrs "Vary")))
                        (is (not (nil? resp-body-stream)))
                        (let [pct (rucore/parse-media-type (get hdrs "Content-Type"))
                              charset (get rumeta/char-sets (:charset pct))
                              resp-veh (rucore/read-res pct resp-body-stream charset)
                              veh-last-modified-str (get resp-veh "last-modified")]
                          (is (not (nil? resp-veh-entid-str)))
                          (is (not (nil? resp-veh)))
                          (is (not (nil? veh-last-modified-str)))
                          (is (= "Fairlady Z" (get resp-veh "fpvehicle/name")))
                          (let [veh-update-last-modified (Long. veh-last-modified-str)]
                            (is (> veh-update-last-modified veh-create-last-modified))))))
                    (let [loaded-vehicles (fpcore/vehicles-for-user @conn loaded-user-entid)]
                      (is (= 1 (count loaded-vehicles)))
                      (let [[[_ loaded-veh-300z]] loaded-vehicles]
                        (is (= "Fairlady Z" (:fpvehicle/name loaded-veh-300z)))
                        (is (= 21.2 (:fpvehicle/fuel-capacity loaded-veh-300z)))
                        (is (= 94 (:fpvehicle/min-reqd-octane loaded-veh-300z)))))))))))))))
