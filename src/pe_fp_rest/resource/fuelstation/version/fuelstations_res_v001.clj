(ns pe-fp-rest.resource.fuelstation.version.fuelstations-res-v001
  (:require [pe-fp-rest.meta :as meta]
            [clojure.tools.logging :as log]
            [pe-fp-core.core :as fpcore]
            [pe-fp-core.validation :as fpval]
            [pe-fp-rest.resource.fuelstation.fuelstations-res :refer [new-fuelstation-validator-fn
                                                                      body-data-in-transform-fn
                                                                      body-data-out-transform-fn
                                                                      save-new-fuelstation-fn]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 Validator function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod new-fuelstation-validator-fn meta/v001
  [version body-data]
  (fpval/create-fuelstation-validation-mask body-data))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 body-data transformation functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod body-data-in-transform-fn meta/v001
  [version body-data]
  (identity body-data))

(defmethod body-data-out-transform-fn meta/v001
  [version body-data]
  (identity body-data))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 Save new fuel station function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod save-new-fuelstation-fn meta/v001
  [version conn partition user-entid fuelstation]
  (fpcore/save-new-fuelstation-txnmap partition user-entid fuelstation))
