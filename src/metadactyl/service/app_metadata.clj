(ns metadactyl.service.app-metadata
  "DE app metadata services."
  (:use [clojure.java.io :only [reader]]
        [metadactyl.util.service :only [success-response]]
        [korma.db :only [transaction]]
        [slingshot.slingshot :only [throw+]])
  (:require [cheshire.core :as cheshire]
            [clojure.tools.logging :as log]
            [metadactyl.persistence.app-metadata :as amp]))

(defn relabel-app
  "This service allows labels to be updated in any app, whether or not the app has been submitted
   for public use."
  [body]
  (let [req (cheshire/decode-stream (reader body) true)]
    (transaction (amp/update-app-labels req (:hid (amp/get-app (:id req)))))
    (success-response)))
