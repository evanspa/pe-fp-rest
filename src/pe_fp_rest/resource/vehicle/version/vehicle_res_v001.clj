(ns pe-fp-rest.resource.vehicle.version.vehicle-res-v001
  (:require [pe-fp-rest.meta :as meta]
            [clojure.tools.logging :as log]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [pe-core-utils.core :as ucore]
            [pe-user-rest.utils :as userresutils]
            [pe-fp-core.core :as fpcore]
            [pe-fp-core.validation :as fpval]
            [pe-fp-rest.resource.vehicle.vehicle-utils :as vehresutils]
            [pe-fp-rest.resource.vehicle.vehicle-res :refer [save-vehicle-validator-fn
                                                             body-data-in-transform-fn
                                                             body-data-out-transform-fn
                                                             save-vehicle-fn
                                                             delete-vehicle-fn
                                                             load-vehicle-fn]]))

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
   user-id
   vehicle-id
   vehicle]
  (identity vehicle))

(defmethod body-data-out-transform-fn meta/v001
  [version
   db-spec
   user-id
   vehicle-id
   base-url
   entity-uri-prefix
   entity-uri
   vehicle]
  (vehresutils/vehicle-out-transform vehicle))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 Save vehicle function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod save-vehicle-fn meta/v001
  [version
   db-spec
   user-id
   vehicle-id
   plaintext-auth-token
   vehicle
   if-unmodified-since]
  (if (= (:fpvehicle/name vehicle) "log-me-out")
    (userresutils/become-unauthenticated db-spec user-id plaintext-auth-token)
    (fpcore/save-vehicle db-spec
                         vehicle-id
                         (assoc vehicle :fpvehicle/user-id user-id)
                         if-unmodified-since)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 Delete vehicle function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod delete-vehicle-fn meta/v001
  [version
   db-spec
   user-id
   vehicle-id
   delete-reason
   plaintext-auth-token
   if-unmodified-since]
  (fpcore/mark-vehicle-as-deleted db-spec vehicle-id if-unmodified-since))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 Load vehicle function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod load-vehicle-fn meta/v001
  [version
   db-spec
   user-id
   vehicle-id
   plaintext-auth-token
   if-modified-since]
  (fpcore/vehicle-by-id db-spec vehicle-id true))
