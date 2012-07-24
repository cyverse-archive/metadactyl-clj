(ns metadactyl.app-listings
  (:use [clojure.data.json :only [read-json json-str]]
        [korma.core]
        [kameleon.core]
        [kameleon.entities]
        [kameleon.app-groups]
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
