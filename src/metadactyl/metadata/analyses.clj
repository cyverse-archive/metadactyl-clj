(ns metadactyl.metadata.analyses
  (:use [clj-time.core :only [default-time-zone]]
        [clj-time.format :only [formatter parse]]
        [kameleon.entities :only [transformation_activity workspace]]
        [korma.core]
        [metadactyl.config :only [osm-base-url osm-jobs-bucket]]
        [metadactyl.util.conversions :only [to-long]]
        [slingshot.slingshot :only [throw+]])
  (:require [clojure.data.json :as json]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [clojure-commons.osm :as osm]))

(defn- validate-workspace-id
  "Verifies that the given workspace ID exists."
  [workspace-id]
  (when (empty? (select workspace (where {:id workspace-id})))
    (throw+ {:type         ::invalid_workspace_id
             :workspace_id workspace-id})))

(def ^:private create-osm-client
  "Creates and returns the OSM client used to retrieve analysis information."
  (memoize
   (fn []
     (osm/create (osm-base-url) (osm-jobs-bucket)))))

(def ^:private accepted-timestamp-formats
  "The timestamp formats accepted by this service."
  ["EEE MMM dd YYYY HH:mm:ss 'GMT'Z" "YYYY MMM dd HH:mm:ss"])

(def ^:private timestamp-parser
  "Creates and returns the timestamp parser used to format timestamps."
  (memoize
   (fn []
     (apply formatter (default-time-zone) accepted-timestamp-formats))))

