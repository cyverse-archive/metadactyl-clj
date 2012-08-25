(ns metadactyl.metadata.reference-genomes
  (:use [clojure.string :only [blank?]]
        [kameleon.core]
        [kameleon.entities]
        [kameleon.queries :only [get-user-id]]
        [korma.core]
        [korma.db]
        [metadactyl.util.conversions :only [date->long long->timestamp]]
        [slingshot.slingshot :only [throw+]]))

(defn- format-reference-genome
  "Formats a reference genome for the reference genome listing service."
  [genome]
  (assoc genome
    :created_on        (date->long (:created_on genome) "")
    :last_modified_on  (date->long (:last_modified_on genome) "")))

(defn get-reference-genomes
  "Lists all of the reference genomes in the database."
  []
  (map format-reference-genome
       (select genome_reference
               (fields :uuid :name :path :deleted :created_on :last_modified_on
                       [:created_by.username :created_by]
                       [:last_modified_by.username :last_modified_by])
               (join created_by)
               (join last_modified_by))))

(defn- validate-field
  "Validates a single field in a reference genome."
  [genome field]
  (when (nil? (genome field))
    (throw+ {:action ::insert_reference_genome
             :type   ::missing_required_field
             :genome genome
             :field  field}))
  (when (blank? (genome field))
    (throw+ {:action ::insert_reference_genome
             :type   ::empty_required_field
             :genome genome
             :field  field})))

(defn- validate-username
  "Validates a username field in a reference genome."
  [genome field]
  (let [username (genome field)]
    (when-not (or (blank? username)
                  (= "<public>" username)
                  (re-find #"@" username))
      (throw+ {:action ::insert_reference_genome
               :type   ::username_not_fully_qualified
               :genome genome
               :field  field}))))

(defn- validate-reference-genome
  "Validates a reference genome for the reference genome replacement service."
  [genome]
  (dorun (map #(validate-field genome %) [:uuid :name :path :created_by]))
  (dorun (map #(validate-username genome %) [:created_by :last_modified_by]))
  genome)

(defn- get-user-ids
  "Gets the user IDs for the provided usernames."
  [genome]
  (letfn [(get-id [nm] (when-not (blank? nm) (get-user-id nm)))]
    (assoc genome
      :created_by       (get-id (:created_by genome))
      :last_modified_by (get-id (:last_modified_by genome)))))

(defn- parse-timestamps
  "Parses the timestamps in a reference genome."
  [genome]
  (assoc genome
    :created_on       (long->timestamp (:created_on genome))
    :last_modified_on (long->timestamp (:last_modified_on genome))))

(defn- extract-known-fields
  "Extracts only the fields that we know about from a reference genome
   definition."
  [genome]
  (let [known-fields #{:uuid :name :path :deleted :created_by :created_on
                       :last_modified_by :last_modified_on}]
    (into {} (filter #(known-fields (key %)) genome))))

(defn- parse-reference-genome
  "Parses a reference genome for the reference genome replacement service.  The
   result of this function can be passed directly to an insert for the genome
   reference table."
  [genome]
  (-> genome
      extract-known-fields
      validate-reference-genome
      get-user-ids
      parse-timestamps))

(defn put-reference-genomes
  "Replaces the existing reference genomes in the database."
  [genomes]
  (transaction
   (exec-raw "TRUNCATE genome_reference")
   (insert genome_reference
           (values (map parse-reference-genome genomes)))))
