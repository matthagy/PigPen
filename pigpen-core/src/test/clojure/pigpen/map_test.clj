;;
;;
;;  Copyright 2013 Netflix, Inc.
;;
;;     Licensed under the Apache License, Version 2.0 (the "License");
;;     you may not use this file except in compliance with the License.
;;     You may obtain a copy of the License at
;;
;;         http://www.apache.org/licenses/LICENSE-2.0
;;
;;     Unless required by applicable law or agreed to in writing, software
;;     distributed under the License is distributed on an "AS IS" BASIS,
;;     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;;     See the License for the specific language governing permissions and
;;     limitations under the License.
;;
;;

(ns pigpen.map-test
  (:use clojure.test)
  (:require [pigpen.extensions.test :refer [test-diff pigsym-zero pigsym-inc]]
            [pigpen.map :as pig]))

(deftest test-map
  (with-redefs [pigpen.raw/pigsym (pigsym-inc)]

    (let [^:local r {:fields '[value]}
          bar 2
          baz (fn [v] v)]
      
      (test-diff
        (pig/map (fn [v] v) r)
        '{:type :bind
          :id bind1
          :description "(fn [v] v)\n"
          :ancestors [{:fields [value]}]
          :func (pigpen.runtime/map->bind
                  (pigpen.runtime/with-ns pigpen.map-test
                    (clojure.core/let [bar (quote 2)]
                      (fn [v] v))))
          :args [value]
          :requires []
          :fields [value]
          :field-type-in :frozen
          :field-type-out :frozen
          :opts {:type :bind-opts}})
      
      (is (thrown? AssertionError (pig/map nil r)))
      (is (thrown? clojure.lang.Compiler$CompilerException (pig/map foo r)))
      (is (thrown? AssertionError (pig/map bar r)))
      (is (thrown? clojure.lang.Compiler$CompilerException (pig/map baz r)))
      (is (thrown? AssertionError (pig/map (fn [] 42) r)))
      (is (thrown? AssertionError (pig/map #(vector %1 %2) r))))))

(deftest test-mapcat
  (with-redefs [pigpen.raw/pigsym (pigsym-inc)]

    (let [^:local r {:fields '[value]}]
      
      (test-diff
        (pig/mapcat (fn [v] [v]) r)
        '{:type :bind
          :id bind1
          :description "(fn [v] [v])\n"
          :ancestors [{:fields [value]}]
          :func (pigpen.runtime/mapcat->bind
                  (pigpen.runtime/with-ns pigpen.map-test
                    (fn [v] [v])))
          :args [value]
          :requires []
          :fields [value]
          :field-type-in :frozen
          :field-type-out :frozen
          :opts {:type :bind-opts}}))))

(deftest test-map-indexed
  (with-redefs [pigpen.raw/pigsym (pigsym-inc)]
    (let [^:local r {:fields '[value]}]
      (test-diff
        (pig/map-indexed vector r)
        '{:type :bind
          :id bind2
          :description nil
          :func (pigpen.runtime/map->bind
                  (pigpen.runtime/with-ns pigpen.map-test
                    vector))
          :args [$0 value]
          :requires []
          :fields [value]
          :field-type-out :frozen
          :field-type-in :frozen
          :opts {:type :bind-opts}
          :ancestors [{:type :rank
                       :id rank1
                       :description "vector\n"
                       :fields [value $0]
                       :field-type :frozen
                       :opts {:type :rank-opts}
                       :sort-keys []
                       :ancestors [{:fields [value]}]}]}))))

(deftest test-sort
  (with-redefs [pigpen.raw/pigsym (pigsym-inc)]
    (let [^:local r {:fields '[value]}]
      (test-diff
        (pig/sort r)
        '{:type :order
          :id order3
          :description nil
          :fields [value]
          :field-type :frozen
          :opts {:type :order-opts}
          :sort-keys [key :asc]
          :ancestors [{:type :generate
                       :id generate2
                       :description nil
                       :fields [key value]
                       :field-type :frozen
                       :opts {:type :generate-opts}
                       :projections [{:type :projection-field, :field 0, :alias key}
                                     {:type :projection-field, :field 1, :alias value}]
                       :ancestors [{:type :bind
                                    :id bind1
                                    :description nil
                                    :func (pigpen.runtime/key-selector->bind clojure.core/identity)
                                    :args [value]
                                    :requires []
                                    :fields [value]
                                    :field-type-in :frozen
                                    :field-type-out :native-key-frozen-val
                                    :ancestors [{:fields [value]}]
                                    :opts {:type :bind-opts
                                           :implicit-schema true}}]}]}))))

(deftest test-sort-by
  (with-redefs [pigpen.raw/pigsym (pigsym-inc)]
    (let [^:local r {:fields '[value]}]
      (test-diff
        (pig/sort-by :a r)
        '{:type :order
          :id order3
          :description ":a\n"
          :fields [value]
          :field-type :frozen
          :opts {:type :order-opts}
          :sort-keys [key :asc]
          :ancestors [{:type :generate
                       :id generate2
                       :description nil
                       :fields [key value]
                       :field-type :frozen
                       :opts {:type :generate-opts}
                       :projections [{:type :projection-field, :field 0, :alias key}
                                     {:type :projection-field, :field 1, :alias value}]
                       :ancestors [{:type :bind
                                    :id bind1
                                    :description nil
                                    :func (pigpen.runtime/key-selector->bind
                                            (pigpen.runtime/with-ns pigpen.map-test :a))
                                    :args [value]
                                    :requires []
                                    :fields [value]
                                    :field-type-in :frozen
                                    :field-type-out :native-key-frozen-val
                                    :ancestors [{:fields [value]}]
                                    :opts {:type :bind-opts
                                           :implicit-schema true}}]}]}))))
