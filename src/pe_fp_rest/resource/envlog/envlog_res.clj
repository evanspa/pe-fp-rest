(ns pe-fp-rest.resource.envlog.envlog-res
  (:require [liberator.core :refer [defresource]]
            [pe-fp-rest.meta :as meta]
            [clojure.tools.logging :as log]
            [pe-rest-utils.macros :refer [defmulti-by-version]]
            [pe-rest-utils.core :as rucore]
            [pe-rest-utils.meta :as rumeta]
            [pe-user-rest.utils :as userresutils]
            [pe-fp-core.core :as fpcore]
            [pe-fp-core.validation :as fpval]))

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
   db-spec
   base-url
   entity-uri-prefix
   envlog-uri
   user-id
   envlog-id
   plaintext-auth-token
   embedded-resources-fn
   links-fn]
  (rucore/put-or-post-invoker ctx
                              :put
                              db-spec
                              base-url
                              entity-uri-prefix
                              envlog-uri
                              embedded-resources-fn
                              links-fn
                              [user-id envlog-id]
                              plaintext-auth-token
                              save-envlog-validator-fn
                              fpval/senvlog-any-issues
                              body-data-in-transform-fn
                              body-data-out-transform-fn
                              nil
                              nil
                              nil
                              save-envlog-fn
                              nil
                              nil
                              nil))

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
  [db-spec
   mt-subtype-prefix
   hdr-auth-token
   hdr-error-mask
   auth-scheme
   auth-scheme-param-name
   base-url
   entity-uri-prefix
   user-id
   envlog-id
   embedded-resources-fn
   links-fn]
  :available-media-types (rucore/enumerate-media-types (meta/supported-media-types mt-subtype-prefix))
  :available-charsets rumeta/supported-char-sets
  :available-languages rumeta/supported-languages
  :allowed-methods [:put]
  :authorized? (fn [ctx] (userresutils/authorized? ctx
                                                   db-spec
                                                   user-id
                                                   auth-scheme
                                                   auth-scheme-param-name))
  :known-content-type? (rucore/known-content-type-predicate (meta/supported-media-types mt-subtype-prefix))
  :exists? (fn [ctx] (not (nil? (fpcore/envlog-by-id db-spec envlog-id))))
  :can-put-to-missing? false
  :new? false
  :respond-with-entity? true
  :multiple-representations? false
  :put! (fn [ctx] (handle-envlog-put! ctx
                                      db-spec
                                      base-url
                                      entity-uri-prefix
                                      (:uri (:request ctx))
                                      user-id
                                      envlog-id
                                      (userresutils/get-plaintext-auth-token ctx
                                                                             auth-scheme
                                                                             auth-scheme-param-name)
                                      embedded-resources-fn
                                      links-fn))
  :handle-ok (fn [ctx] (rucore/handle-resp ctx
                                           hdr-auth-token
                                           hdr-error-mask)))
