(ns pe-fp-rest.resource.fuelstation.fuelstations-res-test
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
                                           verification-flagged-url-maker]]))

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
                          verification-flagged-url-maker))
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
  (testing "Successful creation of user, app txn logs and fuelstations."
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
          (testing "headers and body of created 300Z fuelstation"
            (let [hdrs (:headers resp)
                  resp-body-stream (:body resp)
                  fs-location-str (get hdrs "location")]
              (is (= "Accept, Accept-Charset, Accept-Language" (get hdrs "Vary")))
              (is (not (nil? resp-body-stream)))
              (is (not (nil? fs-location-str)))
              (let [resp-fs-id-str (rtucore/last-url-part fs-location-str)
                    pct (rucore/parse-media-type (get hdrs "Content-Type"))
                    charset (get rumeta/char-sets (:charset pct))
                    resp-fs (rucore/read-res pct resp-body-stream charset)]
                (is (not (nil? resp-fs-id-str)))
                (is (not (nil? resp-fs)))
                (is (not (nil? (get resp-fs "fpfuelstation/created-at"))))
                (is (not (nil? (get resp-fs "fpfuelstation/updated-at"))))
                (is (= "Joe's" (get resp-fs "fpfuelstation/name")))
                (is (= "101 Main Street" (get resp-fs "fpfuelstation/street")))
                (is (= "Charlotte" (get resp-fs "fpfuelstation/city")))
                (is (= "NC" (get resp-fs "fpfuelstation/state")))
                (is (= "28277" (get resp-fs "fpfuelstation/zip")))
                (is (= 80.29103 (get resp-fs "fpfuelstation/longitude")))
                (is (= -13.7002 (get resp-fs "fpfuelstation/latitude")))
                (let [loaded-fuelstations (fpcore/fuelstations-for-user db-spec loaded-user-id)]
                  (is (= 1 (count loaded-fuelstations)))
                  (let [[[loaded-fs-joes-id loaded-fs-joes]] loaded-fuelstations]
                    (is (= (Long/parseLong resp-fs-id-str) loaded-fs-joes-id))
                    (is (= "Joe's" (:fpfuelstation/name loaded-fs-joes)))
                    (is (= "101 Main Street" (:fpfuelstation/street loaded-fs-joes)))
                    (is (= "Charlotte" (:fpfuelstation/city loaded-fs-joes)))
                    (is (= "NC" (:fpfuelstation/state loaded-fs-joes)))
                    (is (= "28277" (:fpfuelstation/zip loaded-fs-joes)))
                    (is (= 80.29103 (:fpfuelstation/longitude loaded-fs-joes)))
                    (is (= -13.7002 (:fpfuelstation/latitude loaded-fs-joes)))

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
                      (testing "headers and body of created Ed's fuel station"
                        (let [hdrs (:headers resp)
                              resp-body-stream (:body resp)
                              fs-location-str (get hdrs "location")]
                          (is (= "Accept, Accept-Charset, Accept-Language" (get hdrs "Vary")))
                          (is (not (nil? resp-body-stream)))
                          (is (not (nil? fs-location-str)))
                          (let [resp-fs-id-str (rtucore/last-url-part fs-location-str)
                                pct (rucore/parse-media-type (get hdrs "Content-Type"))
                                charset (get rumeta/char-sets (:charset pct))
                                resp-fs (rucore/read-res pct resp-body-stream charset)]
                            (is (not (nil? resp-fs-id-str)))
                            (is (not (nil? resp-fs)))
                            (is (= "Ed's" (get resp-fs "fpfuelstation/name")))
                            (is (= "103 Main Street" (get resp-fs "fpfuelstation/street")))
                            (is (= "Providence" (get resp-fs "fpfuelstation/city")))
                            (is (= "NC" (get resp-fs "fpfuelstation/state")))
                            (is (= "28278" (get resp-fs "fpfuelstation/zip")))
                            (is (= 82.29103 (get resp-fs "fpfuelstation/longitude")))
                            (is (= -14.7002 (get resp-fs "fpfuelstation/latitude")))
                            (let [loaded-fuelstations (fpcore/fuelstations-for-user db-spec loaded-user-id)]
                              (is (= 2 (count loaded-fuelstations)))
                              (let [[[loaded-fs-eds-id loaded-fs-eds] _] loaded-fuelstations]
                                (is (= (Long/parseLong resp-fs-id-str) loaded-fs-eds-id))
                                (is (= "Ed's" (:fpfuelstation/name loaded-fs-eds)))
                                (is (= "103 Main Street" (:fpfuelstation/street loaded-fs-eds)))
                                (is (= "Providence" (:fpfuelstation/city loaded-fs-eds)))
                                (is (= "NC" (:fpfuelstation/state loaded-fs-eds)))
                                (is (= "28278" (:fpfuelstation/zip loaded-fs-eds)))
                                (is (= 82.29103 (:fpfuelstation/longitude loaded-fs-eds)))
                                (is (= -14.7002 (:fpfuelstation/latitude loaded-fs-eds)))))))))))))))))))
