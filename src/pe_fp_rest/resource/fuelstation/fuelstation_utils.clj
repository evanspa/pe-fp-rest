(ns pe-fp-rest.resource.fuelstation.fuelstation-utils
  (:require [clojure.tools.logging :as log]
            [pe-rest-utils.core :as rucore]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [pe-user-rest.utils :as userrestutil]
            [pe-fp-rest.meta :as meta]
            [pe-core-utils.core :as ucore]))

(defn fuelstation-out-transform
  [fuelstation]
  (-> fuelstation
      (ucore/transform-map-val :fpfuelstation/created-at #(c/to-long %))
      (ucore/transform-map-val :fpfuelstation/deleted-at #(c/to-long %))
      (ucore/transform-map-val :fpfuelstation/updated-at #(c/to-long %))
      (dissoc :fpfuelstation/user-id)
      (dissoc :fpfuelstation/updated-count)))
