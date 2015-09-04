(ns pe-fp-rest.resource.vehicle.vehicle-utils
  (:require [clojure.tools.logging :as log]
            [pe-rest-utils.core :as rucore]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [pe-user-rest.utils :as userrestutil]
            [pe-fp-rest.meta :as meta]
            [pe-core-utils.core :as ucore]))

(defn vehicle-out-transform
  [vehicle]
  (-> vehicle
      (ucore/transform-map-val :fpvehicle/created-at #(c/to-long %))
      (ucore/transform-map-val :fpvehicle/updated-at #(c/to-long %))
      (ucore/transform-map-val :fpvehicle/deleted-at #(c/to-long %))
      (dissoc :fpvehicle/user-id)
      (dissoc :fpvehicle/updated-count)))
