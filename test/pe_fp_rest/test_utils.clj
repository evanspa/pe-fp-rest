(ns pe-fp-rest.test-utils
  (:require [pe-fp-rest.meta :as meta]
            [pe-user-rest.meta :as usermeta]
            [pe-rest-utils.core :as rucore]
            [pe-rest-utils.meta :as rumeta]))

(def fp-schema-files ["fp-schema-updates-0.0.1.dtm"])
(def user-schema-files ["user-schema-updates-0.0.1.dtm"])
(def apptxn-logging-schema-files ["apptxn-logging-schema-updates-0.0.1.dtm"])
(def db-uri "datomic:mem://fp")
(def fp-partition :fp)
(def fpmt-subtype-prefix "vnd.fp.")
(def fp-auth-scheme "fp-auth")
(def fp-auth-scheme-param-name "fp-user-token")
(def fphdr-auth-token "fp-rest-auth-token")
(def fphdr-error-mask "fp-rest-error-mask")
(def fphdr-apptxn-id "fp-apptxn-id")
(def fphdr-useragent-device-make "fp-rest-useragent-device-make")
(def fphdr-useragent-device-os "fp-rest-useragent-device-os")
(def fphdr-useragent-device-os-version "fp-rest-useragent-device-os-version")
(def base-url "")
(def entity-uri-prefix "/testing/")
(def fphdr-establish-session "fp-establish-session")

(def users-uri-template
  (format "%s%s%s"
          base-url
          entity-uri-prefix
          usermeta/pathcomp-users))

(def vehicles-uri-template
  (format "%s%s%s/:user-entid/%s"
          base-url
          entity-uri-prefix
          usermeta/pathcomp-users
          meta/pathcomp-vehicles))

(def vehicle-uri-template
  (format "%s%s%s/:user-entid/%s/:vehicle-entid"
          base-url
          entity-uri-prefix
          usermeta/pathcomp-users
          meta/pathcomp-vehicles))

(def fuelstations-uri-template
  (format "%s%s%s/:user-entid/%s"
          base-url
          entity-uri-prefix
          usermeta/pathcomp-users
          meta/pathcomp-fuelstations))

(def fuelstation-uri-template
  (format "%s%s%s/:user-entid/%s/:fuelstation-entid"
          base-url
          entity-uri-prefix
          usermeta/pathcomp-users
          meta/pathcomp-fuelstations))

(def envlogs-uri-template
  (format "%s%s%s/:user-entid/%s"
          base-url
          entity-uri-prefix
          usermeta/pathcomp-users
          meta/pathcomp-environment-logs))

(def envlog-uri-template
  (format "%s%s%s/:user-entid/%s/:envlog-entid"
          base-url
          entity-uri-prefix
          usermeta/pathcomp-users
          meta/pathcomp-environment-logs))

(def fplogs-uri-template
  (format "%s%s%s/:user-entid/%s"
          base-url
          entity-uri-prefix
          usermeta/pathcomp-users
          meta/pathcomp-fuelpurchase-logs))

(def fplog-uri-template
  (format "%s%s%s/:user-entid/%s/:fplog-entid"
          base-url
          entity-uri-prefix
          usermeta/pathcomp-users
          meta/pathcomp-fuelpurchase-logs))
