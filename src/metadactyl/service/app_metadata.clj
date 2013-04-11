(ns metadactyl.service.app-metadata
  "DE app metadata services."
  (:use [metadactyl.util.service :only [success-response]]
        [korma.db :only [transaction]]
        [slingshot.slingshot :only [throw+]])
  (:require [cheshire.core :as cheshire]
            [metadactyl.persistence.app-metadata :as amp]))

(defn relabel-app
  "This service allows labels to be updated in any app, whether or not the app has been submitted
   for public use."
  [body]
  (transaction
   (let [req (cheshire/decode body :keywordize)]
     (amp/update-app-labels req (:hid (amp/get-app (:id req)))))))
