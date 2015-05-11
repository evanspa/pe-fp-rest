(ns pe-fp-rest.resource.envlog.version.envlogs-res-v001
  (:require [pe-fp-rest.meta :as meta]
            [pe-fp-rest.resource.envlog.envlog-utils :as envlogresutils]
            [clojure.tools.logging :as log]
            [pe-core-utils.core :as ucore]
            [pe-fp-core.core :as fpcore]
            [pe-fp-core.validation :as fpval]
            [pe-fp-rest.resource.envlog.envlogs-res :refer [new-envlog-validator-fn
                                                            body-data-in-transform-fn
                                                            body-data-out-transform-fn
                                                            save-new-envlog-fn]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 Validator function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod new-envlog-validator-fn meta/v001
  [version envlog]
  (fpval/create-environmentlog-validation-mask envlog))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 body-data transformation functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod body-data-in-transform-fn meta/v001
  [version
   conn
   _   ;for 'envlogs' resource, the 'in' would only ever be a NEW (to-be-created) envlog, so it by definition wouldn't have an entid
   envlog
   apptxnlogger]
  (envlogresutils/envlog-data-in-transform envlog))

(defmethod body-data-out-transform-fn meta/v001
  [version
   conn
   envlog-entid
   envlog
   apptxnlogger]
  (identity envlog))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 Save new envlog function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod save-new-envlog-fn meta/v001
  [version conn partition user-entid envlog]
  (fpcore/save-new-envlog-txnmap partition user-entid envlog))
