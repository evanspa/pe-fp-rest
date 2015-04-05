(ns pe-fp-rest.resource.vehicle.vehicles-res
  (:require [liberator.core :refer [defresource]]
            [pe-fp-rest.meta :as meta]
            [clojure.tools.logging :as log]
            [pe-rest-utils.macros :refer [defmulti-by-version]]
            [pe-rest-utils.core :as rucore]
            [pe-rest-utils.meta :as rumeta]
            [pe-user-rest.utils :as userresutils]
            [pe-fp-core.validation :as fpval]
            [pe-fp-rest.resource.vehicle.apptxn :as vehapptxn]
            [pe-apptxn-restsupport.resource-support :as atressup]))

(declare process-vehicles-post!)
(declare new-vehicle-validator-fn)
(declare body-data-in-transform-fn)
(declare body-data-out-transform-fn)
(declare save-new-vehicle-fn)
(declare extract-name-fn)
(declare get-vehicles-by-name-fn)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Handler
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn handle-vehicles-post!
  [ctx
   conn
   partition
   apptxn-partition
   hdr-apptxn-id
   hdr-useragent-device-make
   hdr-useragent-device-os
   hdr-useragent-device-os-version
   base-url
   entity-uri-prefix
   vehicles-uri
   user-entid
   embedded-resources-fn
   links-fn]
  (rucore/put-or-post-invoker ctx
                              :post-as-create
                              conn
                              partition
                              apptxn-partition
                              hdr-apptxn-id
                              hdr-useragent-device-make
                              hdr-useragent-device-os
                              hdr-useragent-device-os-version
                              base-url
                              entity-uri-prefix
                              vehicles-uri
                              embedded-resources-fn
                              links-fn
                              [user-entid]
                              new-vehicle-validator-fn
                              fpval/savevehicle-any-issues
                              body-data-in-transform-fn
                              body-data-out-transform-fn
                              [[extract-name-fn get-vehicles-by-name-fn]]
                              fpval/savevehicle-vehicle-already-exists
                              save-new-vehicle-fn
                              nil
                              nil
                              nil
                              nil
                              vehapptxn/fpapptxn-vehicle-create
                              vehapptxn/fpapptxnlog-syncvehicle-remote-proc-started
                              vehapptxn/fpapptxnlog-syncvehicle-remote-proc-done-success
                              vehapptxn/fpapptxnlog-syncvehicle-remote-proc-done-err-occurred
                              :fpvehicle/name
                              atressup/apptxn-async-logger
                              atressup/make-apptxn))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Validator function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmulti-by-version new-vehicle-validator-fn meta/v001)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; body-data transformation functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmulti-by-version body-data-in-transform-fn meta/v001)
(defmulti-by-version body-data-out-transform-fn meta/v001)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Name extraction functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmulti-by-version extract-name-fn meta/v001)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Entity lookup-by-name functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmulti-by-version get-vehicles-by-name-fn meta/v001)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Save new vehicle function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmulti-by-version save-new-vehicle-fn meta/v001)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Resource definition
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defresource vehicles-res
  [conn
   partition
   apptxn-partition
   mt-subtype-prefix
   hdr-auth-token
   hdr-error-mask
   auth-scheme
   auth-scheme-param-name
   base-url
   entity-uri-prefix
   hdr-apptxn-id
   hdr-useragent-device-make
   hdr-useragent-device-os
   hdr-useragent-device-os-version
   user-entid
   embedded-resources-fn
   links-fn]
  :available-media-types (rucore/enumerate-media-types (meta/supported-media-types mt-subtype-prefix))
  :available-charsets rumeta/supported-char-sets
  :available-languages rumeta/supported-languages
  :allowed-methods [:post]
  :authorized? (fn [ctx] (userresutils/authorized? ctx
                                                   conn
                                                   user-entid
                                                   auth-scheme
                                                   auth-scheme-param-name))
  :known-content-type? (rucore/known-content-type-predicate (meta/supported-media-types mt-subtype-prefix))
  :post! (fn [ctx] (handle-vehicles-post! ctx
                                          conn
                                          partition
                                          apptxn-partition
                                          hdr-apptxn-id
                                          hdr-useragent-device-make
                                          hdr-useragent-device-os
                                          hdr-useragent-device-os-version
                                          base-url
                                          entity-uri-prefix
                                          (:uri (:request ctx))
                                          user-entid
                                          embedded-resources-fn
                                          links-fn))
  :handle-created (fn [ctx] (rucore/handle-resp ctx
                                                hdr-auth-token
                                                hdr-error-mask)))
