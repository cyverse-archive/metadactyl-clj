(ns metadactyl.translations.app-metadata.external-to-internal
  (:use [metadactyl.translations.app-metadata.util :only [get-property-groups]]
        [slingshot.slingshot :only [throw+]])
  (:require [clojure-commons.error-codes :as ce]))

(defn build-validator-for-property
  "Builds a validator for a property in its external format."
  [{rules :validators required :required args :arguments default-value :defaultValue
    :or {rules [] required false}}]
  (if (or required (seq rules) (seq args))
    (let [add-default-flag (fn [arg] (assoc arg :isDefault (= default-value arg)))
          rules            (mapv (fn [{:keys [type params]}] {(keyword type) params}) rules)]
      {:required required
       :rules    (if (seq args)
                   (conj rules {:MustContain (map add-default-flag args)})
                   rules)})))

(defn get-default-value
  "Takes a property in its external format and determines what its default value should be
   after it's been translated to its internal format.."
  [{args :arguments default-value :defaultValue}]
  (if (seq args) nil default-value))

(defn translate-property
  "Translates a property from its external format to its internal format."
  [property]
  (assoc (dissoc property :arguments :required :validators :defaultValue)
    :validator (build-validator-for-property property)
    :value     (get-default-value property)))

(defn translate-property-group
  "Translates a property group from its external format to its internal format."
  [property-group]
  (assoc property-group
    :properties (map translate-property (:properties property-group))))

(defn translate-template
  "Translates a template from its external format to its internal format."
  [template]
  (assoc template
    :groups (map translate-property-group (get-property-groups template))))
