(ns pe-fp-rest.resource.fplog.version.fplogs-res-v001
  (:require [pe-fp-rest.meta :as meta]
            [pe-fp-rest.resource.fplog.fplog-utils :as fplogresutils]
            [clojure.tools.logging :as log]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [pe-core-utils.core :as ucore]
            [pe-fp-core.core :as fpcore]
            [pe-fp-core.validation :as fpval]
            [pe-fp-rest.resource.fplog.fplogs-res :refer [new-fplog-validator-fn
                                                          body-data-in-transform-fn
                                                          body-data-out-transform-fn
                                                          save-new-fplog-fn
                                                          next-fplog-id-fn]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 Validator function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod new-fplog-validator-fn meta/v001
  [version fplog]
  (fpval/create-fplog-validation-mask fplog))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 body-data transformation functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod body-data-in-transform-fn meta/v001
  [version
   user-id
   fplog]
  (-> fplog
      (fplogresutils/fplog-data-in-transform)
      (assoc :fplog/purchased-at (c/from-long (Long. (:fplog/purchased-at fplog))))))

(defmethod body-data-out-transform-fn meta/v001
  [version
   db-spec
   user-id
   base-url
   entity-uri-prefix
   entity-uri
   new-fplog-id
   new-fplog]
  (-> new-fplog
      (fplogresutils/fplog-data-out-transform base-url entity-uri-prefix)
      (ucore/transform-map-val :fplog/created-at #(c/to-long %))
      (ucore/transform-map-val :fplog/deleted-at #(c/to-long %))
      (ucore/transform-map-val :fplog/updated-at #(c/to-long %))
      (ucore/transform-map-val :fplog/purchased-at #(c/to-long %))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 Save new fplog function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod save-new-fplog-fn meta/v001
  [version
   db-spec
   user-id
   plaintext-auth-token
   new-fplog-id
   fplog]
  (fpcore/save-new-fplog db-spec
                         user-id
                         (:fplog/vehicle-id fplog)
                         (:fplog/fuelstation-id fplog)
                         new-fplog-id
                         fplog))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 Next fplog id function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod next-fplog-id-fn meta/v001
  [version db-spec]
  (fpcore/next-fplog-id db-spec))
