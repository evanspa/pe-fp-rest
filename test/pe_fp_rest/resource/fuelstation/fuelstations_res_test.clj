(ns pe-fp-rest.resource.fuelstation.fuelstations-res-test
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
            [pe-fp-rest.resource.fuelstation.fuelstations-res :as fssres]
            [pe-fp-rest.resource.fuelstation.version.fuelstations-res-v001]
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
                                           fuelstations-uri-template]]))
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
  (testing "Successful creation of user, app txn logs and fuelstations."
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
        ;; Create 1st fuelstation
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
          (testing "headers and body of created 300Z fuelstation"
            (let [hdrs (:headers resp)
                  resp-body-stream (:body resp)
                  fs-location-str (get hdrs "location")
                  fs-last-modified-str (get hdrs "last-modified")]
              (is (= "Accept, Accept-Charset, Accept-Language" (get hdrs "Vary")))
              (is (not (nil? resp-body-stream)))
              (is (not (nil? fs-location-str)))
              (is (not (nil? fs-last-modified-str)))
              (let [last-modified (ucore/rfc7231str->instant fs-last-modified-str)
                    resp-fs-entid-str (rtucore/last-url-part fs-location-str)
                    pct (rucore/parse-media-type (get hdrs "Content-Type"))
                    charset (get rumeta/char-sets (:charset pct))
                    resp-fs (rucore/read-res pct resp-body-stream charset)]
                (is (not (nil? last-modified)))
                (is (not (nil? resp-fs-entid-str)))
                (is (not (nil? resp-fs)))
                (is (= "Joe's" (get resp-fs "fpfuelstation/name")))
                (is (= "101 Main Street" (get resp-fs "fpfuelstation/street")))
                (is (= "Charlotte" (get resp-fs "fpfuelstation/city")))
                (is (= "NC" (get resp-fs "fpfuelstation/state")))
                (is (= "28277" (get resp-fs "fpfuelstation/zip")))
                (is (= 80.29103 (get resp-fs "fpfuelstation/longitude")))
                (is (= -13.7002 (get resp-fs "fpfuelstation/latitude")))
                (let [loaded-fuelstations (fpcore/fuelstations-for-user @conn loaded-user-entid)]
                  (is (= 1 (count loaded-fuelstations)))
                  (let [[[loaded-fs-joes-entid loaded-fs-joes]] loaded-fuelstations]
                    (is (= (Long/parseLong resp-fs-entid-str) loaded-fs-joes-entid))
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
                      (testing "headers and body of created Ed's fuel station"
                        (let [hdrs (:headers resp)
                              resp-body-stream (:body resp)
                              fs-location-str (get hdrs "location")
                              fs-last-modified-str (get hdrs "last-modified")]
                          (is (= "Accept, Accept-Charset, Accept-Language" (get hdrs "Vary")))
                          (is (not (nil? resp-body-stream)))
                          (is (not (nil? fs-location-str)))
                          (is (not (nil? fs-last-modified-str)))
                          (let [last-modified (ucore/rfc7231str->instant fs-last-modified-str)
                                resp-fs-entid-str (rtucore/last-url-part fs-location-str)
                                pct (rucore/parse-media-type (get hdrs "Content-Type"))
                                charset (get rumeta/char-sets (:charset pct))
                                resp-fs (rucore/read-res pct resp-body-stream charset)]
                            (is (not (nil? last-modified)))
                            (is (not (nil? resp-fs-entid-str)))
                            (is (not (nil? resp-fs)))
                            (is (= "Ed's" (get resp-fs "fpfuelstation/name")))
                            (is (= "103 Main Street" (get resp-fs "fpfuelstation/street")))
                            (is (= "Providence" (get resp-fs "fpfuelstation/city")))
                            (is (= "NC" (get resp-fs "fpfuelstation/state")))
                            (is (= "28278" (get resp-fs "fpfuelstation/zip")))
                            (is (= 82.29103 (get resp-fs "fpfuelstation/longitude")))
                            (is (= -14.7002 (get resp-fs "fpfuelstation/latitude")))
                            (let [loaded-fuelstations (sort-by :fpfuelstation/date-added (vec (fpcore/fuelstations-for-user @conn loaded-user-entid)))]
                              (is (= 2 (count loaded-fuelstations)))
                              (let [[_ [loaded-fs-eds-entid loaded-fs-eds]] loaded-fuelstations]
                                (is (= (Long/parseLong resp-fs-entid-str) loaded-fs-eds-entid))
                                (is (= "Ed's" (:fpfuelstation/name loaded-fs-eds)))
                                (is (= "103 Main Street" (:fpfuelstation/street loaded-fs-eds)))
                                (is (= "Providence" (:fpfuelstation/city loaded-fs-eds)))
                                (is (= "NC" (:fpfuelstation/state loaded-fs-eds)))
                                (is (= "28278" (:fpfuelstation/zip loaded-fs-eds)))
                                (is (= 82.29103 (:fpfuelstation/longitude loaded-fs-eds)))
                                (is (= -14.7002 (:fpfuelstation/latitude loaded-fs-eds)))))))))))))))))))
