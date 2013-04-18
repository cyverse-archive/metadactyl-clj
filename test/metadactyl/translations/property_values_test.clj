(ns metadactyl.translations.property-values-test
  (:use [metadactyl.translations.property-values]
        [clojure.test]))

(deftest string-value
  (is (= {:parameters [{:param_value {:value "foo"}}]})
      (normalize-property-values
       {:parameters [{:param_value "foo"}]})))

(deftest int-value
  (is (= {:parameters [{:param_value {:value 1}}]}
         (normalize-property-values
          {:parameters [{:param_value 1}]}))))

(deftest array-value
  (is (= {:parameters [{:param_value {:value ["foo" "bar" "baz"]}}]}
         (normalize-property-values
          {:parameters [{:param_value ["foo" "bar" "baz"]}]}))))

(deftest object-value
  (is (= {:parameters [{:param_value {:value {:foo "bar" :baz "quux"}}}]}
         (normalize-property-values
          {:parameters [{:param_value {:foo "bar" :baz "quux"}}]}))))
