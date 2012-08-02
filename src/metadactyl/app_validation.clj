(ns metadactyl.app-validation
  (:use [slingshot.slingshot :only [try+ throw+]]
        [korma.core]
        [kameleon.core]
        [kameleon.entities]
        [kameleon.queries :only [property-types-for-tool-type]])
  (:import [org.iplantc.persistence.dto.components DeployedComponent]
           [org.iplantc.workflow.integration.validation
            UnsupportedPropertyTypeException]))

(defn- get-tool-type-from-database
  "Gets the tool type for the deployed component with the given identifier from
   the database."
  [component-id]
  (first (select deployed_components
                 (fields :tool_types.id :tool_types.name)
                 (join tool_types)
                 (where {:id component-id}))))

(defn- get-tool-type-from-registry
  "Gets the tool type for the deployed component with the given identifier from
   the given registry."
  [registry component-id]
  (let [components (.getRegisteredObjects registry DeployedComponent)
        component  (first (filter #(= component-id (.getId %)) components))
        tool-type  (when-not (nil? component) (.getToolType component))]
    (when-not (nil? tool-type)
      {:id   (.getId tool-type)
       :name (.getName tool-type)})))

(defn- get-tool-type
  "Gets the tool type name for the deployed component with the given identifier."
  [registry component-id]
  (let [tool-type (or (get-tool-type-from-registry registry component-id)
                      (get-tool-type-from-database component-id))]
    (when (nil? tool-type)
      (throw+ {:type         ::component_tool_type_not_found
               :component_id component-id}))
    tool-type))

(defn- get-valid-ptype-names
  "Gets the valid property type names for a given tool type."
  [{tool-type-id :id}]
  (map :name (property-types-for-tool-type tool-type-id)))

(defn validate-template-property-types
  "Validates the property types in a template that is being imported."
  [template registry]
  (let [tool-type    (get-tool-type registry (.getComponent template))
        valid-ptypes (into #{} (get-valid-ptype-names tool-type))
        properties   (mapcat #(.getProperties %) (.getPropertyGroups template))]
    (dorun (map #(throw (UnsupportedPropertyTypeException. % (:name tool-type)))
                (filter #(nil? (valid-ptypes %))
                        (map #(.getPropertyTypeName %) properties))))))
