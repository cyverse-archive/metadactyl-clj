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
   :startdate        (format-timestamp (:submission_date state ""))
   :enddate          (format-timestamp (:completion_date state ""))
   :analysis_id      (:analysis_id state "")
   :analysis_name    (:analysis_name state "")
   :analysis_details (:analysis_details state "")
   :wiki_url         (:wiki_url state "")
   :status           (:status state "")
   :description      (:description state "")
   :resultfolderid   (:output_dir state "")})

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

(defn- get-analyses
  "Retrieves information about the analyses associated with the given workspace
   ID from the OSM."
  [workspace-id]
  (let [osm-client (create-osm-client)]
    (map (comp analysis-from-state :state)
         (:objects (json/read-json
                    (osm/query osm-client
                               {:state.workspace_id (str workspace-id)
                                :state.deleted      {:$exists false}}))))))

(defn- selected-analyses
  "Retrieves information about the analyses with the given identifiers provided
   that they're associated with the given workspace ID."
  [workspace-id ids]
  (let [osm-client (create-osm-client)]
    (map (comp analysis-from-state :state)
         (:objects (json/read-json
                    (osm/query osm-client
                               {:state.workspace_id (str workspace-id)
                                :state.deleted      {:$exists false}
                                :state.uuid         {:$in ids}}))))))

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
        analyses   (sort-by sort-field sort-fn (get-analyses workspace-id))
        analyses   (filter-analyses filter analyses)
        analyses   (if (> offset 0) (drop offset analyses) analyses)
        analyses   (if (> limit 0) (take limit analyses) analyses)
        app-fields (load-app-fields (set (map :analysis_id analyses)))]
    (map (partial add-extra-app-fields app-fields) analyses)))

(defn get-selected-analyses
  "Retrieves information about selected analyses."
  [workspace-id ids]
  (validate-workspace-id workspace-id)
  (let [analyses   (selected-analyses workspace-id ids)
        app-fields (load-app-fields (set (map :analysis_id analyses)))]
    (map (partial add-extra-app-fields app-fields) analyses)))
