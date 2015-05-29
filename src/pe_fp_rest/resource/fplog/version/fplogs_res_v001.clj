(ns pe-fp-rest.resource.fplog.version.fplogs-res-v001
  (:require [pe-fp-rest.meta :as meta]
            [pe-fp-rest.resource.fplog.fplog-utils :as fplogresutils]
            [clojure.tools.logging :as log]
            [pe-core-utils.core :as ucore]
            [pe-fp-core.core :as fpcore]
            [pe-fp-core.validation :as fpval]
            [pe-fp-rest.resource.fplog.fplogs-res :refer [new-fplog-validator-fn
                                                          body-data-in-transform-fn
                                                          body-data-out-transform-fn
                                                          save-new-fplog-fn]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 Validator function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod new-fplog-validator-fn meta/v001
  [version fplog]
  (fpval/create-fuelpurchaselog-validation-mask fplog))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 body-data transformation functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod body-data-in-transform-fn meta/v001
  [version
   db-spec
   _   ;for 'fplogs' resource, the 'in' would only ever be a NEW (to-be-created) fplog, so it by definition wouldn't have an id
   fplog]
  (-> fplog
      (fplogresutils/fplog-data-in-transform)
      (assoc :fplog/created-at (c/from-long (Long. (:fplog/created-at fplog))))))

(defmethod body-data-out-transform-fn meta/v001
  [version
   db-spec
   fplog-id
   fplog
   apptxnlogger]
  (-> fplog
      (ucore/transform-map-val :fplog/created-at #(c/to-long %))
      (ucore/transform-map-val :fplog/updated-at #(c/to-long %))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 Save new fplog function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod save-new-fplog-fn meta/v001
  [version
   db-spec
   user-id
   new-fplog-id
   fplog]
  (fpcore/save-new-fplog db-spec
                         user-id
                         (:fplog/vehicle-id fplog)
                         (:fplog/fuelstation-id fplog)
                         new-fplog-id
                         fplog))
