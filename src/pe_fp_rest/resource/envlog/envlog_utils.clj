(ns pe-fp-rest.resource.envlog.envlog-utils
  (:require [clojure.tools.logging :as log]
            [pe-core-utils.core :as ucore]))

(defn envlog-data-in-transform
  [{vehicle-link :fpenvironmentlog/vehicle :as body-data}]
  (-> body-data
      (ucore/transform-map-val :fpenvironmentlog/reported-avg-mpg #(.doubleValue %))
      (ucore/transform-map-val :fpenvironmentlog/reported-avg-mph #(.doubleValue %))
      (ucore/transform-map-val :fpenvironmentlog/outside-temp #(.doubleValue %))
      (ucore/transform-map-val :fpenvironmentlog/odometer #(.doubleValue %))
      (assoc :fpenvironmentlog/vehicle
             (Long/parseLong (.substring vehicle-link (inc (.lastIndexOf vehicle-link "/")))))))
