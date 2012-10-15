(ns metadactyl.core
  (:gen-class)
  (:use [clojure.java.io :only [file]]
        [clojure-commons.query-params :only (wrap-query-params)]
        [compojure.core]
        [metadactyl.app-categorization]
        [metadactyl.app-listings]
        [metadactyl.beans]
        [metadactyl.collaborators]
        [metadactyl.config]
        [metadactyl.kormadb]
        [metadactyl.metadactyl]
        [metadactyl.service]
        [ring.middleware keyword-params nested-params])
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [clojure.tools.logging :as log]
            [clojure-commons.clavin-client :as cl]
            [clojure-commons.props :as cp]
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

  (GET "/copy-template/:app-id" [app-id]
       (copy-app app-id))

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

  (GET "/get-property-values/:job-id" [job-id]
       (trap #(get-property-values job-id)))

  (GET "/get-app-description/:app-id" [app-id]
       (trap #(get-app-description app-id)))

  (context "/secured" [:as {params :params}]
           (store-current-user secured-routes params))

  (route/not-found (unrecognized-path-response)))

(defn- log-props
  "Logs the configuration properties."
  []
  (dorun (map #(log/warn (key %) "=" (val %))
              (sort-by key @props))))

(defn- init-service
  "Initializes the service."
  []
  (log-props)
  (init-registered-beans)
  (when (not (configuration-valid))
    (log/warn "THE CONFIGURATION IS INVALID - EXITING NOW")
    (System/exit 1))
  (define-database))

(defn load-configuration-from-props
  "Loads the configuration from a properties file."
  []
  (let [filename "metadactyl.properties"
        conf-dir (System/getenv "IPLANT_CONF_DIR")]
    (if (nil? conf-dir)
      (reset! props (cp/read-properties (file filename)))
      (reset! props (cp/read-properties (file conf-dir filename)))))
  (init-service))

(defn load-configuration-from-zookeeper
  "Loads the configuration properties from Zookeeper."
  []
  (cl/with-zk
    (zk-url)
    (when (not (cl/can-run?))
      (log/warn "THIS APPLICATION CANNOT RUN ON THIS MACHINE. SO SAYETH ZOOKEEPER.")
      (log/warn "THIS APPLICATION WILL NOT EXECUTE CORRECTLY.")
      (System/exit 1))
    (reset! props (cl/properties "metadactyl")))
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
  (load-configuration-from-zookeeper)
  (log/warn "Listening on" (listen-port))
  (jetty/run-jetty app {:port (listen-port)}))
