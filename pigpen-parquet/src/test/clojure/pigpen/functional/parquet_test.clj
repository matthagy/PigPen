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

(ns pigpen.functional.parquet-test
  (:require [clojure.test :refer :all]
            [pigpen.extensions.test :refer [test-diff pigsym-zero pigsym-inc regex->string]]
            [pigpen.core :as pig]
            [pigpen.pig :refer [dump]]
            [pigpen.parquet.core :as pig-parquet]))

(.mkdirs (java.io.File. "build/functional/parquet-test"))

(deftest test-parquet
  (let [location "build/functional/parquet-test/test-parquet"
        schema {:x :int, :y :chararray}
        data [{:x 1 :y "a"}
              {:x 2 :y "b"}
              {:x 3 :y "c"}]
        store-command (pig-parquet/store-parquet location schema (pig/return data))
        load-command (pig-parquet/load-parquet (str location "/part-m-00001.parquet") schema)]
    (dump store-command)
    (is (= (dump load-command) data))))
