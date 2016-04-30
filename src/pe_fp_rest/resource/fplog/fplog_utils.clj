(ns pe-fp-rest.resource.fplog.fplog-utils
  (:require [clojure.tools.logging :as log]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [pe-rest-utils.core :as rucore]
            [pe-user-rest.utils :as userrestutil]
            [pe-fp-rest.meta :as meta]
            [pe-core-utils.core :as ucore]))

(defn fplog-in-transform
  [{fuelstation-link :fplog/fuelstation
    vehicle-link :fplog/vehicle
    :as fplog}]
  (-> fplog
      (ucore/transform-map-val :fplog/carwash-per-gal-discount #(.doubleValue %))
      (ucore/transform-map-val :fplog/num-gallons #(.doubleValue %))
      (ucore/transform-map-val :fplog/gallon-price #(.doubleValue %))
      (assoc :fplog/fuelstation-id (rucore/entity-id-from-uri fuelstation-link))
      (assoc :fplog/vehicle-id (rucore/entity-id-from-uri vehicle-link))
      (assoc :fplog/purchased-at (c/from-long (Long. (:fplog/purchased-at fplog))))))

(defn fplog-out-transform
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
      (ucore/transform-map-val :fplog/created-at #(c/to-long %))
      (ucore/transform-map-val :fplog/deleted-at #(c/to-long %))
      (ucore/transform-map-val :fplog/updated-at #(c/to-long %))
      (ucore/transform-map-val :fplog/purchased-at #(c/to-long %))
      (dissoc :fplog/updated-count)
      (dissoc :fplog/user-id)))
