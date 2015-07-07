(ns pe-fp-rest.resource.vehicle.version.vehicle-res-v001
  (:require [pe-fp-rest.meta :as meta]
            [clojure.tools.logging :as log]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [pe-core-utils.core :as ucore]
            [pe-user-rest.utils :as userresutils]
            [pe-fp-core.core :as fpcore]
            [pe-fp-core.validation :as fpval]
            [pe-fp-rest.resource.vehicle.vehicle-res :refer [save-vehicle-validator-fn
                                                              body-data-in-transform-fn
                                                              body-data-out-transform-fn
                                                              save-vehicle-fn]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 Validator function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod save-vehicle-validator-fn meta/v001
  [version vehicle]
  (fpval/save-vehicle-validation-mask vehicle))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 body-data transformation functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod body-data-in-transform-fn meta/v001
  [version
   db-spec
   vehicle-id
   vehicle]
  (identity vehicle))

(defmethod body-data-out-transform-fn meta/v001
  [version
   db-spec
   vehicle-id
   vehicle]
  (-> vehicle
      (ucore/transform-map-val :fpvehicle/created-at #(c/to-long %))
      (ucore/transform-map-val :fpvehicle/updated-at #(c/to-long %))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 Save vehicle function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod save-vehicle-fn meta/v001
  [version
   db-spec
   user-id
   vehicle-id
   plaintext-auth-token
   vehicle]
  (if (= (:fpvehicle/name vehicle) "log-me-out")
    (userresutils/become-unauthenticated db-spec user-id plaintext-auth-token)
    (fpcore/save-vehicle db-spec
                         vehicle-id
                         (assoc vehicle :fpvehicle/user-id user-id))))
