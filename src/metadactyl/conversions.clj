(ns metadactyl.conversions)

(defn to-long
  "Converts a string to a long integer."
  [s]
  (try
    (Integer/parseInt s)
    (catch Exception e
      (throw (IllegalArgumentException. e)))))
