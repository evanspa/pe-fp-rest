(ns pe-fp-rest.resource.vehicle.vehicle-res
  (:require [liberator.core :refer [defresource]]
            [pe-fp-rest.meta :as meta]
            [clojure.tools.logging :as log]
            [pe-rest-utils.macros :refer [defmulti-by-version]]
            [pe-rest-utils.core :as rucore]
            [pe-rest-utils.meta :as rumeta]
            [pe-user-rest.utils :as userresutils]
            [pe-fp-core.core :as fpcore]
            [pe-fp-core.validation :as fpval]
            [pe-fp-rest.resource.vehicle.apptxn :as vehapptxn]
            [pe-apptxn-restsupport.resource-support :as atressup]))

(declare process-vehicles-put!)
(declare save-vehicle-validator-fn)
(declare body-data-in-transform-fn)
(declare body-data-out-transform-fn)
(declare save-vehicle-fn)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Handler
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn handle-vehicle-put!
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
   vehicle-uri
   user-entid
   vehicle-entid
   embedded-resources-fn
   links-fn]
  (rucore/put-or-post-invoker ctx
                              :put
                              conn
                              partition
                              apptxn-partition
                              hdr-apptxn-id
                              hdr-useragent-device-make
                              hdr-useragent-device-os
                              hdr-useragent-device-os-version
                              base-url
                              entity-uri-prefix
                              vehicle-uri
                              embedded-resources-fn
                              links-fn
                              [user-entid vehicle-entid]
                              save-vehicle-validator-fn
                              fpval/savevehicle-any-issues
                              body-data-in-transform-fn
                              body-data-out-transform-fn
                              nil
                              nil
                              nil
                              save-vehicle-fn
                              nil
                              nil
                              nil
                              vehapptxn/fpapptxn-vehicle-edit
                              vehapptxn/fpapptxnlog-syncvehicle-remote-proc-started
                              vehapptxn/fpapptxnlog-syncvehicle-remote-proc-done-success
                              vehapptxn/fpapptxnlog-syncvehicle-remote-proc-done-err-occurred
                              :fpvehicle/name
                              atressup/apptxn-async-logger
                              atressup/make-apptxn))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Validator function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmulti-by-version save-vehicle-validator-fn meta/v001)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; body-data transformation functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmulti-by-version body-data-in-transform-fn meta/v001)
(defmulti-by-version body-data-out-transform-fn meta/v001)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Save new vehicle function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmulti-by-version save-vehicle-fn meta/v001)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Resource definition
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defresource vehicle-res
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
   vehicle-entid
   embedded-resources-fn
   links-fn]
  :available-media-types (rucore/enumerate-media-types (meta/supported-media-types mt-subtype-prefix))
  :available-charsets rumeta/supported-char-sets
  :available-languages rumeta/supported-languages
  :allowed-methods [:put]
  :authorized? (fn [ctx] (userresutils/authorized? ctx
                                                   conn
                                                   user-entid
                                                   auth-scheme
                                                   auth-scheme-param-name))
  :known-content-type? (rucore/known-content-type-predicate (meta/supported-media-types mt-subtype-prefix))
  :exists? (fn [ctx] (not (nil? (fpcore/vehicle-for-user-by-id conn
                                                               user-entid
                                                               vehicle-entid))))
  :can-put-to-missing? false
  :new? false
  :respond-with-entity? true
  :multiple-representations? false
  :put! (fn [ctx] (handle-vehicle-put! ctx
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
                                       vehicle-entid
                                       embedded-resources-fn
                                       links-fn))
  :handle-ok (fn [ctx] (rucore/handle-resp ctx
                                           hdr-auth-token
                                           hdr-error-mask)))
