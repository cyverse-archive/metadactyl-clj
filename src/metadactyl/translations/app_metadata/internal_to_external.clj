(ns metadactyl.translations.app-metadata.internal-to-external
  (:use [metadactyl.translations.app-metadata.util]
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
  (let [rules    (get-in property [:validator :rules] [])
        args     (get-property-arguments rules)
        data-obj (:data_object property)
        type     (:type property)]
    (if (nil? data-obj)
      (assoc (dissoc property :validator :value)
        :arguments    (map #(dissoc % :isDefault) args)
        :required     (get-in property [:validator :required] false)
        :validators   (validators-from-rules rules)
        :defaultValue (get-default-value property args))
      (assoc (dissoc property :validator :value)
        :arguments    (map #(dissoc % :isDefault) args)
        :validators   (validators-from-rules rules)
        :defaultValue (get-default-value property args)
        :data_object  (dissoc data-obj
                              :cmdSwitch :name :description :id :label :order :required
                              :file_info_type_id :format_id :multiplicity)
        :name         (:cmdSwitch data-obj (:name property))
        :description  (:description data-obj (:description property))
        :id           (:id data-obj (:id property))
        :label        (:name data-obj (:label property))
        :order        (:order data-obj (:order property))
        :required     (:required data-obj (:required property false))
        :type         (property-type-for (:type property) (:multiplicity data-obj))))))

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
