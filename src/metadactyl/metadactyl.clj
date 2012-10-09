(ns metadactyl.metadactyl
  (:use [clojure.data.json :only [read-json]]
        [slingshot.slingshot :only [throw+]]
        [metadactyl.app-validation]
        [metadactyl.beans]
        [metadactyl.config]
        [metadactyl.metadata.analyses
         :only [get-analyses-for-workspace-id get-selected-analyses]]
        [metadactyl.metadata.reference-genomes
         :only [get-reference-genomes put-reference-genomes]]
        [metadactyl.metadata.element-listings :only [list-elements]]
        [metadactyl.service]
        [metadactyl.transformers]
        [ring.util.codec :only [url-decode]])
  (:import [com.mchange.v2.c3p0 ComboPooledDataSource]
           [java.util HashMap]
           [org.iplantc.authn.service UserSessionService]
           [org.iplantc.authn.user User]
           [org.iplantc.workflow.client OsmClient ZoidbergClient]
           [org.iplantc.workflow HibernateTemplateFetcher]
           [org.iplantc.workflow.experiment
            AnalysisRetriever AnalysisService ExperimentRunner
            IrodsUrlAssembler]
           [org.iplantc.workflow.integration.validation
            ChainingTemplateValidator OutputRedirectionTemplateValidator
            TemplateValidator]
           [org.iplantc.workflow.service
            AnalysisCategorizationService AnalysisEditService CategoryService
            ExportService InjectableWorkspaceInitializer PipelineService
            TemplateGroupService UserService WorkflowElementRetrievalService
            WorkflowExportService AnalysisListingService WorkflowPreviewService
            WorkflowImportService AnalysisDeletionService RatingService
            WorkflowElementSearchService PropertyValueService]
           [org.iplantc.workflow.template.notifications NotificationAppender]
           [org.springframework.orm.hibernate3.annotation
            AnnotationSessionFactoryBean])
  (:require [clojure.tools.logging :as log]))

(defn- get-property-type-validator
  "Gets an implementation of the TemplateValidator interface that can be used
   to verify that the property types in the templates are all compatible with
   the selected deployed component."
  []
  (proxy [TemplateValidator] []
    (validate [template registry]
      (validate-template-property-types template registry))))

(def
  ^{:doc "The authenticated user or nil if the service is unsecured."
    :dynamic true}
   current-user nil)

(def
  ^{:doc "The service used to get information about the authenticated user."}
   user-session-service (proxy [UserSessionService] []
                          (getUser [] current-user)))

(defn- build-template-validator
  "Builds an object that will be used by the workflow import services
  to to validate incoming templates."
  []
  (doto (ChainingTemplateValidator.)
    (.addValidator (OutputRedirectionTemplateValidator. "stdout"))
    (.addValidator (OutputRedirectionTemplateValidator. "stderr"))
    (.addValidator (get-property-type-validator))))

(defn- user-from-attributes
  "Creates an instance of org.iplantc.authn.user.User from the given map."
  [user-attributes]
  (log/debug user-attributes)
  (let [uid (user-attributes :user)]
    (if (empty? uid)
      (throw+ {:type :metadactyl.service/unauthorized,
               :user user-attributes,
               :message "Invalid user credentials provided."}))
    (doto (User.)
      (.setUsername (str uid "@" (uid-domain)))
      (.setPassword (user-attributes :password))
      (.setEmail (user-attributes :email))
      (.setShortUsername uid))))

(defn store-current-user
  "Creates a function that takes a request, binds current-user to a new instance
   of org.iplantc.authn.user.User that is built from the user attributes found
   in the given params map, then passes request to the given handler."
  [handler params]
  (fn [request]
    (trap
      #(binding [current-user (user-from-attributes params)]
         (handler request)))))

(register-bean
  (defbean db-url
    "The URL to use when connecting to the database."
    (str "jdbc:" (db-subprotocol) "://" (db-host) ":" (db-port) "/" (db-name))))

(register-bean
  (defbean data-source
    "The data source used to obtain database connections."
    (doto (ComboPooledDataSource.)
      (.setDriverClass (db-driver-class))
      (.setJdbcUrl (db-url))
      (.setUser (db-user))
      (.setPassword (db-password)))))

(register-bean
  (defbean session-factory
    "A factory for generating Hibernate sessions."
    (.getObject
      (doto (AnnotationSessionFactoryBean.)
        (.setDataSource (data-source))
        (.setPackagesToScan (into-array String (hibernate-packages)))
        (.setMappingResources (into-array String (hibernate-resources)))
        (.setHibernateProperties (as-properties
                                   {"hibernate.dialect" (hibernate-dialect)
                                    "hibernate.hbm2ddl.auto" "validate"
                                    "hibernate.jdbc.batch-size" "50"}))
        (.afterPropertiesSet)))))

