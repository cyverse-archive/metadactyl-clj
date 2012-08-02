(defproject metadactyl "1.2.0-SNAPSHOT"
  :description "Framework for hosting DiscoveryEnvironment metadata services."
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/tools.logging "0.2.3"]
                 [org.clojure/data.json "0.1.2"]
                 [clj-http "0.4.1"]
                 [c3p0/c3p0 "0.9.1.2"]
                 [compojure "1.0.1"]
                 [org.iplantc/clojure-commons "1.2.0-SNAPSHOT"]
                 [org.iplantc/kameleon "0.0.1-SNAPSHOT"]
                 [org.iplantc.core/metadactyl "dev-SNAPSHOT"]
                 [org.springframework/spring-orm "3.1.0.RELEASE"]
                 [korma "0.3.0-beta10"]
                 [ring/ring-jetty-adapter "1.0.1"]
                 [org.slf4j/slf4j-api "1.5.8"]
                 [org.slf4j/slf4j-log4j12 "1.5.8"]
                 [net.sf.json-lib/json-lib "2.4" :classifier "jdk15"]]
  :plugins [[org.iplantc/lein-iplant-rpm "1.3.0-SNAPSHOT"]
            [lein-ring "0.7.1"]
            [lein-swank "1.4.4"]]
  :profiles {:dev {:resource-paths ["conf/test"]}}
  :aot [metadactyl.core]
  :main metadactyl.core
  :ring {:handler metadactyl.core/app
         :init metadactyl.core/load-configuration-from-props}
  :iplant-rpm {:summary "iPlant Discovery Environment Metadata Services"
               :provides "metadactyl"
               :dependencies ["iplant-service-config >= 0.1.0-5"]
               :config-files ["log4j.properties"]
               :config-path "conf/main"}
  :uberjar-exclusions [#"BCKEY.SF"]
  :repositories {"iplantCollaborative"
                 "http://projects.iplantcollaborative.org/archiva/repository/internal/"})
