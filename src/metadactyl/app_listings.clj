(ns metadactyl.app-listings
  (:use [clojure.data.json :only [read-json json-str]]
        [slingshot.slingshot :only [try+ throw+]]
        [korma.core]
        [kameleon.core]
        [kameleon.entities]
        [kameleon.app-groups]
        [kameleon.app-listing]
        [metadactyl.metadactyl :only [current-user]]
        [metadactyl.workspace]
        [metadactyl.config]
        [metadactyl.util.conversions :only [to-long date->long]]))

(defn- add-subgroups
  [group groups]
  (let [subgroups (filter #(= (:hid group) (:parent_hid %)) groups)
        subgroups (map #(add-subgroups % groups) subgroups)
        result    (if (empty? subgroups) group (assoc group :groups subgroups))
        result    (assoc result :template_count (:app_count group))
        result    (dissoc result :app_count :parent_hid :hid)]
    result))

(defn- format-app-group-hierarchy
  "Formats the app group hierarchy rooted at the app group with the given
   identifier."
  [root-id]
  (let [groups (get-app-group-hierarchy root-id)
        root   (first (filter #(= root-id (:hid %)) groups))]
    (add-subgroups root groups)))

(defn get-only-app-groups
  "Retrieves the list of app groups that are visible to the user with the given
   workspace ID."
  [workspace-id]
  (let [workspace-id    (to-long workspace-id)
        root-app-groups (get-visible-root-app-group-ids workspace-id)]
    (json-str {:groups (map format-app-group-hierarchy root-app-groups)})))

(defn- validate-app-pipeline-eligibility
  "Validates an App for pipeline eligibility, throwing a slingshot stone ."
  [app]
  (let [app_id (:id app)
        step_count (:step_count app)
        overall_job_type (:overall_job_type app)]
    (if (< step_count 1)
           (throw+ {:reason
                    (str "Analysis, "
                         app_id
                         ", has too few steps for a pipeline.")}))
    (if (> step_count 1)
           (throw+ {:reason
                    (str "Analysis, "
                         app_id
                         ", has too many steps for a pipeline.")}))
    (if (not (= overall_job_type "executable"))
           (throw+ {:reason
                    (str "Job type, "
                         overall_job_type
                         ", can't currently be included in a pipeline.")}))))

(defn- format-app-pipeline-eligibility
  "Validates an App for pipeline eligibility, reformatting its :step_count and
   :overall_job_type values, replacing them with a :pipeline_eligibility map"
  [app]
  (let [pipeline_eligibility (try+
                               (validate-app-pipeline-eligibility app)
                               {:is_valid true
                                :reason ""}
                               (catch map? {:keys [reason]}
                                 {:is_valid false
                                  :reason reason}))
        app (dissoc app :step_count :overall_job_type)]
    (assoc app :pipeline_eligibility pipeline_eligibility)))

(defn- format-app-ratings
  "Formats an App's :average_rating, :user_rating, and :comment_id values into a
   :rating map."
  [app]
  (let [average_rating (:average_rating app)
        user_rating (:user_rating app)
        comment_id (:comment_id app)
        rating (if (not (or (nil? user_rating) (nil? comment_id)))
                 {:average average_rating
                  :user user_rating
                  :comment_id comment_id}
                 {:average average_rating})
        app (dissoc app :average_rating :user_rating :comment_id)]
    (assoc app :rating rating)))

(defn- format-app-timestamps
  ""
  [app]
  (let [edited_date (date->long (:edited_date app))
        integration_date (date->long (:integration_date app))]
    (assoc app :edited_date edited_date :integration_date integration_date)))

(defn list-apps-in-group
  "This service lists all of the apps in an app group and all of its
   descendents."
  [app_group_id params]
  (let [workspace (get-or-create-workspace (.getUsername current-user))
        app_group (get-app-group app_group_id)
        total (count-apps-in-group-for-user app_group_id)
        apps_in_group (get-apps-in-group-for-user
                        app_group_id
                        workspace
                        (workspace-favorites-app-group-index)
                        params)
        apps_in_group (map #(-> %
                              (format-app-timestamps)
                              (format-app-ratings)
                              (format-app-pipeline-eligibility))
                           apps_in_group)]
    (json-str (assoc app_group
                     :template_count total
                     :templates apps_in_group))))
