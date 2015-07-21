(ns pe-fp-rest.resource.vehicle.version.vehicles-res-v001
  (:require [pe-fp-rest.meta :as meta]
            [clojure.tools.logging :as log]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [pe-user-core.core :as usercore]
            [pe-user-rest.utils :as userresutils]
            [pe-core-utils.core :as ucore]
            [pe-fp-core.core :as fpcore]
            [pe-fp-core.validation :as fpval]
            [pe-fp-rest.resource.vehicle.vehicles-res :refer [new-vehicle-validator-fn
                                                              body-data-in-transform-fn
                                                              body-data-out-transform-fn
                                                              next-vehicle-id-fn
                                                              save-new-vehicle-fn]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 Validator function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod new-vehicle-validator-fn meta/v001
  [version vehicle]
  (fpval/create-vehicle-validation-mask vehicle))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 body-data transformation functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod body-data-in-transform-fn meta/v001
  [version
   db-spec
   _   ;for 'vehicles' resource, the 'in' would only ever be a NEW (to-be-created) vehicle, so it by definition wouldn't have an id
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
;; 0.0.1 Next vehicle id function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod next-vehicle-id-fn meta/v001
  [version db-spec]
  (fpcore/next-vehicle-id db-spec))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 Save new vehicle function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod save-new-vehicle-fn meta/v001
  [version
   db-spec
   user-id
   plaintext-auth-token
   new-vehicle-id
   vehicle]
  (if (= (:fpvehicle/name vehicle) "log-me-out")
    (userresutils/become-unauthenticated db-spec user-id plaintext-auth-token)
    (fpcore/save-new-vehicle db-spec user-id new-vehicle-id vehicle)))
