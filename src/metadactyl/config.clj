(ns metadactyl.config
  (:use [clojure.string :only (blank? split)])
  (:require [clojure-commons.props :as cc-props]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]))

(defn- file-exists-on-classpath
  "Determines if a file exists somewhere on the classpath."
  [filename]
  (not (nil? (cc-props/find-properties-file filename))))

(defn- find-first-existing-file
  "Finds the first file in a list of files that exists on the classpath."
  [files]
  (first (filter file-exists-on-classpath files)))

(def
  ^{:doc "The names of the properties files."}
  prop-files ["zkhosts.properties" "metadactyl.properties"])

(def
  ^{:doc "The name of the properties files that is being used."}
   prop-file (find-first-existing-file prop-files))

(def
  ^{:doc "The properties loaded from the properties file."}
   zk-props (cc-props/parse-properties prop-file))

(def
  ^{:doc "The URL used to connect to zookeeper."}
   zk-url (get zk-props "zookeeper"))

(def
  ^{:doc "The properites that have been loaded from Zookeeper."}
   props (atom nil))

(def
  ^{:doc "True if the configuration is valid."}
   configuration-is-valid (atom true))

(defn- record-missing-prop
  "Records a property that is missing.  Instead of failing on the first
   missing parameter, we log the missing parameter, mark the configuration
   as invalid and keep going so that we can log as many configuration errors
   as possible in one run."
  [prop-name]
  (log/error "required configuration setting" prop-name "is empty or"
             "undefined")
  (reset! configuration-is-valid false))

(defn- record-invalid-prop
  "Records a property that has an invalid value.  Instead of failing on the
   first missing parameter, we log the missing parameter, mark the
   configuration as invalid and keep going so that we can log as many
   configuration errors as possible in one run."
  [prop-name t]
  (log/error "invalid configuration setting for" prop-name ":" t)
  (reset! configuration-is-valid false))

(defn- get-required-prop
  "Gets a required property from the properties that were loaded from
   Zookeeper."
  [prop-name]
  (let [value (get @props prop-name "")]
    (when (blank? value)
      (record-missing-prop prop-name))
    value))

(defn- vector-from-prop
  "Derives a list of values from a single comma-delimited value."
  [value]
  (split value #", *"))

(defn- get-required-vector-prop
  "Gets a required vector property from the properties that were loaded from
   Zookeeper."
  [prop-name]
  (vector-from-prop (get-required-prop prop-name)))

(defn- string-to-int
  "Attempts to convert a String property to an integer property.  Returns nil
   if the property can't be converted."
  [prop-name value]
  (try
    (Integer/parseInt value)
    (catch NumberFormatException e
      (do (record-invalid-prop prop-name e) 0))))

(defn- get-required-integer-prop
  "Gets a required integer property from the properties that wer loaded from
   Zookeeper."
  [prop-name]
  (let [value (get-required-prop prop-name)]
    (if (blank? value)
      0
      (string-to-int prop-name value))))

(defmacro STR
  "defines a required string property."
  {:private true}
  [fname desc pname]
  `(defn ~fname ~desc [] (get-required-prop ~pname)))

(defmacro VEC
  "Defines a required vector property."
  {:private true}
  [fname desc pname]
  `(defn ~fname ~desc [] (get-required-vector-prop ~pname)))

(defmacro INT
  "Defines a required integer property."
  {:private true}
  [fname desc pname]
  `(defn ~fname ~desc [] (get-required-integer-prop ~pname)))

(def ^:dynamic refgens (atom nil))

(defn reference-genomes
  "Pulls in reference_genomes.json from the classpath, parses it as JSON,
   and returns a HashMap (for compatibility with metadactyl)."
  []
  (if (nil? @refgens)
    (reset! refgens 
            (java.util.HashMap. 
              (json/read-json 
                (slurp (cc-props/find-resources-file "reference_genomes.json")) 
                false)))
    @refgens))

(INT listen-port 
  "The port that metadactyl listens to."
  "metadactyl.app.listen-port")

(STR db-driver-class
  "The name of the JDBC driver to use."
  "metadactyl.db.driver" )

(STR db-subprotocol
  "The subprotocol to use when connecting to the database (e.g.
   postgresql)."
  "metadactyl.db.subprotocol")

(STR db-host
  "The host name or IP address to use when
   connecting to the database."
  "metadactyl.db.host")

(STR db-port
  "The port number to use when connecting to the database."
  "metadactyl.db.port")

(STR db-name
  "The name of the database to connect to."
  "metadactyl.db.name")

(STR db-user
  "The username to use when authenticating to the database."
  "metadactyl.db.user")

(STR db-password
  "The password to use when authenticating to the database."
  "metadactyl.db.password")

(VEC hibernate-resources
  "The names of the hibernate resource files to include in the Hibernate
   session factory configuration."
  "metadactyl.hibernate.resources")

(VEC hibernate-packages
  "The names of Java packages that Hibernate needs to scan for JPA
   annotations."
  "metadactyl.hibernate.packages")

(STR hibernate-dialect
  "The dialect that Hibernate should use when generating SQL."
  "metadactyl.hibernate.dialect")

(STR zoidberg-base-url
  "The base URL to use when connecting to Zoidberg."
  "metadactyl.zoidberg.base-url")

(INT zoidberg-connection-timeout
  "The maximum number of milliseconds to wait for a connection to Zoidberg."
  "metadactyl.zoidberg.connection-timeout")

(STR zoidberg-encoding
  "The character encoding to use when communicating with Zoidberg."
  "metadactyl.zoidberg.encoding")

(STR osm-base-url
  "The base URL to use when connecting to the OSM."
  "metadactyl.osm.base-url")

(INT osm-connection-timeout
  "The maximum number of milliseconds to wait for a connection to the OSM."
  "metadactyl.osm.connection-timeout")

(STR osm-encoding
  "The character encoding to use when communicating with the OSM."
  "metadactyl.osm.encoding")

(STR osm-jobs-bucket
  "The OSM bucket containing information about jobs that the user has
   submitted."
  "metadactyl.osm.jobs-bucket")

(STR osm-job-request-bucket
  "The OSM bucket containing job submission requests that were sent from the
   UI to metadactyl."
  "metadactyl.osm.job-request-bucket")

(STR jex-base-url
  "The base URL to use when connecting to the JEX."
  "metadactyl.jex.base-url")

(STR workspace-root-app-group
  "The name of the root app group in each user's workspace."
  "metadactyl.workspace.root-app-group")

(STR workspace-default-app-groups
  "The names of the app groups that appear immediately beneath the root app
   group in each user's workspace."
  "metadactyl.workspace.default-app-groups")

(INT workspace-dev-app-group-index
  "The index of the category within a user's workspace for apps under
   development."
  "metadactyl.workspace.dev-app-group-index")

(INT workspace-favorites-app-group-index
  "The index of the category within a user's workspace for favorite apps."
  "metadactyl.workspace.favorites-app-group-index")

(STR cas-server
  "The base URL used to connect to the CAS server."
  "metadactyl.cas.cas-server")

(STR server-name
  "The name of the local server."
  "metadactyl.cas.server-name")

(STR uid-domain
  "The domain name to append to the user identifier to get the fully qualified
   user identifier."
  "metadactyl.uid.domain")

(defn configuration-valid
  "Returns the value of the configuration validity flag.  This function should
   only be called after Metadactyl has been initialized."
  []
  @configuration-is-valid)
