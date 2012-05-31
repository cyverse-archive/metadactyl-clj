(ns metadactyl.collaborators
  (:use [clojure.data.json :only [read-json]]
        [korma.db :only [transaction]]
        [metadactyl.config :only [uid-domain]]
        [metadactyl.service :only [success-response]])
  (:require [clojure.string :as string]
            [kameleon.queries :as queries]))

(defn- add-domain
  "Adds the username domain to a username."
  [username]
  (str username "@" (uid-domain)))

(defn- remove-domain
  "Removes the username domain from a username."
  [username]
  (string/replace username #"@.*" ""))

(defn get-collaborators
  "Gets the list of collaborators for the current user."
  [{:keys [uid]}]
  (let [collaborators (queries/get-collaborators (add-domain uid))]
    (success-response {:users (map remove-domain collaborators)})))

(defn add-collaborators
  "Adds collaborators for the current user."
  [{:keys [uid]} body]
  (transaction
   (let [collaborators (:users (read-json body))]
     (queries/add-collaborators (add-domain uid) (map add-domain collaborators))
     (success-response))))

(defn remove-collaborators
  "Removes collaborators for the current user."
  [{:keys [uid]} body]
  (transaction
   (let [collaborators (:users (read-json body))]
     (queries/remove-collaborators (add-domain uid)
                                   (map add-domain collaborators))
     (success-response))))
