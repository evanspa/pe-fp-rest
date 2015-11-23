(ns pe-fp-rest.test-utils
  (:require [pe-fp-rest.meta :as meta]
            [compojure.core :refer [ANY]]
            [pe-user-rest.meta :as usermeta]
            [pe-user-core.ddl :as uddl]
            [pe-fp-core.ddl :as fpddl]
            [clojure.java.jdbc :as j]
            [ring.util.codec :refer [url-encode]]
            [clojurewerkz.mailer.core :refer [delivery-mode!]]
            [pe-rest-utils.core :as rucore]
            [pe-jdbc-utils.core :as jcore]
            [pe-rest-utils.meta :as rumeta]
            [pe-user-rest.resource.users-res :as userres]
            [pe-user-rest.resource.version.users-res-v001]))

(def db-name "test_db")

(defn db-spec-fn
  ([]
   (db-spec-fn nil))
  ([db-name]
   (let [subname-prefix "//localhost:5432/"]
     {:classname "org.postgresql.Driver"
      :subprotocol "postgresql"
      :subname (if db-name
                 (str subname-prefix db-name)
                 subname-prefix)
      :user "postgres"})))

(def db-spec-without-db (db-spec-fn nil))
(def db-spec (db-spec-fn db-name))

(def fpmt-subtype-prefix "vnd.fp.")
(def fp-auth-scheme "fp-auth")
(def fp-auth-scheme-param-name "fp-user-token")
(def fphdr-auth-token "fp-rest-auth-token")
(def fphdr-error-mask "fp-rest-error-mask")
(def fphdr-if-unmodified-since "fp-if-unmodified-since")
(def fphdr-if-modified-since "fp-if-modified-since")
(def base-url "")
(def entity-uri-prefix "/testing/")
(def fphdr-establish-session "fp-establish-session")

(def err-notification-mustache-template "email/templates/err-notification.html.mustache")
(def err-subject "Error!")
(def err-from-email "errors@example.com")
(def err-to-email "evansp2@gmail.com")

(def welcome-and-verification-email-mustache-template "email/templates/welcome-and-account-verification.html.mustache")
(def verification-email-mustache-template "email/templates/account-verification.html.mustache")
(def welcome-and-verification-email-subject-line "welcome and account verification")
(def welcome-and-verification-email-from "welcome@example.com")

(def new-user-notification-mustache-template "email/templates/new-signup-notification.html.mustache")
(def new-user-notification-from-email "alerts@example.com")
(def new-user-notification-to-email "evansp2@gmail.com")
(def new-user-notification-subject "New sign-up!")

(defn verification-url-maker
  [email verification-token]
  (url-encode (str base-url
                   entity-uri-prefix
                   usermeta/pathcomp-users
                   email
                   "/"
                   usermeta/pathcomp-verification
                   "/"
                   verification-token)))

(defn verification-flagged-url-maker
  [email verification-token]
  (url-encode (str base-url
                   entity-uri-prefix
                   usermeta/pathcomp-users
                   email
                   "/"
                   usermeta/pathcomp-verification-flagged
                   "/"
                   verification-token)))

(def verification-success-mustache-template "web/templates/verification-success.html.mustache")
(def verification-error-mustache-template "web/templates/verification-error.html.mustache")

(delivery-mode! :test)

