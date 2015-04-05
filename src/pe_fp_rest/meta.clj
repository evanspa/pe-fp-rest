(ns pe-fp-rest.meta
  (:require [pe-rest-utils.meta :as rumeta]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Media type vars
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn mt-subtype-vehicle
  [mt-subtype-prefix]
  (str mt-subtype-prefix "vehicle"))

(defn mt-subtype-fuelstation
  [mt-subtype-prefix]
  (str mt-subtype-prefix "fuelstation"))

(defn mt-subtype-fplog
  [mt-subtype-prefix]
  (str mt-subtype-prefix "fplog"))

(defn mt-subtype-envlog
  [mt-subtype-prefix]
  (str mt-subtype-prefix "envlog"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The versions of this REST API
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def v001 "0.0.1")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Link relations
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def fp-vehicles-relation     :vehicles)
(def fp-fuelstations-relation :fuelstations)
(def fp-fplogs-relation       :fuelpurchase-logs)
(def fp-envlogs-relation      :environment-logs)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 'Authorization' request header components
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; TODO - these should be defined in test_utils, and in pe-fp-app
;(def auth-scheme "fp-user-auth")
;(def auth-scheme-param-name "fp-user-token")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; URL path components
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def pathcomp-vehicles          "vehicles")
(def pathcomp-fuelstations      "fuelstations")
(def pathcomp-fuelpurchase-logs "fplogs")
(def pathcomp-environment-logs  "envlogs")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Information about this REST API, including supported content
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn supported-media-types
  [mt-subtype-prefix]
  "Convenient data structure that succinctly captures the set of media types
   (including version and format indicators) supported by this REST API."
  {rumeta/mt-type
   {:subtypes
    {(mt-subtype-vehicle mt-subtype-prefix)     {:versions {v001 {:format-inds #{"edn" "json"}}}}
     (mt-subtype-fuelstation mt-subtype-prefix) {:versions {v001 {:format-inds #{"edn" "json"}}}}
     (mt-subtype-fplog mt-subtype-prefix)       {:versions {v001 {:format-inds #{"edn" "json"}}}}
     (mt-subtype-envlog mt-subtype-prefix)      {:versions {v001 {:format-inds #{"edn" "json"}}}}}}})
