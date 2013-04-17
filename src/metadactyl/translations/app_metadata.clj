(ns metadactyl.translations.app-metadata
  (:require metadactyl.translations.app-metadata.external-to-internal :as e2i))

(defn template-external-to-internal
  "Translates the external template format to the internal template format."
  [external]
  (e2i/translate-template external))
