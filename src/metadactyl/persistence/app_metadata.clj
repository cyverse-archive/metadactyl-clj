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

(defn ^:private remove-blank-values
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

(defn update-app-labels
  "Updates the labels in an app."
  [req app-hid]
  (update transformation_activity
          (set-fields
           (remove-blank-values
            {:name             (:name req)
             :label            (:label req)
             :edited_date      (long->timestamp (:edited_date req))
             :integration_date (long->timestamp (:published_date req))}))
          (where {:hid app-hid}))
  ;; TODO: implement update-template-labels
  #_(update-template-labels req (get-single-template-for-app app-hid)))
