(ns metadactyl.translations.app-metadata.util
  (:use [slingshot.slingshot :only [throw+]])
  (:require [clojure.set :as set]
            [clojure-commons.error-codes :as ce]))

(def input-property-types
  #{"Input" "FileInput" "FolderInput" "MultiFileSelector"})

(def output-property-types
  #{"Output" "FileOutput" "FolderOutput" "MultiFileOutput"})

(def io-property-types
  (set/union input-property-types output-property-types))

(def ^:private input-multiplicities-and-prop-types
  [["FileInput"         "One"]
   ["FolderInput"       "Folder"]
   ["MultiFileSelector" "Many"]])

(def ^:private output-multiplicities-and-prop-types
  [["FileOutput"      "One"]
   ["FolderOutput"    "Folder"]
   ["MultiFileOutput" "Many"]])

(def ^:private input-multiplicity-for
  (into {} input-multiplicities-and-prop-types))

(def ^:private input-property-type-for
  (into {} (map (comp vec reverse) input-multiplicities-and-prop-types)))

(def ^:private output-multiplicity-for
  (into {} output-multiplicities-and-prop-types))

(def ^:private output-property-type-for
  (into {} (map (comp vec reverse)  output-multiplicities-and-prop-types)))

(defn multiplicity-for
  [prop-type mult]
  (cond
   (input-property-types prop-type)  (input-multiplicity-for prop-type mult)
   (output-property-types prop-type) (output-multiplicity-for prop-type mult)
   :else                             mult))

(defn property-type-for
  [prop-type mult]
  (cond
   (input-property-types prop-type)  (input-property-type-for mult prop-type)
   (output-property-types prop-type) (output-property-type-for mult prop-type)
   :else                             mult))

(defn generic-property-type-for
  [prop-type]
  (cond
   (input-property-types prop-type)  "Input"
   (output-property-types prop-type) "Output"
   :else                             prop-type))

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
