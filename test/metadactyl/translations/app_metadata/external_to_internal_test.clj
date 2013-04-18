(ns metadactyl.translations.app-metadata.external-to-internal-test
  (:use [clojure.test]
        [metadactyl.translations.app-metadata.external-to-internal]))

(defn- external-selection-args
  []
  (mapv (fn [n] {:name n :value n :display n}) ["foo" "bar" "baz"]))

(defn- internal-selection-args
  ([]
     (internal-selection-args nil))
  ([default]
     (map #(assoc % :isDefault (= default (:name %)))
          (external-selection-args))))

(deftest build-validator-for-property-test
  (is (= {:required true :rules []}
         (build-validator-for-property {:required true})))
  (is (= {:required false :rules [{:IntAbove [0]}]}
         (build-validator-for-property {:validators [{:type "IntAbove" :params [0]}]})))
  (is (= {:required true :rules [{:MustContain (internal-selection-args)}]}
         (build-validator-for-property {:required true :arguments (external-selection-args)})))
  (is (= {:required false
          :rules    [{:IntAbove    [0]}
                     {:MustContain (internal-selection-args)}]}
         (build-validator-for-property {:validators [{:type "IntAbove" :params [0]}]
                                        :arguments  (external-selection-args)})))
  (is (= {:required     false
          :rules        [{:MustContain (internal-selection-args "foo")}]}
         (build-validator-for-property
          {:arguments    (external-selection-args)
           :defaultValue {:name "foo" :value "foo" :display "foo"}}))))

(deftest translate-property-test
  (is (= {:value "foo" :validator nil}
         (translate-property {:defaultValue "foo"})))
  (is (= {:value nil :validator nil}
         (translate-property {}))))
