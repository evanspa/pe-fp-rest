(ns pe-fp-rest.resource.fuelstation.fuelstation-res-test
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
            [pe-fp-rest.resource.fuelstation.fuelstations-res :as fssres]
            [pe-fp-rest.resource.fuelstation.version.fuelstations-res-v001]
            [pe-fp-rest.resource.fuelstation.fuelstation-res :as fsres]
            [pe-fp-rest.resource.fuelstation.version.fuelstation-res-v001]
            [pe-fp-rest.meta :as meta]
            [pe-user-core.ddl :as uddl]
            [pe-fp-core.ddl :as fpddl]
            [pe-jdbc-utils.core :as jcore]
            [pe-fp-core.core :as fpcore]
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
                                           fphdr-if-unmodified-since
                                           fphdr-if-modified-since
                                           entity-uri-prefix
                                           users-uri-template
                                           fuelstations-uri-template
                                           fuelstation-uri-template
                                           db-spec
                                           fixture-maker
                                           verification-email-mustache-template
                                           verification-email-subject-line
                                           verification-email-from
                                           verification-url-maker
                                           flagged-url-maker]]))
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
                          empty-links-fn
                          verification-email-mustache-template
                          verification-email-subject-line
                          verification-email-from
                          verification-url-maker
                          flagged-url-maker))
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
  (ANY fuelstation-uri-template
       [user-id fuelstation-id]
       (fsres/fuelstation-res db-spec
                              fpmt-subtype-prefix
                              fphdr-auth-token
                              fphdr-error-mask
                              fp-auth-scheme
                              fp-auth-scheme-param-name
                              base-url
                              entity-uri-prefix
                              (Long. user-id)
                              (Long. fuelstation-id)
                              empty-embedded-resources-fn
                              empty-links-fn
                              fphdr-if-unmodified-since
                              fphdr-if-modified-since)))

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
  (testing "Successful create, update and delete of fuelstation"
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
            [loaded-user-id loaded-user-ent] (usercore/load-user-by-authtoken db-spec
                                                                              (Long. resp-user-id-str)
                                                                              auth-token)]
        ;; Create 1st fuelstation
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
          (testing "headers and body of created fuelstation"
            (let [hdrs (:headers resp)
                  fs-location-str (get hdrs "location")]
              (let [loaded-fuelstations (fpcore/fuelstations-for-user db-spec loaded-user-id)]
                (is (= 1 (count loaded-fuelstations)))
                (let [[[_ loaded-fs-joes]] loaded-fuelstations]
                  (is (= "Joe's" (:fpfuelstation/name loaded-fs-joes)))
                  (is (= "101 Main Street" (:fpfuelstation/street loaded-fs-joes)))
                  (is (= "Charlotte" (:fpfuelstation/city loaded-fs-joes)))
                  (is (= "NC" (:fpfuelstation/state loaded-fs-joes)))
                  (is (= "28277" (:fpfuelstation/zip loaded-fs-joes)))
                  (is (= 80.29103 (:fpfuelstation/longitude loaded-fs-joes)))
                  (is (= -13.7002 (:fpfuelstation/latitude loaded-fs-joes)))
                  ;; Update 1st fuel station
                  (let [fuelstation {"fpfuelstation/name" "Joseph's"
                                     "fpfuelstation/updated-at" (c/to-long (t/now))
                                     "fpfuelstation/street" "103 Main Drive"
                                     "fpfuelstation/city" "Schenectady"
                                     "fpfuelstation/state" "NY"
                                     "fpfuelstation/zip" "12309"
                                     "fpfuelstation/longitude" 82.29103
                                     "fpfuelstation/latitude" -15.7002}
                        req (-> (rtucore/req-w-std-hdrs rumeta/mt-type
                                                        (meta/mt-subtype-fuelstation fpmt-subtype-prefix)
                                                        meta/v001
                                                        "UTF-8;q=1,ISO-8859-1;q=0"
                                                        "json"
                                                        "en-US"
                                                        :put
                                                        fs-location-str)
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
                    (testing "status code" (is (= 200 (:status resp))))
                    (testing "headers and body of created fuelstation"
                      (let [hdrs (:headers resp)
                            fs-location-str (get hdrs "location")]
                        (let [loaded-fuelstations (fpcore/fuelstations-for-user db-spec loaded-user-id)]
                          (is (= 1 (count loaded-fuelstations)))
                          (let [[[_ loaded-fs-joes]] loaded-fuelstations]
                            (is (= "Joseph's" (:fpfuelstation/name loaded-fs-joes)))
                            (is (= "103 Main Drive" (:fpfuelstation/street loaded-fs-joes)))
                            (is (= "Schenectady" (:fpfuelstation/city loaded-fs-joes)))
                            (is (= "NY" (:fpfuelstation/state loaded-fs-joes)))
                            (is (= "12309" (:fpfuelstation/zip loaded-fs-joes)))
                            (is (= 82.29103 (:fpfuelstation/longitude loaded-fs-joes)))
                            (is (= -15.7002 (:fpfuelstation/latitude loaded-fs-joes)))))))))))))))))
