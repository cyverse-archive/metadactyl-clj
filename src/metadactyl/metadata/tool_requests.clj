(ns metadactyl.metadata.tool-requests
  (:use [kameleon.entities]
        [korma.core]
        [korma.db]
        [slingshot.slingshot :only [throw+]])
  (:require [cheshire.core :as cheshire]
            [clojure.string :as string]))

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

(defn- username-to-user-id
  "Gets the internal user ID for a username."
  [username]
  (let [id (:id (first (select users (where {:username username}))))]
    (when (nil? id)
      (throw+ {:code     ::user_id_not_found
               :username username}))
    id))

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

(defn handle-new-tool-request
  "Submits a tool request on behalf of the authenticated user."
  [username req]
  (let [requestor-id        (username-to-user-id username)
        phone               (:phone req)
        tool-name           (required-field req :name)
        desc                (required-field req :description)
        src-url             (required-field req :src_url :src_upload_file)
        doc-url             (required-field req :documentation_url)
        version             (required-field req :version)
        attribution         (:attribution req)
        multithreaded?      (multithreaded-str-to-flag (:multithreaded req))
        architecture-id     (architecture-name-to-id (required-field req :architecture))
        test-data-url       (required-field req :test_data_file)
        instructions        (required-field req :cmd_line)
        additional-info     (:additional_info req)
        additional-data-url (:additional_data_file req)]
    (transaction
     (let [id (:id (insert tool_requests
                           (values {:phone                phone
                                    :tool_name            tool-name
                                    :description          desc
                                    :source_url           src-url
                                    :doc_url              doc-url
                                    :version              version
                                    :attribution          attribution
                                    :multithreaded        multithreaded?
                                    :test_data_path       test-data-url
                                    :instructions         instructions
                                    :additional_info      additional-info
                                    :additional_data_file additional-data-url
                                    :requestor_id         requestor-id
                                    :tool_architecture_id architecture-id})))]
       (insert tool_request_statuses
               (values {:tool_request_id             id
                        :tool_request_status_code_id (status-code-subselect status-submitted)}))))))

(defn submit-tool-request
  "Submits a tool request on behalf of the authenticated user."
  [username body]
  (handle-new-tool-request username (cheshire/decode body true)))