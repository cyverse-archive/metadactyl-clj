(ns metadactyl.translations.app-metadata.internal-to-external
  (:use [metadactyl.translations.app-metadata.util :only [get-property-groups]]
        [slingshot.slingshot :only [throw+]])
  (:require [clojure-commons.error-codes :as ce]))

(defn validators-from-rules
  "Converts a list of rules from the internal JSON format to a list of validators for the
   external JSON format."
  [rules]
  (mapcat (partial map (fn [[k v]] {:type (name k) :params v}))
          (remove :MustContain rules)))

(defn get-property-arguments
  "Gets the property arguments from a list of validation rules."
  [rules]
  (:MustContain (first (filter :MustContain rules)) []))

(defn get-default-value
  "Gets the default value for a property and a set of list of selectable arguments."
  [property args]
  (or (if (seq args)
        (-> (filter #(Boolean/parseBoolean (str (:isDefault %))) args)
            (first)
            (dissoc :isDefault))
        (:value property))
      ""))

(defn translate-property
  "Translates a property from its internal format to its external format."
  [property]
  (let [rules (get-in property [:validator :rules] [])
        args  (get-property-arguments rules)]
    (assoc (dissoc property :validator :value)
      :arguments    (map #(dissoc % :isDefault) args)
      :required     (get-in property [:validator :required] false)
      :validators   (validators-from-rules rules)
      :defaultValue (get-default-value property args))))

(defn translate-property-group
  "Translates a property group from its internal format to its external format."
  [property-group]
  (assoc property-group
    :properties (map translate-property (:properties property-group))))

(defn translate-template
  "Translates a template from its internal format to its external format."
  [template]
  (assoc template
    :groups (map translate-property-group (get-property-groups template))))
