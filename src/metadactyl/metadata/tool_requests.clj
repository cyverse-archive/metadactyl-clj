(ns metadactyl.metadata.tool-requests
  (:use [kameleon.entities]
        [kameleon.queries :only [get-user-id]]
        [korma.core]
        [korma.db]
        [metadactyl.service :only [success-response]]
        [slingshot.slingshot :only [throw+]])
  (:require [cheshire.core :as cheshire]
            [clojure.string :as string]
            [metadactyl.util.params :as params])
  (:import [java.util UUID]))

;; Status codes.
(def ^:private status-submitted "Submitted")
(def ^:private status-pending "Pending")
(def ^:private status-evaluation "Evaluation")
(def ^:private status-installation "Installation")
(def ^:private status-completion "Completion")
(def ^:private status-failed "Failed")

(defn- required-field
  "Extracts a required field from a map."
  [m & ks]
  (let [v (first (remove string/blank? (map m ks)))]
    (when (nil? v)
      (throw+ {:code          ::missing_required_field
               :accepted_keys ks}))
    v))

(defn- multithreaded-str-to-flag
  "Converts a multithreaded indication string to a boolean flag."
  [s]
  (condp = s
    "Yes" true
    "No"  false))

(defn- architecture-name-to-id
  "Gets the internal architecture identifier for an architecture name."
  [architecture]
  (let [id (:id (first (select tool_architectures (where {:name architecture}))))]
    (when (nil? id)
      (throw+ {:code ::unknown_architecture
               :name architecture}))
    id))

(defn- status-code-subselect
  "Creates a subselect statement to find the primary key of a status code."
  [status-code]
  (subselect tool_request_status_codes
             (fields :id)
             (where {:name status-code})))

(defn- tool-request-subselect
  "Creates a subselect statement to find the primary key for a tool request UUID."
  [uuid]
  (subselect tool_requests
             (fields :id)
             (where {:uuid uuid})))

(defn- handle-new-tool-request
  "Submits a tool request on behalf of the authenticated user."
  [username req]
  (let [user-id         (get-user-id username)
        architecture-id (architecture-name-to-id (required-field req :architecture))
        uuid            (UUID/randomUUID)]

    (transaction
     (insert tool_requests
             (values {:phone                (:phone req)
                      :uuid                 uuid
                      :tool_name            (required-field req :name)
                      :description          (required-field req :description)
                      :source_url           (required-field req :src_url :src_upload_file)
                      :doc_url              (required-field req :documentation_url)
                      :version              (required-field req :version)
                      :attribution          (:attribution req)
                      :multithreaded        (multithreaded-str-to-flag (:multithreaded req))
                      :test_data_path       (required-field req :test_data_file)
                      :instructions         (required-field req :cmd_line)
                      :additional_info      (:additional_info req)
                      :additional_data_file (:additional_data_file req)
                      :requestor_id         user-id
                      :tool_architecture_id architecture-id})))

    (insert tool_request_statuses
           (values {:tool_request_id             (tool-request-subselect uuid)
                    :tool_request_status_code_id (status-code-subselect status-submitted)
                    :updater_id                  user-id}))))

(def ^:private valid-status-transitions
  #{[status-submitted    status-submitted]
    [status-submitted    status-failed]
    [status-submitted    status-evaluation]
    [status-submitted    status-pending]
    [status-evaluation   status-evaluation]
    [status-evaluation   status-failed]
    [status-evaluation   status-installation]
    [status-evaluation   status-pending]
    [status-installation status-installation]
    [status-installation status-failed]
    [status-installation status-pending]
    [status-installation status-completion]
    [status-pending      status-submitted]
    [status-pending      status-evaluation]
    [status-pending      status-installation]})

(defn- valid-status-transition?
  "Determines if a status transition is valid."
  [old-status new-status]
  (contains? valid-status-transitions [old-status new-status]))

(defn- get-tool-req
  "Loads a tool request from the database."
  [uuid]
  (let [req (first (select tool_requests (where {:uuid uuid})))]
    (when (nil? req)
      (throw+ {:code ::tool_request_not_found
               :uuid (string/upper-case (.toString uuid))}))
    req))

(defn- get-most-recent-status
  "Gets the most recent status for a tool request."
  [uuid]
  (let [status ((comp :name first)
                (select [:tool_requests :tr]
                        (fields :trsc.name)
                        (join [:tool_request_statuses :trs]
                              {:tr.id :trs.tool_request_id})
                        (join [:tool_request_status_codes :trsc]
                              {:trs.tool_request_status_code_id :trsc.id})
                        (where {:tr.uuid uuid})
                        (order :trs.date_assigned :DESC)
                        (limit 1)))]
    (when (nil? status)
      (throw+ {:code ::no_status_found_for_tool_request
               :uuid (string/upper-case (.toString uuid))}))
    status))

(defn load-status-code
  "Gets the status code for a status code name."
  [status-code]
  (let [status (first (select tool_request_status_codes (where {:name status-code})))]
    (when (nil? status)
      (throw+ {:code   ::unrecognized_status_code
               :status status-code}))
    status))

(defn- validate-status-transition
  "Validates a transition from one status code to another."
  [old-status new-status]
  (when-not (valid-status-transition? old-status new-status)
    (throw+ {:code       ::invalild_tool_request_status_transition
             :old_status old-status
             :new_status new-status})))

(defn- handle-tool-request-update
  "Updates a tool request."
  [update]
  (transaction
   (let [uuid        (UUID/fromString (required-field update :uuid))
         req-id      (:id (get-tool-req uuid))
         prev-status (get-most-recent-status uuid)
         status      (:status update prev-status)
         status-id   (:id (load-status-code status))
         _           (validate-status-transition prev-status status)
         user-id     (get-user-id (required-field update :username))
         comments    (:comments update)
         comments    (when-not (string/blank? comments) comments)]
     (insert tool_request_statuses
             (values {:tool_request_id             req-id
                      :tool_request_status_code_id status-id
                      :updater_id                  user-id
                      :comments                    comments})))))

(defn submit-tool-request
  "Submits a tool request on behalf of a user."
  [username body]
  (handle-new-tool-request username (cheshire/decode body true))
  (success-response))

(defn update-tool-request
  "Updates the status of a tool request."
  [body]
  (handle-tool-request-update (cheshire/decode body true))
  (success-response))
