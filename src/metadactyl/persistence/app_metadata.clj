(ns metadactyl.persistence.app-metadata
  "Persistence layer for app metadata."
  (:use [kameleon.entities]
        [korma.core]
        [metadactyl.util.assertions])
  (:require [metadactyl.persistence.app-metadata.relabel :as relabel]
            [metadactyl.persistence.app-metadata.delete :as delete]))

(defn get-app
  "Retrieves an app from the database."
  [app-id]
  (assert-not-nil
   [:app-id app-id]
   (first (select transformation_activity
                  (where {:id app-id})))))

(defn update-app-labels
  "Updates the labels in an app."
  [req app-hid]
  (relabel/update-app-labels req app-hid))

(defn permanently-delete-app
  "Permanently removes an app from the metadata database."
  [app-id]
  (delete/permanently-delete-app ((comp :hid get-app) app-id)))
