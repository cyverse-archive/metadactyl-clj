(ns metadactyl.collaborators
  (:use [clojure.data.json :only [read-json]]
        [metadactyl.config :only [uid-domain]]
        [metadactyl.service :only [success-response]])
  (:require [clojure.string :as string]
            [kameleon.queries :as queries]))

(defn get-collaborators
  "Gets the list of collaborators for the current user."
  [{:keys [uid]}]
  (let [collaborators (queries/get-collaborators (str uid "@" (uid-domain)))]
    (success-response {:users (map #(string/replace % #"@.*" "")
                                   collaborators)})))

(defn add-collaborators
  "Adds collaborators for the current user."
  [{:keys [uid]} body]
  (let [collaborators (:users (read-json body))]
    (queries/add-collaborators (str uid "@" (uid-domain))
                               (map #(str % "@" (uid-domain)) collaborators))
    (success-response)))
