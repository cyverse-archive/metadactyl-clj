(ns metadactyl.util.params
  (:use [clojure.string :only [blank? lower-case]]
        [metadactyl.util.conversions]
        [slingshot.slingshot :only [throw+]]))

(defn required-string
  "Extracts a required string argument from a map."
  [ks m]
  (let [v (first (remove blank? (map m ks)))]
    (when (blank? v)
      (throw+ {:code   ::missing-or-empty-param
               :params ks}))
    v))

(defn optional-long
  "Extracts an optional long argument from a map."
  ([ks m]
     (optional-long ks m nil))
  ([ks m d]
     (let [v (first (remove blank? (map m ks)))]
       (if-not (nil? v)
         (to-long v)
         d))))

(defn optional-boolean
  "Extracts an optional Boolean argument from a map."
  ([ks m]
     (optional-boolean ks m nil))
  ([ks m d]
     (let [v (first (remove blank? (map m ks)))]
       (if (nil? v) d (Boolean/valueOf v)))))

(defn as-keyword
  "Converts a string to a lower-case keyword."
  [s]
  (keyword (lower-case s)))

(defn optional-keyword
  "Extracts an optional keyword argument from a map."
  ([ks m]
     (optional-keyword ks m nil))
  ([ks m d]
     (let [v (first (remove blank? (map m ks)))]
       (if (nil? v) d (as-keyword v)))))
