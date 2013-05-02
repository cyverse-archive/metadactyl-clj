(ns metadactyl.translations.app-metadata.util
  (:use [slingshot.slingshot :only [throw+]])
  (:require [clojure.set :as set]
            [clojure-commons.error-codes :as ce]))

(def input-property-types
  #{"Input" "FileInput" "FolderInput" "MultiFileSelector"})

(def output-property-types
  #{"Output"})

(def io-property-types
  (set/union input-property-types output-property-types))

(def ^:private multiplicities-and-prop-types
  [["FileInput"         "One"]
   ["FolderInput"       "Folder"]
   ["MultiFileSelector" "Many"]
   ["Output"            "One"]])

(def multiplicity-for
  (into {} multiplicities-and-prop-types))

(def property-type-for
  (into {} (map (comp vec reverse) multiplicities-and-prop-types)))

(defn get-property-groups
  "Gets the list of property groups "
  [template]
  (cond
   (map? (:groups template))        (get-property-groups (:groups template))
   (sequential? (:groups template)) (:groups template)
   (nil? (:groups template))        (throw+ {:error_code ce/ERR_INVALID_JSON
                                             :detail     :MISSING_PROPERTY_GROUP_LIST})
   :else                            (throw+ {:error_code ce/ERR_INVALID_JSON
                                             :detail     :INVALID_PROPERTY_GROUP_LIST})))
