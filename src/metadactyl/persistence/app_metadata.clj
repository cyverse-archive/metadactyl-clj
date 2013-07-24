(ns metadactyl.persistence.app-metadata
  "Persistence layer for app metadata."
  (:use [kameleon.entities]
        [kameleon.queries :only [get-templates-for-app]]
        [korma.core]
        [metadactyl.util.conversions :only [long->timestamp]]
        [slingshot.slingshot :only [throw+]])
  (:require [cheshire.core :as cheshire]
            [clojure.string :as string]
            [clojure-commons.error-codes :as ce]))

(defmacro ^:private assert-not-nil
  "Throws an exception if the result of a group of expressions is nil.

   Parameters:
     id-field - the name of the field to use when storing the ID.
     id       - the identifier to store in the ID field."
  [[id-field id] & body]
  `(let [res# (do ~@body)]
     (if (nil? res#)
       (throw+ {:error_code ce/ERR_NOT_FOUND
                ~id-field   ~id})
       res#)))

(defmacro ^:private assert-not-blank
  "Throws an exception if the result of a group of expresssions is blank.

   Parameters:
     field - the name of the field whose value is blank."
  [[field] & body]
  `(let [res# (do ~@body)]
     (if (string/blank? res#)
       (throw+ {:error_code ce/ERR_ILLEGAL_ARGUMENT
                :field      ~field})
       res#)))

(defn- remove-nil-values
  "Removes entries containing blank values from a map."
  [m]
  (into {} (remove (comp nil? val) m)))

(defn get-app
  "Retrieves an app from the database."
  [app-id]
  (assert-not-nil
   [:app-id app-id]
   (first (select transformation_activity
                  (where {:id app-id})))))

(defn get-single-template-for-app
  "Retrieves the template from a single-step app. An exception will be thrown if the app doesn't
   have exactly one step."
  [app-hid]
  (let [templates (get-templates-for-app app-hid)]
    (when (not= 1 (count templates))
      (throw+ {:error_code ce/ERR_ILLEGAL_ARGUMENT
               :reason     :NOT_SINGLE_STEP_APP
               :step_count (count templates)}))
    (first templates)))

(defn- get-property-group-in-template
  "Verifies that a selected property group belongs to a specific template."
  [template-hid group-id]
  (assert-not-nil
   [:group_id group-id]
   (first
    (select [:property_group :pg]
            (join [:template_property_group :tpg]
                  {:pg.hid :tpg.property_group_id})
            (join [:template :t]
                  {:tpg.template_id :t.hid})
            (where {:t.hid template-hid
                    :pg.id group-id})))))

(defn- get-property-in-group
  "Verifies that a property belongs to a specific property group."
  [group-hid prop-id]
  (assert-not-nil
   [:property_id prop-id]
   (first
    (select [:property :p]
            (fields :p.hid :p.dataobject_id [:pt.name :type])
            (join [:property_group_property :pgp]
                  {:p.hid :pgp.property_id})
            (join [:property_group :pg]
                  {:pgp.property_group_id :pg.hid})
            (join [:property_type :pt]
                  {:p.property_type :pt.hid})
            (where {:pg.hid group-hid
                    :p.id   prop-id})))))

(defn- get-validator-for-prop
  "Gets the validator associated with a property."
  [prop-hid]
  (select [:validator :v]
          (join [:property :p]
                {:v.hid :p.validator})
          (where {:p.hid prop-hid})))

(defn- get-prop-info-type
  "Gets the info type associated with a property."
  [prop-hid]
  ((comp :name first)
   (select [:info_type :t]
           (join [:dataobjects :d]
                 {:t.hid :d.info_type})
           (join [:property :p]
                 {:d.hid :p.dataobject_id})
           (where {:p.hid prop-hid}))))

(defn- get-validator-for-prop
  "Gets the validator associated with a property."
  [prop-hid]
  (select [:validator :v]
          (join [:property :p]
                {:p.validator :v.hid})
          (where {:p.hid prop-hid})))

(defn- get-must-contain-rules-for-prop
  "Gets the list of MustContain rules associated with a property."
  [prop-hid]
  (select [:rule :r]
          (join [:rule_type :rt]
                {:r.rule_type :rt.hid})
          (join [:validator_rule :vr]
                {:r.hid :vr.rule_id})
          (join [:validator :v]
                {:v.hid :vr.validator_id})
          (join [:property :p]
                {:v.hid :p.validator})
          (where {:p.hid   prop-hid
                  :rt.name "MustContain"})))

(def ^:private generated-selection-list-info-types
  "The list of info types for which selection lists are generated."
  ["ReferenceAnnotation" "ReferenceGenome" "ReferenceSequence"])

(defn update-must-contain-arg
  "Updates a single argument in a MustContain rule."
  [new-args {:keys [rule_id argument_value hid]}]
  (let [old-arg    (cheshire/decode argument_value true)
        wanted-arg (fn [arg] (every? true? (map #(= (% old-arg) (% arg)) [:name :value])))
        new-arg    (first (filter wanted-arg new-args))]
    (when-not (nil? (:display new-arg))
      (let [updated-arg (cheshire/encode (assoc old-arg :display (:display new-arg)))]
        (update :rule_argument
                (set-fields {:argument_value updated-arg})
                (where {:rule_id rule_id
                        :hid     hid}))))))

(defn update-must-contain-rule-labels
  "Updates the display strings in a single MustContain rule."
  [arguments {:keys [hid] :as rule}]
  (dorun (map (partial update-must-contain-arg arguments)
              (select :rule_argument (where {:rule_id hid})))))

(defn update-must-contain-rules-in-validator
  "Updates the labels in a MustContain rule."
  [prop-hid arguments]
  (let [info-type (get-prop-info-type prop-hid)]
    (when-not (some (partial = info-type) generated-selection-list-info-types)
      (if-let [rules (seq (get-must-contain-rules-for-prop prop-hid))]
        (dorun (map (partial update-must-contain-rule-labels arguments) rules))
        (throw+ {:error_code ce/ERR_ILLEGAL_ARGUMENT
                 :reason     :property_missing_must_contain_rule})))))

(defn update-data-object-labels
  "Updates the labels in a data object."
  ([hid label description]
     (when hid
       (update data_object
               (set-fields
                (remove-nil-values
                 {:name        label
                  :description description}))
               (where {:hid hid}))))
  ([hid {:keys [type label description]}]
     (cond
      (= type "MultiFileSelector") (update-data-object-labels hid label description)
      (re-find #"Input$" type)     (update-data-object-labels hid label description)
      (re-find #"Output$" type)    (update-data-object-labels hid nil description))))

(defn update-property-labels
  "Updates the labels in a property."
  [group-id {:keys [id name description label arguments] :as prop}]
  (let [{:keys [hid dataobject_id type]} (get-property-in-group group-id id)]
    (update property
            (set-fields
             (remove-nil-values
              {:name        name
               :description description
               :label       label}))
            (where {:hid hid}))
    (update-data-object-labels dataobject_id (assoc prop :type type))
    (when (seq arguments)
      (update-must-contain-rules-in-validator hid arguments))))

(defn update-property-group-labels
  "Updates the labels in a property group."
  [template-hid {:keys [id name description label] :as group}]
  (let [hid (:hid (get-property-group-in-template template-hid id))]
    (update property_group
            (set-fields
             (remove-nil-values
              {:name        name
               :description description
               :label       label}))
            (where {:hid hid}))
    (dorun (map (partial update-property-labels hid) (:properties group)))))

(defn update-template-labels
  "Updates the labels in a template."
  [req template-hid]
  (update template
          (set-fields
           (remove-nil-values
            {:name        (:name req)
             :description (:description req)
             :label       (:label req)}))
          (where {:hid template-hid}))
  (dorun (map (partial update-property-group-labels template-hid) (:groups req))))

(defn update-app-labels
  "Updates the labels in an app."
  [req app-hid]
  (update transformation_activity
          (set-fields
           (remove-nil-values
            {:name             (:name req)
             :description      (:description req)
             :edited_date      (long->timestamp (:edited_date req))
             :integration_date (long->timestamp (:published_date req))}))
          (where {:hid app-hid}))
  (update-template-labels req (:hid (get-single-template-for-app app-hid))))
