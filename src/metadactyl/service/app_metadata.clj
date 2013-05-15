(ns metadactyl.service.app-metadata
  "DE app metadata services."
  (:use [clojure.java.io :only [reader]]
        [metadactyl.util.service :only [build-url success-response parse-json]]
        [korma.db :only [transaction]]
        [slingshot.slingshot :only [try+ throw+]])
  (:require [cheshire.core :as cheshire]
            [clj-http.client :as client]
            [clojure.tools.logging :as log]
            [metadactyl.persistence.app-metadata :as amp]
            [metadactyl.translations.app-metadata :as atx]
            [metadactyl.util.config :as config]))

(defn relabel-app
  "This service allows labels to be updated in any app, whether or not the app has been submitted
   for public use."
  [body]
  (let [req (parse-json body)]
    (transaction (amp/update-app-labels req (:hid (amp/get-app (:id req)))))
    (success-response)))

(defn preview-command-line
  "This service sends a command-line preview request to the JEX."
  [body]
  (let [in-req  (parse-json body)
        jex-req (atx/template-cli-preview-req in-req)]
    (cheshire/decode-stream
     ((comp reader :body)
      (client/post
       (build-url (config/jex-base-url) "arg-preview")
       {:body             (cheshire/encode jex-req)
        :content-type     :json
        :as               :stream}))
     true)))
