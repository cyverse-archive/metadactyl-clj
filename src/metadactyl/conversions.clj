(ns metadactyl.conversions)

(defn to-long
  "Converts a string to a long integer."
  [s]
  (try
    (Long/parseLong s)
    (catch Exception e
      (throw (IllegalArgumentException. e)))))

(defn date->long
  "Converts a Date object to a Long representation of its timestamp. Returns nil
   if a nil date is given."
  [date]
  (if (nil? date) nil (.getTime date)))