(defn fixture-maker
  []
  (fn [f]
    ;; Database setup
    (jcore/drop-database db-spec-without-db db-name)
    (jcore/create-database db-spec-without-db db-name)

    ;; User / auth-token setup
    (j/db-do-commands db-spec
                      true
                      uddl/schema-version-ddl
                      uddl/v0-create-user-account-ddl
                      uddl/v0-add-unique-constraint-user-account-email
                      uddl/v0-add-unique-constraint-user-account-username
                      uddl/v0-create-authentication-token-ddl
                      uddl/v1-user-add-deleted-reason-col
                      uddl/v1-user-add-suspended-at-col
                      uddl/v1-user-add-suspended-reason-col
                      uddl/v1-user-add-suspended-count-col
                      uddl/v2-create-email-verification-token-ddl
                      uddl/v3-create-password-reset-token-ddl)
    (jcore/with-try-catch-exec-as-query db-spec
      (uddl/v0-create-updated-count-inc-trigger-fn db-spec))
    (jcore/with-try-catch-exec-as-query db-spec
      (uddl/v0-create-user-account-updated-count-trigger-fn db-spec))
    (jcore/with-try-catch-exec-as-query db-spec
      (uddl/v1-create-suspended-count-inc-trigger-fn db-spec))
    (jcore/with-try-catch-exec-as-query db-spec
      (uddl/v1-create-user-account-suspended-count-trigger-fn db-spec))

    ;; Vehicle setup
    (j/db-do-commands db-spec
                      true
                      fpddl/v0-create-vehicle-ddl
                      fpddl/v0-add-unique-constraint-vehicle-name
                      fpddl/v1-vehicle-add-fuel-capacity-col
                      fpddl/v2-vehicle-drop-erroneous-unique-name-constraint
                      fpddl/v2-vehicle-add-proper-unique-name-constraint)
    (jcore/with-try-catch-exec-as-query db-spec
      (fpddl/v0-create-vehicle-updated-count-inc-trigger-fn db-spec))
    (jcore/with-try-catch-exec-as-query db-spec
      (fpddl/v0-create-vehicle-updated-count-trigger-fn db-spec))

    ;; Fuelstation setup
    (j/db-do-commands db-spec
                      true
                      fpddl/v0-create-fuelstation-ddl
                      fpddl/v0-create-index-on-fuelstation-name)
    (jcore/with-try-catch-exec-as-query db-spec
      (fpddl/v0-create-fuelstation-updated-count-inc-trigger-fn db-spec))
    (jcore/with-try-catch-exec-as-query db-spec
      (fpddl/v0-create-fuelstation-updated-count-trigger-fn db-spec))

    ;; Fuel purchase log setup
    (j/db-do-commands db-spec
                      true
                      fpddl/v0-create-fplog-ddl)
    (jcore/with-try-catch-exec-as-query db-spec
      (fpddl/v0-create-fplog-updated-count-inc-trigger-fn db-spec))
    (jcore/with-try-catch-exec-as-query db-spec
      (fpddl/v0-create-fplog-updated-count-trigger-fn db-spec))

    ;; Environment log setup
    (j/db-do-commands db-spec
                      true
                      fpddl/v0-create-envlog-ddl)
    (jcore/with-try-catch-exec-as-query db-spec
      (fpddl/v0-create-envlog-updated-count-inc-trigger-fn db-spec))
    (jcore/with-try-catch-exec-as-query db-spec
      (fpddl/v0-create-envlog-updated-count-trigger-fn db-spec))
    (f)))

(def users-uri-template
  (format "%s%s%s"
          base-url
          entity-uri-prefix
          usermeta/pathcomp-users))

(def vehicles-uri-template
  (format "%s%s%s/:user-id/%s"
          base-url
          entity-uri-prefix
          usermeta/pathcomp-users
          meta/pathcomp-vehicles))

(def vehicle-uri-template
  (format "%s%s%s/:user-id/%s/:vehicle-id"
          base-url
          entity-uri-prefix
          usermeta/pathcomp-users
          meta/pathcomp-vehicles))

(def fuelstations-uri-template
  (format "%s%s%s/:user-id/%s"
          base-url
          entity-uri-prefix
          usermeta/pathcomp-users
          meta/pathcomp-fuelstations))

(def fuelstation-uri-template
  (format "%s%s%s/:user-id/%s/:fuelstation-id"
          base-url
          entity-uri-prefix
          usermeta/pathcomp-users
          meta/pathcomp-fuelstations))

(def envlogs-uri-template
  (format "%s%s%s/:user-id/%s"
          base-url
          entity-uri-prefix
          usermeta/pathcomp-users
          meta/pathcomp-environment-logs))

(def envlog-uri-template
  (format "%s%s%s/:user-id/%s/:envlog-id"
          base-url
          entity-uri-prefix
          usermeta/pathcomp-users
          meta/pathcomp-environment-logs))

(def fplogs-uri-template
  (format "%s%s%s/:user-id/%s"
          base-url
          entity-uri-prefix
          usermeta/pathcomp-users
          meta/pathcomp-fuelpurchase-logs))

(def fplog-uri-template
  (format "%s%s%s/:user-id/%s/:fplog-id"
          base-url
          entity-uri-prefix
          usermeta/pathcomp-users
          meta/pathcomp-fuelpurchase-logs))

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

(def users-route
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
                          welcome-and-verification-email-mustache-template
                          welcome-and-verification-email-subject-line
                          welcome-and-verification-email-from
                          verification-url-maker
                          verification-flagged-url-maker
                          new-user-notification-mustache-template
                          new-user-notification-from-email
                          new-user-notification-to-email
                          new-user-notification-subject
                          err-notification-mustache-template
                          err-subject
                          err-from-email
                          err-to-email)))
