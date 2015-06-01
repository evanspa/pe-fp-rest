(ns pe-fp-rest.resource.fplog.version.fplog-res-v001
  (:require [pe-fp-rest.meta :as meta]
            [pe-fp-rest.resource.fplog.fplog-utils :as fplogresutils]
            [clojure.tools.logging :as log]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [pe-core-utils.core :as ucore]
            [pe-fp-core.core :as fpcore]
            [pe-fp-core.validation :as fpval]
            [pe-fp-rest.resource.fplog.fplog-res :refer [save-fplog-validator-fn
                                                         body-data-in-transform-fn
                                                         body-data-out-transform-fn
                                                         save-fplog-fn]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 Validator function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod save-fplog-validator-fn meta/v001
  [version fplog]
  (fpval/save-fplog-validation-mask fplog))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 body-data transformation functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod body-data-in-transform-fn meta/v001
  [version
   db-spec
   fplog-entid
   fplog]
  (-> fplog
      (fplogresutils/fplog-data-in-transform)
      (assoc :fplog/updated-at (c/from-long (Long. (:fplog/updated-at fplog))))))

(defmethod body-data-out-transform-fn meta/v001
  [version
   db-spec
   fplog-entid
   fplog]
  (-> fplog
      (ucore/transform-map-val :fplog/created-at #(c/to-long %))
      (ucore/transform-map-val :fplog/updated-at #(c/to-long %))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 Save fplog function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod save-fplog-fn meta/v001
  [version
   db-spec
   user-id
   fplog-id
   fplog]
  (fpcore/save-fplog db-spec
                     fplog-id
                     (assoc fplog :fplog/user-id user-id)))
