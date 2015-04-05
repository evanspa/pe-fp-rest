(ns pe-fp-rest.resource.fplog.fplog-utils
  (:require [clojure.tools.logging :as log]
            [pe-core-utils.core :as ucore]))

(defn fplog-data-in-transform
  [{fuelstation-link :fpfuelpurchaselog/fuelstation
    vehicle-link :fpfuelpurchaselog/vehicle
    :as body-data}]
  (-> body-data
      (ucore/transform-map-val :fpfuelpurchaselog/carwash-per-gal-discount #(.doubleValue %))
      (ucore/transform-map-val :fpfuelpurchaselog/num-gallons #(.doubleValue %))
      (ucore/transform-map-val :fpfuelpurchaselog/gallon-price #(.doubleValue %))
      (assoc :fpfuelpurchaselog/fuelstation
             (Long/parseLong (.substring fuelstation-link (inc (.lastIndexOf fuelstation-link "/")))))
      (assoc :fpfuelpurchaselog/vehicle
             (Long/parseLong (.substring vehicle-link (inc (.lastIndexOf vehicle-link "/")))))))
