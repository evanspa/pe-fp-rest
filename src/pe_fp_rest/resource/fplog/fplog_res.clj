(ns pe-fp-rest.resource.fplog.fplog-res
  (:require [liberator.core :refer [defresource]]
            [pe-fp-rest.meta :as meta]
            [clojure.tools.logging :as log]
            [pe-rest-utils.macros :refer [defmulti-by-version]]
            [pe-rest-utils.core :as rucore]
            [pe-rest-utils.meta :as rumeta]
            [pe-user-rest.utils :as userresutils]
            [pe-fp-core.core :as fpcore]
            [pe-fp-core.validation :as fpval]))

(declare process-fplogs-put!)
(declare save-fplog-validator-fn)
(declare body-data-in-transform-fn)
(declare body-data-out-transform-fn)
(declare save-fplog-fn)
(declare delete-fplog-fn)
(declare load-fplog-fn)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Handler
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn handle-fplog-put!
  [ctx
   db-spec
   base-url
   entity-uri-prefix
   fplog-uri
   user-id
   fplog-id
   plaintext-auth-token
   embedded-resources-fn
   links-fn
   if-unmodified-since-hdr]
  (rucore/put-or-post-invoker ctx
                              :put
                              db-spec
                              base-url
                              entity-uri-prefix
                              fplog-uri
                              embedded-resources-fn
                              links-fn
                              [user-id fplog-id]
                              plaintext-auth-token
                              save-fplog-validator-fn
                              fpval/sfplog-any-issues
                              body-data-in-transform-fn
                              body-data-out-transform-fn
                              nil
                              nil
                              save-fplog-fn
                              nil
                              nil
                              nil
                              if-unmodified-since-hdr))

(defn handle-fplog-delete!
  [ctx
   db-spec
   base-url
   entity-uri-prefix
   fplog-uri
   user-id
   fplog-id
   plaintext-auth-token
   embedded-resources-fn
   links-fn
   if-unmodified-since-hdr]
  (rucore/delete-invoker ctx
                         db-spec
                         base-url
                         entity-uri-prefix
                         fplog-uri
                         embedded-resources-fn
                         links-fn
                         [user-id fplog-id]
                         plaintext-auth-token
                         body-data-out-transform-fn
                         delete-fplog-fn
                         nil
                         if-unmodified-since-hdr))

(defn handle-fplog-get
  [ctx
   db-spec
   base-url
   entity-uri-prefix
   fplog-uri
   user-id
   fplog-id
   plaintext-auth-token
   embedded-resources-fn
   links-fn
   if-modified-since-hdr
   resp-gen-fn]
  (rucore/get-invoker ctx
                      db-spec
                      base-url
                      entity-uri-prefix
                      fplog-uri
                      embedded-resources-fn
                      links-fn
                      [user-id fplog-id]
                      plaintext-auth-token
                      body-data-out-transform-fn
                      load-fplog-fn
                      if-modified-since-hdr
                      resp-gen-fn))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Validator function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmulti-by-version save-fplog-validator-fn meta/v001)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; body-data transformation functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmulti-by-version body-data-in-transform-fn meta/v001)
(defmulti-by-version body-data-out-transform-fn meta/v001)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Save new fplog function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmulti-by-version save-fplog-fn meta/v001)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Delete fplog function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmulti-by-version delete-fplog-fn meta/v001)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Load fplog function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmulti-by-version load-fplog-fn meta/v001)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Resource definition
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defresource fplog-res
  [db-spec
   mt-subtype-prefix
   hdr-auth-token
   hdr-error-mask
   auth-scheme
   auth-scheme-param-name
   base-url
   entity-uri-prefix
   user-id
   fplog-id
   embedded-resources-fn
   links-fn
   if-unmodified-since-hdr
   if-modified-since-hdr]
  :available-media-types (rucore/enumerate-media-types (meta/supported-media-types mt-subtype-prefix))
  :available-charsets rumeta/supported-char-sets
  :available-languages rumeta/supported-languages
  :allowed-methods [:put :delete :get]
  :authorized? (fn [ctx] (userresutils/authorized? ctx
                                                   db-spec
                                                   user-id
                                                   auth-scheme
                                                   auth-scheme-param-name))
  :known-content-type? (rucore/known-content-type-predicate (meta/supported-media-types mt-subtype-prefix))
  :can-put-to-missing? false
  :new? false
  :respond-with-entity? true
  :multiple-representations? false
  :put! (fn [ctx]
          (handle-fplog-put! ctx
                             db-spec
                             base-url
                             entity-uri-prefix
                             (:uri (:request ctx))
                             user-id
                             fplog-id
                             (userresutils/get-plaintext-auth-token ctx
                                                                    auth-scheme
                                                                    auth-scheme-param-name)
                             embedded-resources-fn
                             links-fn
                             if-unmodified-since-hdr))
  :delete! (fn [ctx]
             (handle-fplog-delete! ctx
                                   db-spec
                                   base-url
                                   entity-uri-prefix
                                   (:uri (:request ctx))
                                   user-id
                                   fplog-id
                                   (userresutils/get-plaintext-auth-token ctx
                                                                          auth-scheme
                                                                          auth-scheme-param-name)
                                   embedded-resources-fn
                                   links-fn
                                   if-unmodified-since-hdr))
  :handle-ok (fn [ctx]
               (if (= (get-in ctx [:request :request-method]) :get)
                 (handle-fplog-get ctx
                                   db-spec
                                   base-url
                                   entity-uri-prefix
                                   (:uri (:request ctx))
                                   user-id
                                   fplog-id
                                   (userresutils/get-plaintext-auth-token ctx
                                                                          auth-scheme
                                                                          auth-scheme-param-name)
                                   embedded-resources-fn
                                   links-fn
                                   if-modified-since-hdr
                                   #(rucore/handle-resp % hdr-auth-token hdr-error-mask))
                 (rucore/handle-resp ctx hdr-auth-token hdr-error-mask))))