(defn- strip-zone-name
  "Strips the time zone name from a timestamp."
  [timestamp]
  (string/replace timestamp #"\s*\([^\)]*\)$" ""))

(defn- parse-timestamp
  "Parses a timestamp in one of the accepted formats."
  [timestamp]
  (.getMillis (parse (timestamp-parser) (strip-zone-name timestamp))))

(defn- format-timestamp
  "Formats a timestamp for inclusion in the analysis listing.  The timestamps
   that are returned should be a string representing the number of milliseconds
   since the Unix epoch."
  [timestamp]
  (cond (nil? timestamp)                 0
        (= timestamp "")                 0
        (number? timestamp)              timestamp
        (re-matches #"[0-9]+" timestamp) (Long/parseLong timestamp)
        :else                            (parse-timestamp timestamp)))

(defn- analysis-from-state
  "Converts a job state map to an analysis listing map."
  [state]
  {:id               (:uuid state nil)
   :name             (:name state nil)
   :startdate        (str (format-timestamp (:submission_date state "")))
   :enddate          (str (format-timestamp (:completion_date state "")))
   :analysis_id      (:analysis_id state "")
   :analysis_name    (:analysis_name state "")
   :analysis_details (:analysis_details state "")
   :wiki_url         (:wiki_url state "")
   :status           (:status state "")
   :description      (:description state "")
   :resultfolderid   (:output_dir state "")})

(def ^:private analysis-from-object
  "Converts a job status information object from the OSM to an analysis object
   in the format provided by the listing services."
  (comp analysis-from-state :state))

(defn- load-app-fields
  "Loads extra app fields for the given app ID from the database."
  [app-ids]
  (into {}
        (map #(vector (:id %) %)
             (select transformation_activity
                     (fields :id :description :wikiurl)
                     (where {:id [in app-ids]})))))

(defn- log-missing-app
  "Logs an error message for an undefined app ID."
  [app-id]
  (log/error "unable to add extra analysis fields from app: app ID" app-id
             "not found"))

(defn- analysis-query
  "Builds an OSM query that can be used to retrieve analyses."
  ([workspace-id]
     {:state.workspace_id (str workspace-id)
      :state.deleted      {:$exists false}})
  ([workspace-id ids]
     (assoc (analysis-query workspace-id)
       :state.uuid {:$in ids})))

(defn- id-only-analysis-query
  "Builds an OSM query that can be used to retrieve analyses by ID only."
  [ids]
  {:state.uuid {:$in ids}})

(defn- load-analyses
  "Retrieves information about analyses from teh OSM."
  ([query]
     (load-analyses query identity))
  ([query f]
     (->> query
          (osm/query (create-osm-client))
          json/read-json
          :objects
          (map f))))

(defn- add-extra-app-fields
  "Adds extra fields from the app metadata in the database to the analysis
   listing."
  [app-fields {app-id :analysis_id :as analysis}]
  (if-let [app (app-fields app-id)]
    (assoc analysis
      :analysis_details (or (:description app) "")
      :wiki_url         (or (:wikiurl app) ""))
    (do (log-missing-app app-id)
        analysis)))

(defn- filter-analyses
  "Filters analyses according to a filter specification."
  [filt analyses]
  (if-not (nil? filt)
    (let [[k v] (string/split filt #"=" 2)
          k     (keyword k)]
      (filter #(= (k %) v) analyses))
    analyses))

(defn- get-sort-fn
  "Obtains the sort function to use for the specified sort order."
  [sort-order]
  (condp = sort-order
    :desc (comp - compare)
    :asc  compare
    (throw+ {:type       ::invalid-sort-order
             :sort-order sort-order})))

(defn get-analyses-for-workspace-id
  "Retrieves information about the analyses that were submitted by the user with
   the given workspace ID."
  [workspace-id {:keys [limit offset filter sort-field sort-order]
                 :or   {limit      0
                        offset     0
                        sort-field :startdate
                        sort-order :desc}}]
  (validate-workspace-id workspace-id)
  (let [limit      (if (string? limit) (to-long limit) limit)
        offset     (if (string? offset) (to-long offset) offset)
        sort-field (keyword sort-field)
        sort-fn    (get-sort-fn (keyword sort-order))
        query      (analysis-query workspace-id)
        analyses   (load-analyses query analysis-from-object)
        analyses   (sort-by sort-field sort-fn analyses)
        analyses   (filter-analyses filter analyses)
        analyses   (if (> offset 0) (drop offset analyses) analyses)
        analyses   (if (> limit 0) (take limit analyses) analyses)
        app-fields (load-app-fields (set (map :analysis_id analyses)))]
    (map (partial add-extra-app-fields app-fields) analyses)))

(defn get-selected-analyses
  "Retrieves information about selected analyses."
  [workspace-id ids]
  (validate-workspace-id workspace-id)
  (let [query      (analysis-query workspace-id ids)
        analyses   (load-analyses query analysis-from-object)
        app-fields (load-app-fields (set (map :analysis_id analyses)))]
    (map (partial add-extra-app-fields app-fields) analyses)))

(defn- delete-analysis
  "Deletes a single analysis in the OSM."
  [{osm-id :object_persistence_uuid
    state  :state}]
  (if (:deleted state false)
    (log/warn "job" (:uuid state) "is already deleted")
    ((comp clojure.pprint/pprint json/read-json)
     (osm/update-object (create-osm-client) osm-id
                        (assoc state :deleted true)))))

(defn- delete-analyses-for-job
  "Marks all analyses associated with a job and a workspace as deleted."
  [workspace-id analyses-by-job-id id]
  (let [workspace-id       (str workspace-id)
        get-workspace-id   #(get-in % [:state :workspace_id])
        right-workspace-id #(= workspace-id (get-workspace-id %))
        analyses           (filter right-workspace-id (analyses-by-job-id id))]
    (if (empty? analyses)
      (println "attempt to delete non-existent job" id "for workspace"
                workspace-id "ignored")
      (dorun (map delete-analysis analyses)))))

(defn delete-analyses
  "Marks analyses as deleted, provided that they exist and are associated with
   the given workspace ID."
  [workspace-id ids]
  (validate-workspace-id workspace-id)
  (let [extract-fn #(select-keys % [:object_persistence_uuid :state])
        analyses   (load-analyses (id-only-analysis-query ids) extract-fn)
        analyses   (group-by #(get-in % [:state :uuid]) analyses)]
    (dorun (map (partial delete-analyses-for-job workspace-id analyses) ids))))