(register-bean
  (defbean workflow-element-service
    "Services used to obtain elements that are commonly shared by multiple
     apps in the metadata model (for example, property types)."
    (doto (WorkflowElementRetrievalService.)
      (.setSessionFactory (session-factory)))))

(register-bean
  (defbean workflow-element-search-service
    "Services used to search elements that are commonly shared by multiple
     apps in the metadata model (currently, only deployed components)."
    (doto (WorkflowElementSearchService.)
      (.setSessionFactory (session-factory)))))

(register-bean
  (defbean workflow-export-service
    "Services used to export apps and templates from the DE."
    (WorkflowExportService. (session-factory))))

(register-bean
  (defbean export-service
    "Services used to determine whether or not an ap can be exported."
    (doto (ExportService.)
      (.setSessionFactory (session-factory)))))

(register-bean
  (defbean category-service
    "Services used to manage app categories."
    (doto (CategoryService.)
      (.setSessionFactory (session-factory)))))

(register-bean
  (defbean pipeline-service
    "Services used to manage pipelines"
    (doto (PipelineService.)
      (.setSessionFactory (session-factory)))))

(register-bean
  (defbean zoidberg-client
    "The client used to communicate with Zoidberg services."
    (doto (ZoidbergClient.)
      (.setBaseUrl (zoidberg-base-url))
      (.setConnectionTimeout (zoidberg-connection-timeout))
      (.setEncoding (zoidberg-encoding)))))

(register-bean
  (defbean osm-job-request-client
    "The client used to communicate with OSM services."
    (doto (OsmClient.)
      (.setBaseUrl (osm-base-url))
      (.setBucket (osm-job-request-bucket))
      (.setConnectionTimeout (osm-connection-timeout))
      (.setEncoding (osm-encoding)))))

(register-bean
  (defbean user-service
    "Services used to obtain information about a user."
    (doto (UserService.)
      (.setSessionFactory (session-factory))
      (.setUserSessionService user-session-service)
      (.setRootAnalysisGroup (workspace-root-app-group))
      (.setDefaultAnalysisGroups (workspace-default-app-groups)))))

(register-bean
  (defbean workspace-initializer
    "A bean that can be used to initialize a user's workspace."
    (doto (InjectableWorkspaceInitializer.)
      (.setUserService (user-service)))))

(register-bean
  (defbean analysis-categorization-service
    "Services used to categorize apps."
    (doto (AnalysisCategorizationService.)
      (.setSessionFactory (session-factory))
      (.setDevAnalysisGroupIndex (workspace-dev-app-group-index))
      (.setFavoritesAnalysisGroupIndex (workspace-favorites-app-group-index))
      (.setWorkspaceInitializer (workspace-initializer)))))

(register-bean
  (defbean analysis-listing-service
    "Services used to list analyses."
    (doto (AnalysisListingService.)
      (.setSessionFactory (session-factory))
      (.setFavoritesAnalysisGroupIndex (workspace-favorites-app-group-index))
      (.setWorkspaceInitializer (workspace-initializer)))))

(register-bean
  (defbean template-group-service
    "Services used to place apps in app groups."
    (doto (TemplateGroupService.)
      (.setSessionFactory (session-factory))
      (.setZoidbergClient (zoidberg-client))
      (.setUserSessionService user-session-service))))

(register-bean
  (defbean workflow-preview-service
    "Handles workflow/metadactyl related previews."
    (WorkflowPreviewService. (session-factory))))

(register-bean
 (defbean workflow-import-service
   "Handles workflow/metadactyl import actions."
   (doto (WorkflowImportService.
          (session-factory)
          (Integer/toString (workspace-dev-app-group-index))
          (Integer/toString (workspace-favorites-app-group-index))
          (workspace-initializer))
     (.setTemplateValidator (build-template-validator)))))

(register-bean
  (defbean analysis-deletion-service
    "Handles workflow/metadactyl deletion actions."
    (doto (AnalysisDeletionService. (session-factory))
      (.setZoidbergClient (zoidberg-client)))))

(register-bean
  (defbean app-fetcher
    "Retrieves apps from the database."
    (doto (HibernateTemplateFetcher.)
      (.setSessionFactory (session-factory)))))

(register-bean
  (defbean notification-appender
    "Appends UI notifications to an app."
    (doto (NotificationAppender.)
      (.setSessionFactory (session-factory)))))

(register-bean
  (defbean analysis-edit-service
    "Services to make apps available for editing in Tito."
    (doto (AnalysisEditService.)
      (.setSessionFactory (session-factory))
      (.setZoidbergClient (zoidberg-client))
      (.setUserService (user-service))
      (.setWorkflowImportService (workflow-import-service)))))

