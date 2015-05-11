(ns pe-fp-rest.resource.fuelstation.version.fuelstation-res-v001
  (:require [pe-fp-rest.meta :as meta]
            [clojure.tools.logging :as log]
            [pe-fp-core.core :as fpcore]
            [pe-fp-core.validation :as fpval]
            [pe-fp-rest.resource.fuelstation.fuelstation-res :refer [save-fuelstation-validator-fn
                                                                     body-data-in-transform-fn
                                                                     body-data-out-transform-fn
                                                                     save-fuelstation-fn]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 Validator function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod save-fuelstation-validator-fn meta/v001
  [version fuelstation]
  (fpval/save-fuelstation-validation-mask fuelstation))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 body-data transformation functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod body-data-in-transform-fn meta/v001
  [version
   conn
   fuelstation-entid
   fuelstation
   apptxnlogger]
  (identity fuelstation))

(defmethod body-data-out-transform-fn meta/v001
  [version
   conn
   fuelstation-entid
   fuelstation
   apptxnlogger]
  (identity fuelstation))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 Save fuel station function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod save-fuelstation-fn meta/v001
  [version
   conn
   partition
   user-entid
   fuelstation-entid
   fuelstation]
  (fpcore/save-fuelstation-txnmap user-entid fuelstation-entid fuelstation))
