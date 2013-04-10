(ns metadactyl.zoidberg
  (:use [korma.core]
        [kameleon.core]
        [kameleon.entities]
        [metadactyl.metadactyl :only [current-user]]
        [slingshot.slingshot :only [throw+]])
  (:require [cheshire.core :as cheshire]
            [clojure-commons.error-codes :as cc-errs]))

(defn- get-integrator-email
  "Fetches the integrator email for the given integration data ID."
  [integration_data_id]
  (let [integrator (first (select integration_data
                                  (fields :integrator_email)
                                  (where {:id integration_data_id})))]
    (:integrator_email integrator)))

(defn- verify-ownership
  "Verifies that the current user owns the analysis that is being edited."
  [analysis]
  (let [owner (get-integrator-email (:integration_data_id analysis))]
    (if (not= owner (.getUsername current-user))
      (throw+ {:code cc-errs/ERR_NOT_OWNER,
               :username (.getUsername current-user),
               :message (str
                          (.getShortUsername current-user)
                          " does not own analysis "
                          (:analysis_id analysis))}))))

(defn- verify-analysis-not-public
  "Verifies that an analysis has not been made public."
  [analysis]
  (let [analysis-id (:analysis_id analysis)
        app (first (select analysis_listing
                           (fields :is_public)
                           (where {:id analysis-id})))]
    (if (:is_public app)
      (throw+ {:code cc-errs/ERR_NOT_WRITEABLE,
               :message (str "Workflow, "
                             analysis-id
                             ", is public and may not be edited")}))))

(defn- verify-workflow-editable
  "Verifies that the analysis is allowed to be edited by the current user."
  [analysis]
  (verify-ownership analysis)
  (verify-analysis-not-public analysis))

(defn- with-dataobjects
  "Includes a list of related data objects in the query's result set,
   with fields required by the client."
  [query dataobjects_entity]
  (with query dataobjects_entity
    (join data_formats {:data_format :data_formats.id})
    (fields :id
            :name
            :description
            :required
            [:data_formats.name :format])))

(defn- get-templates
  "Fetches a list of templates for the given IDs with their inputs and outputs."
  [template-ids]
  (select template
          (with-dataobjects inputs)
          (with-dataobjects outputs)
          (fields :hid
                  :id
                  :name
                  :description)
          (where (in :id template-ids))))

(defn- format-template
  "Formats template fields for the client."
  [template]
  (dissoc template :hid))

(defn- get-steps
  "Fetches the steps for the given app ID, including their template ID and
   source/target mapping IDs and step names."
  [app-id]
  (select transformation_steps
          (with input_mapping
            (join [:transformation_steps :source_step]
                  {:input_mapping.source :source_step.id})
            (join [:transformation_steps :target_step]
                  {:input_mapping.target :target_step.id})
            (fields [:source_step.name :source_name]
                    [:target_step.name :target_name]
                    :source
                    :target)
            (group :source
                   :source_name
                   :target
                   :target_name))
          (join [:transformations :tx]
                {:transformation_steps.transformation_id :tx.id})
          (join [:transformation_task_steps :tts]
                {:transformation_steps.id :tts.transformation_step_id})
          (join [:transformation_activity :app]
                {:tts.transformation_task_id :app.hid})
          (fields :transformation_steps.id
                  :guid
                  :transformation_steps.name
                  :transformation_steps.description
                  :tx.template_id)
          (where {:app.id app-id})))

(defn- format-step
  "Formats step fields for the client."
  [step]
  (-> step
    (assoc :id (:guid step))
    (dissoc :guid)
    (dissoc :input_mapping)))

(defn- get-input-output-mappings
  "Fetches the output->input mapping UUIDs for the given source and target IDs."
  [source target]
  (select input_mapping
          (join [:dataobject_mapping :map]
                {:hid :map.mapping_id})
          (fields :map.input
                  :map.output)
          (where {:source source
                  :target target})))

(defn- format-mapping
  "Formats mapping fields for the client."
  [mapping]
  (let [input-output-mappings (get-input-output-mappings (:source mapping)
                                                         (:target mapping))]
    {:source_step (:source_name mapping)
     :target_step (:target_name mapping)
     :map (reduce #(assoc %1 (:output %2) (:input %2))
                  {}
                  input-output-mappings)}))

(defn- get-formatted-mapping
  "Formats a step's list of mapping IDs and step names to fields for the client."
  [step]
  (map #(format-mapping %) (:input_mapping step)))

(defn- format-analysis
  "Adds the steps and mappings fields to the analysis, and extracts a set of
   template IDs from the steps into a templates field."
  [analysis]
  (let [steps (get-steps (:analysis_id analysis))
        template-ids (set (map #(:template_id %) steps))
        mappings (apply concat (map #(get-formatted-mapping %) steps))
        steps (map #(format-step %) steps)]
    (-> analysis
      (dissoc :integration_data_id)
      (assoc :steps steps)
      (assoc :mappings mappings)
      (assoc :templates template-ids))))

(defn- get-analyses
  "Fetches analyses for the given app ID."
  [app-id]
  (let [analyses (select transformation_activity
                         (fields [:id :analysis_id]
                                 [:name :analysis_name]
                                 :description
                                 :integration_data_id)
                         (where {:id app-id}))]
    (when (empty? analyses)
      (throw+ {:code cc-errs/ERR_DOES_NOT_EXIST,
               :message (str "Workflow, " app-id ", not found")}))
    (dorun (map #(verify-workflow-editable %) analyses))
    analyses))

(defn edit-workflow
  "This service prepares a JSON response for editing a workflow in the client."
  [app-id]
  (let [analyses (get-analyses app-id)
        analyses (map #(format-analysis %) analyses)
        template-ids (set (apply concat (map #(:templates %) analyses)))
        templates (map #(format-template %) (get-templates template-ids))
        analyses (map #(dissoc % :templates) analyses)]
    (cheshire/encode {:analyses analyses
                      :templates templates})))