(register-bean
  (defbean analysis-retriever
    "Used by several services to retrieve apps from the daatabase."
    (doto (AnalysisRetriever.)
      (.setSessionFactory (session-factory)))))

(register-bean
  (defbean rating-service
    "Services to associate user ratings with or remove user ratings from
     apps."
    (doto (RatingService.)
      (.setSessionFactory (session-factory))
      (.setUserSessionService user-session-service)
      (.setAnalysisRetriever (analysis-retriever)))))

(register-bean
  (defbean analysis-service
    "Services to retrieve information about analyses that a user has
     submitted."
    (doto (AnalysisService.)
      (.setSessionFactory (session-factory))
      (.setOsmBaseUrl (osm-base-url))
      (.setOsmBucket (osm-jobs-bucket))
      (.setConnectionTimeout (osm-connection-timeout)))))

(register-bean
  (defbean url-assembler
    "Used to assemble URLs."
    (IrodsUrlAssembler.)))

(register-bean
  (defbean experiment-runner
    "Services to submit jobs to the JEX for execution."
    (doto (ExperimentRunner.)
      (.setSessionFactory (session-factory))
      (.setUserService (user-service))
      (.setExecutionUrl (jex-base-url))
      (.setUrlAssembler (url-assembler))
      (.setJobRequestOsmClient (osm-job-request-client)))))

(register-bean
  (defbean property-value-service
    "Services to retrieve property values for jobs that have previously been
     submitted."
    (doto (PropertyValueService.)
      (.setSessionFactory (session-factory))
      (.setOsmClient (osm-job-request-client)))))

(defn get-workflow-elements
  "A service to get information about workflow elements."
  [element-type params]
  (let [handler-fn #(.getElements (workflow-element-service) element-type)
        listings   (list-elements element-type params handler-fn)]
    (success-response listings)))

(defn search-deployed-components
  "A service to search information about deployed components."
  [search-term]
  (.searchDeployedComponents (workflow-element-search-service) search-term))

(defn get-all-app-ids
  "A service to get the list of app identifiers."
  []
  (.getAnalysisIds (workflow-export-service)))

(defn delete-categories
  "A service used to delete app categories."
  [body]
  (.deleteCategories (category-service) (slurp body)))

(defn validate-app-for-pipelines
  "A service used to determine whether or not an app can be included in a
   pipeline."
  [app-id]
  (.validateAnalysisForPipelines (pipeline-service) app-id))

(defn get-data-objects-for-app
  "A service used to list the data objects in an app."
  [app-id]
  (.getDataObjectsForAnalysis (pipeline-service) app-id))

(defn get-app-categories
  "A service used to get a list of app categories."
  [category-set]
  (.getAnalysisCategories (analysis-categorization-service) category-set))

(defn can-export-app
  "A service used to determine whether or not an app can be exported to Tito."
  [body]
  (.canExportAnalysis (export-service) (slurp body)))

(defn add-app-to-group
  "A service used to add an existing app to an app group."
  [body]
  (.addAnalysisToTemplateGroup (template-group-service) (slurp body)))

(defn get-app
  "A service used to get an app in the format required by the DE."
  [app-id]
  (.appendNotificationToTemplate (notification-appender)
    (.fetchTemplateByName (app-fetcher) app-id)))

(defn export-template
  "This service will export the template with the given identifier."
  [template-id]
  (.exportTemplate (workflow-export-service) template-id))

(defn export-workflow
  "This service will export a workflow with the given identifier."
  [app-id]
  (.exportAnalysis (workflow-export-service) app-id))

(defn export-deployed-components
  "This service will export all or selected deployed components."
  [body]
  (.getDeployedComponents (workflow-export-service) (slurp body)))

(defn preview-template
  "This service will convert a JSON document in the format consumed by 
   the import service into the format required by the DE."
  [body]
  (.previewTemplate (workflow-preview-service) (slurp body)))

(defn preview-workflow
  "This service will convert a JSON document in the format consumed by 
   the import service into the format required by the DE."
  [body]
  (.previewWorkflow (workflow-preview-service) (slurp body)))

(defn import-template
  "This service will import a template into the DE."
  [body]
  (.importTemplate (workflow-import-service) (slurp body))
  (empty-response))

(defn import-workflow
  "This service will import a workflow into the DE."
  [body]
  (.importWorkflow (workflow-import-service) (slurp body))
  (empty-response))

(defn import-tools
  "This service will import deployed components into the DE and send
   notifications if notification information is included and the deployed
   components are successfully imported."
  [body]
  (.updateWorkflow (workflow-import-service) (slurp body))
  (success-response))

(defn update-app
  "This service will update the information at the top level of an analysis.
   It will not update any of the components of the analysis."
  [body]
  (.updateAnalysisOnly (workflow-import-service) (slurp body))
  (success-response))

