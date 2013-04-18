(ns metadactyl.translations.app-metadata.util
  (:use [slingshot.slingshot :only [throw+]])
  (:require [clojure-commons.error-codes :as ce]))

(defn get-property-groups
  "Gets the list of property groups "
  [template]
  (cond
   (map? (:groups template))    (get-property-groups (:groups template))
   (vector? (:groups template)) (:groups template)
   (nil? (:groups template))    (throw+ {:error_code ce/ERR_INVALID_JSON
                                         :detail     :MISSING_PROPERTY_GROUP_LIST})
   :else                        (throw+ {:error_code ce/ERR_INVALID_JSON
                                         :detail     :INVALID_PROPERTY_GROUP_LIST})))
