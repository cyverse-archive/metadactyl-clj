(ns metadactyl.json
  (:use [clojure.data.json :only [read-json json-str]]
        [slingshot.slingshot :only [throw+ try+]]))

(defn from-json
  "Parses a JSON string, throwing an informative exception if the JSON string
   can't be parsed."
  [str]
  (try+
   (read-json str)
   (catch Exception e
     (throw+ {:type   ::invalid_request_body
              :reason "NOT_JSON"
              :detail (.getMessage e)}))))

(defn to-json
  "Converts a Clojure data structure to a JSON string."
  [data]
  (json-str data))
