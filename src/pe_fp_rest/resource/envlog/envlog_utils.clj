(ns pe-fp-rest.resource.envlog.envlog-utils
  (:require [clojure.tools.logging :as log]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [pe-rest-utils.core :as rucore]
            [pe-user-rest.utils :as userrestutil]
            [pe-fp-rest.meta :as meta]
            [pe-core-utils.core :as ucore]))

(defn envlog-in-transform
  [{vehicle-link :envlog/vehicle :as envlog}]
  (-> envlog
      (ucore/transform-map-val :envlog/reported-avg-mpg #(.doubleValue %))
      (ucore/transform-map-val :envlog/reported-avg-mph #(.doubleValue %))
      (ucore/transform-map-val :envlog/outside-temp #(.doubleValue %))
      (ucore/transform-map-val :envlog/odometer #(.doubleValue %))
      (ucore/assoc-if-contains envlog :envlog/vehicle :envlog/vehicle-id rucore/entity-id-from-uri)
      (ucore/transform-map-val :envlog/logged-at #(c/from-long (Long. %)))))

(defn envlog-out-transform
  [{user-id :envlog/user-id :as envlog}
   base-url
   entity-url-prefix]
  (-> envlog
      (ucore/assoc-if-contains envlog :envlog/vehicle-id :envlog/vehicle #(userrestutil/make-user-subentity-url base-url
                                                                                                                entity-url-prefix
                                                                                                                user-id
                                                                                                                meta/pathcomp-vehicles
                                                                                                                %))
      (ucore/transform-map-val :envlog/created-at #(c/to-long %))
      (ucore/transform-map-val :envlog/deleted-at #(c/to-long %))
      (ucore/transform-map-val :envlog/updated-at #(c/to-long %))
      (ucore/transform-map-val :envlog/logged-at #(c/to-long %))
      (dissoc :envlog/updated-count)
      (dissoc :envlog/user-id)))
