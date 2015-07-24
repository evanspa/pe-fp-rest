(ns pe-fp-rest.resource.envlog.envlog-utils
  (:require [clojure.tools.logging :as log]
            [pe-rest-utils.core :as rucore]
            [pe-user-rest.utils :as userrestutil]
            [pe-fp-rest.meta :as meta]
            [pe-core-utils.core :as ucore]))

(defn envlog-data-in-transform
  [{vehicle-link :envlog/vehicle :as body-data}]
  (-> body-data
      (ucore/transform-map-val :envlog/reported-avg-mpg #(.doubleValue %))
      (ucore/transform-map-val :envlog/reported-avg-mph #(.doubleValue %))
      (ucore/transform-map-val :envlog/outside-temp #(.doubleValue %))
      (ucore/transform-map-val :envlog/odometer #(.doubleValue %))
      (assoc :envlog/vehicle-id (rucore/entity-id-from-uri vehicle-link))))

(defn envlog-data-out-transform
  [{envlog-id :envlog/id
    user-id :envlog/user-id
    vehicle-id :envlog/vehicle-id
    :as envlog}
   base-url
   entity-url-prefix]
  (-> envlog
      (assoc :envlog/vehicle (userrestutil/make-user-subentity-url base-url
                                                                   entity-url-prefix
                                                                   user-id
                                                                   meta/pathcomp-vehicles
                                                                   vehicle-id))
      (dissoc :envlog/user-id)
      (dissoc :envlog/vehicle-id)))
