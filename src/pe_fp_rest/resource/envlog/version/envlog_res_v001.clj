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
                                                           save-envlog-fn]]))

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
   db-spec
   envlog-id
   envlog]
  (-> envlog
      (envlogresutils/envlog-data-in-transform)
      (assoc :envlog/logged-at (c/from-long (Long. (:envlog/logged-at envlog))))))

(defmethod body-data-out-transform-fn meta/v001
  [version
   db-spec
   envlog-id
   envlog]
  (-> envlog
      (ucore/transform-map-val :envlog/created-at #(c/to-long %))
      (ucore/transform-map-val :envlog/updated-at #(c/to-long %))
      (ucore/transform-map-val :envlog/logged-at #(c/to-long %))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 Save envlog function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod save-envlog-fn meta/v001
  [version
   db-spec
   user-id
   envlog-id
   envlog]
  (fpcore/save-envlog db-spec
                      envlog-id
                      (assoc envlog :envlog/user-id user-id)))
