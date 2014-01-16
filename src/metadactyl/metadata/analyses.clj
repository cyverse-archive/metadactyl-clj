(ns metadactyl.metadata.analyses
  (:use [clj-time.core :only [default-time-zone]]
        [clj-time.format :only [formatter parse]]
        [kameleon.entities :only [transformation_activity workspace]]
        [korma.core]
        [metadactyl.util.config :only [osm-base-url osm-jobs-bucket]]
        [metadactyl.util.conversions :only [to-long]]
        [slingshot.slingshot :only [throw+]])
  (:require [cheshire.core :as cheshire]
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
  {:id               (:uuid state)
   :name             (first (remove string/blank? [(:display_name state) (:name state)]))
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
                     (fields :id :description :wikiurl :disabled)
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
          (#(cheshire/decode % true))
          :objects
          (map f))))

(defn- add-extra-app-fields
  "Adds extra fields from the app metadata in the database to the analysis
   listing."
  [app-fields {app-id :analysis_id :as analysis}]
  (if-let [app (app-fields app-id)]
    (assoc analysis
      :analysis_details (or (:description app) "")
      :wiki_url         (or (:wikiurl app) "")
      :app_disabled     (or (:disabled app) false))
    (do (log-missing-app app-id)
        analysis)))

(defn- analysis-contains-filter?
  "Returns true if the filter value is contained in the value of the analysis
   field that matches the filter field. The comparison is case-insensitive."
  [{:keys [field value]} analysis]
  (let [analysis-value (.toLowerCase ((keyword field) analysis))
        filter-value (.toLowerCase value)]
    (.contains analysis-value filter-value)))

(defn- analysis-matches-filters?
  "Returns a non-nil value if one of the analysis fields contains the value in
   the corresponding field of one of the given filters."
  [filters analysis]
  (some
    #(analysis-contains-filter? % analysis)
    filters))

(defn- filter-analyses
  "Filters analyses according to a filter specification."
  [filt analyses]
  (if-not (nil? filt)
    (let [filt (cheshire/decode filt true)]
      (filter #(analysis-matches-filters? filt %) analyses))
    analyses))

(defn- get-sort-fn
  "Obtains the sort function to use for the specified sort order."
  [sort-order]
  (condp contains? sort-order
    #{:desc :DESC} (comp - compare)
    #{:asc  :ASC}  compare
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
        total      (count analyses)
        analyses   (if (> offset 0) (drop offset analyses) analyses)
        analyses   (if (> limit 0) (take limit analyses) analyses)
        app-fields (load-app-fields (set (map :analysis_id analyses)))]
    {:analyses  (map (partial add-extra-app-fields app-fields) analyses)
     :total total}))

(defn get-selected-analyses
  "Retrieves information about selected analyses."
  [workspace-id ids]
  (validate-workspace-id workspace-id)
  (let [query      (analysis-query workspace-id ids)
        analyses   (load-analyses query analysis-from-object)
        app-fields (load-app-fields (set (map :analysis_id analyses)))]
    (map (partial add-extra-app-fields app-fields) analyses)))
