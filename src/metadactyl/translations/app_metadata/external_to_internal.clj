(ns metadactyl.translations.app-metadata.external-to-internal
  (:use [slingshot.slingshot :only [throw+]])
  (:require [clojure-commons.error-codes :as ce]))

(defn build-validator-for-property
  "Builds a validator for a property in its external format."
  [{rules :validators required :required args :arguments default-value :defaultValue
    :or {rules [] required false}}]
  (if (or required (seq rules) (seq args))
    (letfn [(add-default-flag [arg] (assoc arg :isDefault (= default-value arg)))]
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

(defn translate-template
  "Translates a template from its external format to its internal format."
  [template]
  (assoc template
    :groups (map translate-property-group (get-property-groups template))))
