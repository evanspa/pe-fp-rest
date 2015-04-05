(ns pe-fp-rest.resource.envlog.envlog-res
  (:require [liberator.core :refer [defresource]]
            [pe-fp-rest.meta :as meta]
            [clojure.tools.logging :as log]
            [pe-rest-utils.macros :refer [defmulti-by-version]]
            [pe-rest-utils.core :as rucore]
            [pe-rest-utils.meta :as rumeta]
            [pe-user-rest.utils :as userresutils]
            [pe-fp-core.core :as fpcore]
            [pe-fp-core.validation :as fpval]
            [pe-fp-rest.resource.envlog.apptxn :as envlogapptxn]
            [pe-apptxn-restsupport.resource-support :as atressup]))

(declare process-envlogs-put!)
(declare save-envlog-validator-fn)
(declare body-data-in-transform-fn)
(declare body-data-out-transform-fn)
(declare save-envlog-fn)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Handler
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn handle-envlog-put!
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
   envlog-uri
   user-entid
   envlog-entid
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
                              envlog-uri
                              embedded-resources-fn
                              links-fn
                              [user-entid envlog-entid]
                              save-envlog-validator-fn
                              fpval/saveenvironmentlog-any-issues
                              body-data-in-transform-fn
                              body-data-out-transform-fn
                              nil
                              nil
                              nil
                              save-envlog-fn
                              nil
                              nil
                              nil
                              envlogapptxn/fpapptxn-envlog-edit
                              envlogapptxn/fpapptxnlog-syncenvlog-remote-proc-started
                              envlogapptxn/fpapptxnlog-syncenvlog-remote-proc-done-success
                              envlogapptxn/fpapptxnlog-syncenvlog-remote-proc-done-err-occurred
                              :fpenvironmentlog/log-date
                              atressup/apptxn-async-logger
                              atressup/make-apptxn))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Validator function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmulti-by-version save-envlog-validator-fn meta/v001)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; body-data transformation functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmulti-by-version body-data-in-transform-fn meta/v001)
(defmulti-by-version body-data-out-transform-fn meta/v001)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Save new envlog function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmulti-by-version save-envlog-fn meta/v001)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Resource definition
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defresource envlog-res
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
   envlog-entid
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
  :exists? (fn [ctx] (not (nil? (fpcore/envlog-for-user-by-id conn
                                                              user-entid
                                                              envlog-entid))))
  :can-put-to-missing? false
  :new? false
  :respond-with-entity? true
  :multiple-representations? false
  :put! (fn [ctx] (handle-envlog-put! ctx
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
                                      envlog-entid
                                      embedded-resources-fn
                                      links-fn))
  :handle-ok (fn [ctx] (rucore/handle-resp ctx
                                           hdr-auth-token
                                           hdr-error-mask)))
