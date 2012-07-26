(ns metadactyl.service
  (:use [clojure.data.json :only (json-str)]
        [clojure.string :only (join upper-case)]
        [slingshot.slingshot :only [try+]])
  (:require [clj-http.client :as client]
            [clojure.tools.logging :as log]))

(defn empty-response []
  {:status 200})

(defn success-response
  ([map]
     {:status       200
      :body         (json-str (merge {:success true} map))
      :content-type :json})
  ([]
     (success-response {})))

(defn failure-response [e]
  (log/error e "bad request")
  {:status       400
   :body         (json-str {:success false :reason (.getMessage e)})
   :content-type :json})

(defn slingshot-failure-response [m]
  (log/error "bad request:" m)
  {:status       400
   :body         (json-str (assoc (dissoc m :type)
                             :code (upper-case (name (:type m)))))
   :content-type :json})

(defn forbidden-response [e]
  (log/error e "unauthorized")
  {:status 401})

(defn error-response [e]
  (log/error e "internal error")
  {:status 500
   :body (json-str {:success false :reason (.getMessage e)})
   :content-type :json})

(defn unrecognized-path-response []
  "Builds the response to send for an unrecognized service path."
  (let [msg "unrecognized service path"]
    (json-str {:success false :reason msg})))

(defn trap
  "Traps any exception thrown by a service and returns an appropriate
   repsonse."
  [f]
  (try+
    (f)
    (catch [:type ::unauthorized] {:keys [user message]}
      (log/error message user)
      (forbidden-response (:throwable &throw-context)))
    (catch map? m (slingshot-failure-response m))
    (catch IllegalArgumentException e (failure-response e))
    (catch IllegalStateException e (failure-response e))
    (catch Throwable t (error-response t))))

(defn build-url
  "Builds a URL from a base URL and one or more URL components."
  [base & components]
  (join "/" (map #(.replaceAll % "^/|/$" "")
                 (cons base components))))

(defn prepare-forwarded-request
  "Prepares a request to be forwarded to a remote service."
  [request body]
  {:content-type (get-in request [:headers :content-type])
   :headers (dissoc (:headers request) "content-length" "content-type")
   :body body})

(defn forward-get
  "Forwards a GET request to a remote service."
  [url request]
  (client/get url (prepare-forwarded-request request)))

(defn forward-post
  "Forwards a POST request to a remote service."
  [url request body]
  (client/post url (prepare-forwarded-request request body)))

(defn forward-put
  "Forwards a PUT request to a remote service."
  [url request body]
  (client/put url (prepare-forwarded-request request body)))

(defn forward-delete
  "Forwards a DELETE request to a remote service."
  [url request]
  (client/delete url (prepare-forwarded-request request)))
