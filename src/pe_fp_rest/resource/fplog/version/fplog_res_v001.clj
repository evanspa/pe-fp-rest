(ns pe-fp-rest.resource.fplog.version.fplog-res-v001
  (:require [pe-fp-rest.meta :as meta]
            [pe-fp-rest.resource.fplog.fplog-utils :as fplogresutils]
            [clojure.tools.logging :as log]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [pe-core-utils.core :as ucore]
            [pe-fp-core.core :as fpcore]
            [pe-fp-core.validation :as fpval]
            [pe-fp-rest.resource.fplog.fplog-res :refer [save-fplog-validator-fn
                                                         body-data-in-transform-fn
                                                         body-data-out-transform-fn
                                                         save-fplog-fn
                                                         delete-fplog-fn
                                                         load-fplog-fn]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 Validator function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod save-fplog-validator-fn meta/v001
  [version fplog]
  (fpval/save-fplog-validation-mask fplog))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 body-data transformation functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod body-data-in-transform-fn meta/v001
  [version
   user-id
   fplog-entid
   fplog]
  (-> fplog
      (fplogresutils/fplog-data-in-transform)
      (assoc :fplog/purchased-at (c/from-long (Long. (:fplog/purchased-at fplog))))))

(defmethod body-data-out-transform-fn meta/v001
  [version
   db-spec
   user-id
   fplog-id
   base-url
   entity-uri-prefix
   entity-uri
   fplog]
  (-> fplog
      (fplogresutils/fplog-data-out-transform base-url entity-uri-prefix)
      (ucore/transform-map-val :fplog/created-at #(c/to-long %))
      (ucore/transform-map-val :fplog/deleted-at #(c/to-long %))
      (ucore/transform-map-val :fplog/updated-at #(c/to-long %))
      (ucore/transform-map-val :fplog/purchased-at #(c/to-long %))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 Save fplog function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod save-fplog-fn meta/v001
  [version
   db-spec
   user-id
   fplog-id
   plaintext-auth-token
   fplog
   if-unmodified-since]
  (fpcore/save-fplog db-spec
                     fplog-id
                     (assoc fplog :fplog/user-id user-id)
                     if-unmodified-since))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 Delete fplog function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod delete-fplog-fn meta/v001
  [version
   db-spec
   user-id
   fplog-id
   delete-reason
   plaintext-auth-token
   if-unmodified-since]
  (fpcore/mark-fplog-as-deleted db-spec fplog-id if-unmodified-since))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 Load fplog function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod load-fplog-fn meta/v001
  [version
   db-spec
   user-id
   fplog-id
   plaintext-auth-token
   if-modified-since]
  (fpcore/fplog-by-id db-spec fplog-id true))