(defn update-template
  "This service will either update an existing template or import a new template."
  [body]
  (.updateTemplate (workflow-import-service) (slurp body))
  (empty-response))

(defn update-workflow
  "This service will either update an existing workflow or import a new workflow."
  [body]
  (.updateWorkflow (workflow-import-service) (slurp body))
  (empty-response))

(defn force-update-workflow
  "This service will either update an existing workflow or import a new workflow.  
   Vetted workflows may be updated."
  [body {:keys [update-mode]}]
  (.forceUpdateWorkflow (workflow-import-service) (slurp body) update-mode)
  (empty-response))

(defn delete-workflow
  "This service will logically remove a workflow from the DE."
  [body]
  (.deleteAnalysis (analysis-deletion-service) (slurp body))
  (empty-response))

(defn permanently-delete-workflow
  "This service will physically remove a workflow from the DE."
  [body]
  (.physicallyDeleteAnalysis (analysis-deletion-service) (slurp body))
  (empty-response))

(defn bootstrap
  "This service obtains information about and initializes the workspace for
   the authenticated user."
  []
  (object->json-str (.getCurrentUserInfo (user-service))))

(defn- get-analysis
  "Gets an app from the database."
  [app-id]
  (if (nil? app-id)
    nil
    (try 
      (.getTransformationActivity (analysis-retriever) app-id)
      (catch Exception e nil))))

(defn get-app-description
  "Gets an app description from the database."
  [app-id]
  (log/debug "looking up the description for app" app-id)
  (let [app (get-analysis app-id)]
    (if (nil? app) "" (.getDescription app))))

(defn run-experiment
  "This service accepts a job submission from a user then reformats it and
   submits it to the JEX."
  [body workspace-id]
  (let [json-str (add-workspace-id (slurp body) workspace-id)
        json-obj (object->json-obj json-str)]
    (.runExperiment (experiment-runner) json-obj))
  (empty-response))

(defn get-experiments
  "This service retrieves information about jobs that a user has submitted."
  [workspace-id params]
  (success-response
   {:analyses
    (get-analyses-for-workspace-id (string->long workspace-id) params)}))

(defn get-selected-experiments
  "This service retrieves information about selected jobs that the user has
   submitted."
  [workspace-id body]
  (let [ids (:executions (read-json (slurp body)))]
    (when (empty? ids)
      (throw+ {:type   ::illegal-request-body
               :reason "JSON request body missing identifier list"}))
    (success-response
     {:analyses (get-selected-analyses (string->long workspace-id) ids)})))

(defn delete-experiments
  "This service marks experiments as deleted so that they no longer show up
   in the Analyses window."
  [body workspace-id]
  (let [json-str (add-workspace-id (slurp body) workspace-id)]
    (.deleteExecutionSet (analysis-service) (object->json-obj json-str)))
  (empty-response))

(defn rate-app
  "This service adds a user's rating to an app."
  [body]
  (.rateAnalysis (rating-service) (slurp body)))

(defn delete-rating
  "This service removes a user's rating from an app."
  [body]
  (.deleteRating (rating-service) (slurp body)))

(defn list-deployed-components-in-app
  "This service lists all of the deployed components in an app."
  [app-id]
  (.listDeployedComponentsInAnalysis (analysis-listing-service) app-id))

(defn list-app
  "This service lists a single application.  The response body contains a JSON
   string representing an object containing a list of apps.  If an app with the
   provided identifier exists then the list will contain that app.  Otherwise,
   the list will be empty."
  [app-id]
  (.listAnalysis (analysis-listing-service) app-id))

(defn update-favorites
  "This service adds apps to or removes apps from a user's favorites list."
  [body]
  (.updateFavorite (analysis-categorization-service) (slurp body)))

(defn edit-app
  "This service makes an app available in Tito for editing."
  [app-id]
  (.prepareAnalysisForEditing (analysis-edit-service) app-id))

(defn copy-app
  "This service makes a copy of an app available in Tito for editing."
  [app-id]
  (.copyAnalysis (analysis-edit-service) app-id))

(defn make-app-public
  "This service copies an app from a user's private workspace to the public
   workspace."
  [body]
  (.makeAnalysisPublic (template-group-service) (slurp body)))

(defn get-property-values
  "Gets the property values for a previously submitted job."
  [job-id]
  (.getPropertyValues (property-value-service) job-id))

(defn list-reference-genomes
  "Lists the reference genomes in the database."
  []
  (success-response {:genomes (get-reference-genomes)}))

(defn replace-reference-genomes
  "Replaces teh reference genomes in the database with a new set of reference
   genomes."
  [body]
  (put-reference-genomes (:genomes (read-json body)))
  (success-response))
