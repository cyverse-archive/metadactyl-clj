(ns metadactyl.metadata.element-listings
  (:use [kameleon.core]
        [kameleon.entities]
        [kameleon.queries]
        [korma.core]
        [slingshot.slingshot :only [throw+]])
  (:require [cheshire.core :as cheshire]))

(defn- base-property-type-query
  "Creates the base query used to list property types for the metadata element
   listing service."
  []
  (-> (select* property_type)
      (fields :property_type.hid :property_type.id :property_type.name
              [:value_type.name :value_type] :property_type.description)
      (join value_type)
      (where {:deprecated false})))

(defn- get-tool-type-id
  "Gets the internal identifier associated with a tool type name."
  [tool-type-name]
  (let [result (get-tool-type-by-name tool-type-name)]
    (when (nil? result)
      (throw+ {:type ::unknown_tool_type
               :name tool-type-name}))
    (:id result)))

(defn- get-tool-type-for-component-id
  "Gets the tool type associated with the given deployed component identifier."
  [component-id]
  (let [result (get-tool-type-by-component-id component-id)]
    (when (nil? result)
      (throw+ {:type ::unknown_deployed_component
               :id   component-id}))
    (:id result)))

(defn- get-tool-type
  "Gets the tool type to use when listing property types.  If the tool type is
   specified directly then we'll use that in the query.  If the deployed
   component is specified then its associated tool type will be used in the
   query.  Otherwise, all property types will be listed."
  [tool-type component-id]
  (cond (not (nil? tool-type))    (get-tool-type-id tool-type)
        (not (nil? component-id)) (get-tool-type-for-component-id component-id)
        :else                     nil))

(defn- list-property-types
  "Obtains the property types for the metadata element listing service.
   Property types may be filtered by tool type or deployed component.  If the
   tool type is specified only property types that are associated with that
   tool type will be listed.  If the deployed component is specified then only
   property tpes associated with the tool type that is associated with the
   deployed component will be listed.  Specifying an invalid tool type name or
   deployed component id will result in an error."
  [{:keys [tool-type component-id]}]
  (let [tool-type-id (get-tool-type tool-type component-id)]
    {:property_types
     (if (nil? tool-type-id)
       (select (base-property-type-query))
       (property-types-for-tool-type (base-property-type-query) tool-type-id))}))

(defn- list-tool-types
  "Obtains the list of tool types for the metadata element listing service."
  [params]
  {:tool_types (select tool_types)})

(def ^:private listing-fns
  "The listing functions to use for various metadata element types."
  {"property-types" list-property-types
   "tool-types"     list-tool-types})

(defn- list-all
  "Lists all of the element types that are available to the listing service."
  [params handler-fn]
  (let [initial-results (cheshire/decode (handler-fn) true)]
    (reduce merge initial-results (map #(% params) (vals listing-fns)))))

(defn list-elements
  "Lists selected workflow elements.  This function handles requests to list
   various different types of workflow elements."
  [elm-type params handler-fn]
  (cond
   (= elm-type "all")               (list-all params handler-fn)
   (contains? listing-fns elm-type) ((listing-fns elm-type) params)
   :else                            (cheshire/decode (handler-fn) true)))
