(ns metadactyl.persistence.app-metadata
  "Persistence layer for app metadata."
  (:use [kameleon.entities]
        [kameleon.queries :only [get-templates-for-app]]
        [korma.core]
        [metadactyl.util.conversions :only [long->timestamp]]
        [slingshot.slingshot :only [throw+]])
  (:require [clojure.string :as string]
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

(defn- remove-blank-values
  "Removes entries containing blank values from a map."
  [m]
  (into {} (remove (comp string/blank? val) m)))

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

(defn- validate-property-group
  "Verifies that a selected property group belongs to a template."
  [template-hid group-id]
  (assert-not-nil
   [:group_id group-id]
   (first
    (select [:template :t]
            (join [:template_property_group :tpg]
                  {:t.hid :tpg.template_id})
            (join [:property_group :pg]
                  {:tpg.property_group_id :pg.hid})
            (where {:t.hid template-hid
                    :pg.id group-id})))))

(defn update-property-group-labels
  "Updates the labels in a property group."
  [req template-hid {:keys [id name description label] :as group}]
  (validate-property-group template-hid id)
  (update property_group
          (set-fields
           (remove-blank-values
            {:name        name
             :description description
             :label       label}))
          (where {:pg.id group-id}))
  ;; TODO: implement update-property-labels.
  #_(dorun (map (partial update-property-labels group-id) (:properties group))))

(defn update-template-labels
  "Updates the labels in a template."
  [req template-hid]
  (update template
          (set-fields
           (remove-blank-values
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
           (remove-blank-values
            {:name             (:name req)
             :description      (:description req)
             :label            (:label req)
             :edited_date      (long->timestamp (:edited_date req))
             :integration_date (long->timestamp (:published_date req))}))
          (where {:hid app-hid}))
  (update-template-labels req (:hid (get-single-template-for-app app-hid))))
