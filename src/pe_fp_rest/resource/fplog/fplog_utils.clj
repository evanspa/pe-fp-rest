(ns pe-fp-rest.resource.fplog.fplog-utils
  (:require [clojure.tools.logging :as log]
            [pe-core-utils.core :as ucore]))

(defn fplog-data-in-transform
  [{fuelstation-link :fplog/fuelstation
    vehicle-link :fplog/vehicle
    :as body-data}]
  (-> body-data
      (ucore/transform-map-val :fplog/carwash-per-gal-discount #(.doubleValue %))
      (ucore/transform-map-val :fplog/num-gallons #(.doubleValue %))
      (ucore/transform-map-val :fplog/gallon-price #(.doubleValue %))
      (assoc :fplog/fuelstation-id
             (Long/parseLong (.substring fuelstation-link (inc (.lastIndexOf fuelstation-link "/")))))
      (assoc :fplog/vehicle-id
             (Long/parseLong (.substring vehicle-link (inc (.lastIndexOf vehicle-link "/")))))))
