(ns metadactyl.translations.app-metadata
  (:require [metadactyl.translations.app-metadata.external-to-internal :as e2i]
            [metadactyl.translations.app-metadata.internal-to-external :as i2e]))

(defn template-external-to-internal
  "Translates the external template format to the internal template format."
  [external]
  (e2i/translate-template external))

(defn template-internal-to-external
  [internal]
  (i2e/translate-template internal))
