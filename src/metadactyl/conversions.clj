(ns metadactyl.conversions)

(defn to-long
  "Converts a string to a long integer."
  [s]
  (try
    (Long/parseLong s)
    (catch Exception e
      (throw (IllegalArgumentException. e)))))
