(ns pe-fp-rest.resource.envlog.version.envlogs-res-v001
  (:require [pe-fp-rest.meta :as meta]
            [pe-fp-rest.resource.envlog.envlog-utils :as envlogresutils]
            [clojure.tools.logging :as log]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [pe-core-utils.core :as ucore]
            [pe-fp-core.core :as fpcore]
            [pe-fp-core.validation :as fpval]
            [pe-fp-rest.resource.envlog.envlogs-res :refer [new-envlog-validator-fn
                                                            body-data-in-transform-fn
                                                            body-data-out-transform-fn
                                                            save-new-envlog-fn
                                                            next-envlog-id-fn]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 Validator function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod new-envlog-validator-fn meta/v001
  [version envlog]
  (fpval/create-envlog-validation-mask envlog))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 body-data transformation functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod body-data-in-transform-fn meta/v001
  [version
   user-id
   new-envlog]
  (envlogresutils/envlog-in-transform new-envlog))

(defmethod body-data-out-transform-fn meta/v001
  [version
   db-spec
   user-id
   base-url
   entity-uri-prefix
   entity-uri
   new-envlog-id
   new-envlog]
  (envlogresutils/envlog-out-transform new-envlog base-url entity-uri-prefix))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 Save new envlog function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod save-new-envlog-fn meta/v001
  [version
   db-spec
   user-id
   plaintext-auth-token
   new-envlog-id
   envlog]
  (fpcore/save-new-envlog db-spec
                          user-id
                          (:envlog/vehicle-id envlog)
                          new-envlog-id
                          envlog))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 Next envlog id function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod next-envlog-id-fn meta/v001
  [version db-spec]
  (fpcore/next-envlog-id db-spec))
