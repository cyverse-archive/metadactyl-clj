(ns metadactyl.core
  (:gen-class)
  (:use [clojure.java.io :only [file]]
        [clojure-commons.query-params :only [wrap-query-params]]
        [compojure.core]
        [metadactyl.app-categorization]
        [metadactyl.app-listings]
        [metadactyl.beans]
        [metadactyl.collaborators]
        [metadactyl.kormadb]
        [metadactyl.metadactyl]
        [metadactyl.metadata.tool-requests]
        [metadactyl.service.app-metadata :only [relabel-app]]
        [metadactyl.util.service]
        [metadactyl.zoidberg]
        [ring.middleware keyword-params nested-params])
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [clojure.tools.logging :as log]
            [clojure-commons.clavin-client :as cl]
            [clojure-commons.error-codes :as ce]
            [metadactyl.config :as config]
            [ring.adapter.jetty :as jetty]))

(defroutes secured-routes
  (GET "/bootstrap" []
       (bootstrap))

  (GET "/template/:app-id" [app-id]
       (get-app app-id))

  (PUT "/workspaces/:workspace-id/newexperiment" [workspace-id :as {body :body}]
       (run-experiment body workspace-id))

  (GET "/workspaces/:workspace-id/executions/list"
       [workspace-id :as {params :params}]
       (get-experiments workspace-id params))

  (POST "/workspaces/:workspace-id/executions/list"
        [workspace-id :as {body :body}]
        (get-selected-experiments workspace-id body))

  (PUT "/workspaces/:workspace-id/executions/delete"
       [workspace-id :as {body :body}]
       (delete-experiments body workspace-id))

  (POST "/rate-analysis" [:as {body :body}]
        (rate-app body))

  (POST "/delete-rating" [:as {body :body}]
        (delete-rating body))

  (GET "/search-analyses" [:as {params :params}]
       (search-apps params))

  (GET "/get-analyses-in-group/:app-group-id"
       [app-group-id :as {params :params}]
       (list-apps-in-group app-group-id params))

  (GET "/get-components-in-analysis/:app-id" [app-id]
       (list-deployed-components-in-app app-id))

  (POST "/update-favorites" [:as {body :body}]
        (update-favorites body))

  (GET "/edit-template/:app-id" [app-id]
       (edit-app app-id))

  (GET "/edit-workflow/:app-id" [app-id]
       (edit-workflow app-id))

  (GET "/copy-template/:app-id" [app-id]
       (copy-app app-id))

  (GET "/copy-workflow/:app-id" [app-id]
       (copy-workflow app-id))

  (PUT "/update-template" [:as {body :body}]
       (trap #(update-template-secured body)))

  (PUT "/update-app" [:as {body :body}]
       (ce/trap "update-app" #(update-app-secured body)))

  (POST "/make-analysis-public" [:as {body :body}]
        (make-app-public body))

  (GET "/collaborators" [:as {params :params}]
       (get-collaborators params))

  (POST "/collaborators" [:as {params :params body :body}]
        (add-collaborators params (slurp body)))

  (POST "/remove-collaborators" [:as {params :params body :body}]
        (remove-collaborators params (slurp body)))

  (GET "/reference-genomes" []
       (list-reference-genomes))

  (PUT "/reference-genomes" [:as {body :body}]
       (replace-reference-genomes (slurp body)))

  (PUT "/tool-request" [:as {body :body}]
       (submit-tool-request (.getUsername current-user) body))

  (POST "/tool-request" [:as {body :body}]
        (update-tool-request (config/uid-domain) (.getUsername current-user) body))

  (GET "/tool-requests" [:as {:keys [params]}]
       (list-tool-requests (.getUsername current-user) params))

  (route/not-found (unrecognized-path-response)))

(defroutes metadactyl-routes
  (GET "/" []
       "Welcome to Metadactyl!\n")

  (GET "/get-workflow-elements/:element-type" [element-type :as {params :params}]
       (trap #(get-workflow-elements element-type params)))

  (GET "/search-deployed-components/:search-term" [search-term]
       (trap #(search-deployed-components search-term)))

  (GET "/get-all-analysis-ids" []
       (trap #(get-all-app-ids)))

  (POST "/delete-categories" [:as {body :body}]
        (trap #(delete-categories body)))

  (GET "/validate-analysis-for-pipelines/:app-id" [app-id]
       (trap #(validate-app-for-pipelines app-id)))

  (GET "/analysis-data-objects/:app-id" [app-id]
       (trap #(get-data-objects-for-app app-id)))

  (POST "/categorize-analyses" [:as {body :body}]
        (trap #(categorize-apps body)))

  (GET "/get-analysis-categories/:category-set" [category-set]
       (trap #(get-app-categories category-set)))

  (POST "/can-export-analysis" [:as {body :body}]
        (trap #(can-export-app body)))

  (POST "/add-analysis-to-group" [:as {body :body}]
        (trap #(add-app-to-group body)))

  (GET "/get-analysis/:app-id" [app-id]
       (trap #(get-app app-id)))

  (GET "/analysis-details/:app-id" [app-id]
       (trap #(get-app-details app-id)))

  (GET "/get-only-analysis-groups/:workspace-id" [workspace-id]
       (trap #(get-only-app-groups workspace-id)))

  (GET "/list-analysis/:app-id" [app-id]
       (list-app app-id))

  (GET "/export-template/:template-id" [template-id]
       (trap #(export-template template-id)))

  (GET "/export-workflow/:app-id" [app-id]
       (trap #(export-workflow app-id)))

  (POST "/export-deployed-components" [:as {body :body}]
        (trap #(export-deployed-components body)))

  (POST "/permanently-delete-workflow" [:as {body :body}]
        (trap #(permanently-delete-workflow body)))

  (POST "/delete-workflow" [:as {body :body}]
        (trap #(delete-workflow body)))

  (POST "/preview-template" [:as {body :body}]
        (trap #(preview-template body)))

  (POST "/preview-workflow" [:as {body :body}]
        (trap #(preview-workflow body)))

  (POST "/update-template" [:as {body :body}]
        (trap #(update-template body)))

  (POST "/force-update-workflow" [:as {body :body params :params}]
        (trap #(force-update-workflow body params)))

  (POST "/update-workflow" [:as {body :body}]
        (trap #(update-workflow body)))

  (POST "/import-template" [:as {body :body}]
        (trap #(import-template body)))

  (POST "/import-workflow" [:as {body :body}]
        (trap #(import-workflow body)))

  (POST "/import-tools" [:as {body :body}]
        (trap #(import-tools body)))

  (POST "/update-analysis" [:as {body :body}]
        (trap #(update-app body)))

  (POST "/update-app-labels" [:as {body :body}]
        (ce/trap "update-app-labels" #(relabel-app body)))

  (GET "/get-property-values/:job-id" [job-id]
       (trap #(get-property-values job-id)))

  (GET "/analysis-rerun-info/:job-id" [job-id]
       (trap #(get-app-rerun-info job-id)))

  (GET "/get-app-description/:app-id" [app-id]
       (trap #(get-app-description app-id)))

  (POST "/tool-request" [:as {body :body}]
        (trap #(update-tool-request (config/uid-domain) body)))

  (GET "/tool-request/:uuid" [uuid]
       (trap #(get-tool-request uuid)))

  (context "/secured" [:as {params :params}]
           (store-current-user secured-routes params))

  (route/not-found (unrecognized-path-response)))

(defn- init-service
  "Initializes the service."
  []
  (init-registered-beans)
  (define-database))

(defn load-config-from-file
  "Loads the configuration settings from a properties file."
  []
  (config/load-config-from-file)
  (init-service))

(defn load-config-from-zookeeper
  "Loads the configuration settings from zookeeper."
  []
  (config/load-config-from-zookeeper)
  (init-service))

(defn site-handler [routes]
  (-> routes
      wrap-keyword-params
      wrap-nested-params
      wrap-query-params))

(def app
  (site-handler metadactyl-routes))

(defn -main
  [& args]
  (load-config-from-zookeeper)
  (log/warn "Listening on" (config/listen-port))
  (jetty/run-jetty app {:port (config/listen-port)}))
