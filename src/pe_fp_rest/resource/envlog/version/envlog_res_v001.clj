(ns pe-fp-rest.resource.envlog.version.envlog-res-v001
  (:require [pe-fp-rest.meta :as meta]
            [pe-fp-rest.resource.envlog.envlog-utils :as envlogresutils]
            [clojure.tools.logging :as log]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [pe-core-utils.core :as ucore]
            [pe-fp-core.core :as fpcore]
            [pe-fp-core.validation :as fpval]
            [pe-fp-rest.resource.envlog.envlog-res :refer [save-envlog-validator-fn
                                                           body-data-in-transform-fn
                                                           body-data-out-transform-fn
                                                           save-envlog-fn
                                                           delete-envlog-fn
                                                           load-envlog-fn]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 Validator function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod save-envlog-validator-fn meta/v001
  [version envlog]
  (fpval/save-envlog-validation-mask envlog))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 body-data transformation functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod body-data-in-transform-fn meta/v001
  [version
   user-id
   envlog-id
   envlog]
  (envlogresutils/envlog-in-transform envlog))

(defmethod body-data-out-transform-fn meta/v001
  [version
   db-spec
   user-id
   envlog-id
   base-url
   entity-uri-prefix
   entity-uri
   envlog]
  (envlogresutils/envlog-out-transform envlog base-url entity-uri-prefix))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 Save envlog function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod save-envlog-fn meta/v001
  [version
   db-spec
   user-id
   envlog-id
   plaintext-auth-token
   envlog
   if-unmodified-since]
  (fpcore/save-envlog db-spec
                      envlog-id
                      (assoc envlog :envlog/user-id user-id)
                      if-unmodified-since))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 Delete envlog function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod delete-envlog-fn meta/v001
  [version
   db-spec
   user-id
   envlog-id
   delete-reason
   plaintext-auth-token
   if-unmodified-since]
  (fpcore/mark-envlog-as-deleted db-spec envlog-id if-unmodified-since))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 Load envlog function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod load-envlog-fn meta/v001
  [ctx
   version
   db-spec
   user-id
   envlog-id
   plaintext-auth-token
   if-modified-since]
  (fpcore/envlog-by-id db-spec envlog-id true))
