(ns metadactyl.util.params
  (:use [metadactyl.util.conversions]))

(defn required-string
  "Extracts a required string argument from a map."
  [k m]
  (let [v (m k)]
    (when (blank? v)
      (throw+ {:code  ::missing-or-empty-param
               :param k}))
    v))

(defn optional-long
  "Extracts an optional long argument from a map, using a default value if the argument wasn't
   provided."
  [k m d]
  (let [v (k m)]
    (if-not (nil? v)
      (to-long v)
      d)))

(defn optional-boolean
  "Extracts an optional Boolean argument from a map."
  ([k m]
     (optional-boolean k m nil))
  ([k m d]
     (let [v (k m)]
       (if (nil? v) d (Boolean/valueOf v)))))

(defn as-keyword
  "Converts a string to a lower-case keyword."
  [s]
  (keyword (lower-case s)))
