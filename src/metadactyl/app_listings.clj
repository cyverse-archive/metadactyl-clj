(ns metadactyl.app-listings
  (:use [clojure.data.json :only [read-json json-str]]
        [korma.core]
        [kameleon.core]
        [kameleon.entities]
        [metadactyl.conversions :only [to-long]]))

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
  (let [groups (select (sqlfn :analysis_group_hierarchy root-id))
        root   (first (filter #(= root-id (:hid %)) groups))]
    (add-subgroups root groups)))

(defn- get-root-app-group-ids
  "Gets the internal identifiers for all app groups associated with workspaces
   that satisfy the given condition."
  [condition]
  (map :app_group_id
       (select workspace
               (fields [:root_analysis_group_id :app_group_id])
               (where condition))))

(defn- get-visible-root-app-group-ids
  "Gets the list of internal root app group identifiers that are visible to the
   user with the given workspace identifier."
  [workspace-id]
  (concat (get-root-app-group-ids {:id workspace-id})
          (get-root-app-group-ids {:is_public true})))

(defn get-only-app-groups
  "Retrieves the list of app groups that are visible to the user with the given
   workspace ID."
  [workspace-id]
  (let [workspace-id    (to-long workspace-id)
        root-app-groups (get-visible-root-app-group-ids workspace-id)]
    (json-str {:groups (map format-app-group-hierarchy root-app-groups)})))
