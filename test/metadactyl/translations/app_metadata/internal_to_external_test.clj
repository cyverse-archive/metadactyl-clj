(ns metadactyl.translations.app-metadata.internal-to-external-test
  (:use [clojure.test]
        [metadactyl.translations.app-metadata.internal-to-external]))

(deftest validators-from-rules-test
  (is (= [{:type "foo" :params ["bar" "baz"]}
          {:type "bar" :params ["baz" "quux"]}]
         (validators-from-rules
          [{:foo ["bar" "baz"]}
           {:bar ["baz" "quux"]}]))))

(deftest validators-from-nil-rules-test
  (is (= [] (validators-from-rules nil))))

(deftest validators-from-empty-rules-test
  (is (= [] (validators-from-rules []))))

(deftest get-property-arguments-from-nil-rules-test
  (is (= [] (get-property-arguments nil))))

(deftest get-property-arguments-from-empty-rules-test
  (is (= [] (get-property-arguments []))))

(deftest get-property-arguments-no-must-contain-test
  (is (= [] (get-property-arguments [{:IntAbove [0]}]))))

(deftest get-property-arguments-test
  (is (= [{:isDefault "false"
           :name      "foo"
           :value     "foo"
           :display   "foo"}]
         (get-property-arguments
          [{:MustContain
            [{:isDefault "false"
              :name      "foo"
              :value     "foo"
              :display   "foo"}]}]))))

(deftest get-empty-default-value-test
  (is (= "" (get-default-value {} []))))

(deftest get-default-value-from-prop-test
  (is (= "testing" (get-default-value {:value "testing"} []))))

(deftest get-default-value-test
  (is (= {:name    "foo"
          :value   "foo"
          :display "foo"}
         (get-default-value
          {}
          [{:isDefault "true"
            :name      "foo"
            :value     "foo"
            :display   "foo"}
           {:isDefault "false"
            :name      "bar"
            :value     "bar"
            :display   "bar"}]))))

(deftest translate-property-test
  (is (= {:name         "prop-name"
          :arguments    []
          :required     false
          :validators   []
          :defaultValue ""}
         (translate-property {:name "prop-name"}))))

(deftest translate-required-property-test
  (is (= {:name         "prop-name"
          :arguments    []
          :required     true
          :validators   []
          :defaultValue ""}
         (translate-property
          {:name      "prop-name"
           :validator {:required true}}))))

(deftest translate-property-with-default-value-test
  (is (= {:name         "prop-name"
          :arguments    []
          :required     false
          :validators   []
          :defaultValue "default-value"}
         (translate-property
          {:name  "prop-name"
           :value "default-value"}))))

(deftest translate-property-with-rules-test
  (is (= {:name         "prop-name"
          :arguments    []
          :required     false
          :validators   [{:type   "IntAbove"
                          :params [42]}]
          :defaultValue ""}
         (translate-property
          {:name      "prop-name"
           :validator {:rules [{:IntAbove [42]}]}}))))

(deftest translate-property-with-args-test
  (is (= {:name         "prop-name"
          :arguments    [{:name    "foo"
                          :value   "foo"
                          :display "foo"}]
          :required     false
          :validators   []
          :defaultValue ""}
         (translate-property
          {:name      "prop-name"
           :validator {:rules [{:MustContain [{:isDefault "false"
                                               :name      "foo"
                                               :value     "foo"
                                               :display   "foo"}]}]}}))))

(deftest translate-property-with-default-arg-test
  (is (= {:name         "prop-name"
          :arguments    [{:name    "foo"
                          :value   "foo"
                          :display "foo"}
                         {:name    "bar"
                          :value   "bar"
                          :display "bar"}]
          :required     true
          :validators   []
          :defaultValue {:name    "foo"
                         :value   "foo"
                         :display "foo"}}
         (translate-property
          {:name      "prop-name"
           :validator {:required true
                       :rules    [{:MustContain [{:isDefault "true"
                                                  :name      "foo"
                                                  :value     "foo"
                                                  :display   "foo"}
                                                 {:isDefault "false"
                                                  :name      "bar"
                                                  :value     "bar"
                                                  :display   "bar"}]}]}}))))

(deftest translate-property-with-args-and-rules-test
  (is (= {:name         "prop-name"
          :arguments    [{:name    "foo"
                          :value   "foo"
                          :display "foo"}
                         {:name    "bar"
                          :value   "bar"
                          :display "bar"}]
          :required     true
          :validators   [{:type   "IntAbove"
                          :params [42]}]
          :defaultValue {:name    "foo"
                         :value   "foo"
                         :display "foo"}}
         (translate-property
          {:name      "prop-name"
           :validator {:required true
                       :rules    [{:MustContain [{:isDefault "true"
                                                  :name      "foo"
                                                  :value     "foo"
                                                  :display   "foo"}
                                                 {:isDefault "false"
                                                  :name      "bar"
                                                  :value     "bar"
                                                  :display   "bar"}]}
                                  {:IntAbove [42]}]}}))))

(deftest translate-property-group-test
  (is (= {:name       "group-name"
          :properties [{:name         "prop-name"
                        :arguments    []
                        :required     false
                        :validators   []
                        :defaultValue ""}]}
         (translate-property-group
          {:name       "group-name"
           :properties [{:name "prop-name"}]}))))

(deftest translate-template-test
  (is (= {:name   "template-name"
          :groups [{:name       "group-name"
                    :properties [{:name         "prop-name"
                                  :arguments    []
                                  :required     false
                                  :validators   []
                                  :defaultValue ""}]}]}
         (translate-template
          {:name   "template-name"
           :groups [{:name       "group-name"
                     :properties [{:name "prop-name"}]}]}))))
