(ns metadactyl.translations.property-values)

(defn- normalize-property-value
  "Normalizes the parameter value within a property in the property value service."
  [prop]
  (update-in prop [:param_value] (fn [v] {:value v})))

(defn normalize-property-values
  "Normalizes the values in the output for the property value service."
  [output]
  (update-in output [:parameters] (partial map normalize-property-value)))
