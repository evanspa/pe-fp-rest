(ns pe-fp-rest.resource.fplog.fplog-utils
  (:require [clojure.tools.logging :as log]
            [pe-rest-utils.core :as rucore]
            [pe-user-rest.utils :as userrestutil]
            [pe-fp-rest.meta :as meta]
            [pe-core-utils.core :as ucore]))

(defn fplog-data-in-transform
  [{fuelstation-link :fplog/fuelstation
    vehicle-link :fplog/vehicle
    :as body-data}]
  (-> body-data
      (ucore/transform-map-val :fplog/carwash-per-gal-discount #(.doubleValue %))
      (ucore/transform-map-val :fplog/num-gallons #(.doubleValue %))
      (ucore/transform-map-val :fplog/gallon-price #(.doubleValue %))
      (assoc :fplog/fuelstation-id (rucore/entity-id-from-uri fuelstation-link))
      (assoc :fplog/vehicle-id (rucore/entity-id-from-uri vehicle-link))))

(defn fplog-data-out-transform
  [{fplog-id :fplog/id
    user-id :fplog/user-id
    vehicle-id :fplog/vehicle-id
    fuelstation-id :fplog/fuelstation-id
    :as fplog}
   base-url
   entity-url-prefix]
  (-> fplog
      (assoc :fplog/vehicle (userrestutil/make-user-subentity-url base-url
                                                                  entity-url-prefix
                                                                  user-id
                                                                  meta/pathcomp-vehicles
                                                                  vehicle-id))
      (assoc :fplog/fuelstation (userrestutil/make-user-subentity-url base-url
                                                                      entity-url-prefix
                                                                      user-id
                                                                      meta/pathcomp-fuelstations
                                                                      fuelstation-id))
      (dissoc :fplog/user-id)
      (dissoc :fplog/vehicle-id)
      (dissoc :fplog/fuelstation-id)))
