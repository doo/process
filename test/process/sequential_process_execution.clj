(ns process.sequential-process-execution
  (:use clojure.test
        process.definition
        process.execution.sequential))

;;see: https://github.com/stuartsierra/flow
(def process-definition
  {:result (fnc [gamma delta epsilon] (+ gamma delta epsilon))
   :gamma (fnc [alpha beta] (+ alpha beta))
   :delta (fnc [alpha gamma] (+ alpha gamma))
   :epsilon (fnc [gamma delta] (+ gamma delta))})

(def inputs
  {:alpha 1
   :beta 2})

(deftest sequential-process-execution
  (testing "Sequential process execution"
    (is (= (execute-sequential process-definition
                               inputs
                               :result)
         {:result 14
          :epsilon 7
          :delta 4
          :gamma 3
          :beta 2
          :alpha 1}))))

(comment
  (use 'process.dot)
  (write-dot "g.dot" (build-process-graph (merge process-definition inputs))))