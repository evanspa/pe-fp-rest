(ns pe-fp-rest.resource.envlog.envlog-utils
  (:require [clojure.tools.logging :as log]
            [pe-core-utils.core :as ucore]))

(defn envlog-data-in-transform
  [{vehicle-link :envlog/vehicle :as body-data}]
  (-> body-data
      (ucore/transform-map-val :envlog/reported-avg-mpg #(.doubleValue %))
      (ucore/transform-map-val :envlog/reported-avg-mph #(.doubleValue %))
      (ucore/transform-map-val :envlog/outside-temp #(.doubleValue %))
      (ucore/transform-map-val :envlog/odometer #(.doubleValue %))
      (assoc :envlog/vehicle-id
             (Long/parseLong (.substring vehicle-link (inc (.lastIndexOf vehicle-link "/")))))))
