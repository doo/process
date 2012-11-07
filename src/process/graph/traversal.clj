(ns process.graph.traversal
  (:require [clojure.tools.namespace.dependency :as dep]
            [clojure.set :as set]))

(defn level-order
  "Traverses the dependency graph in level order starting with the given node."
  [graph node]
  (loop [result [#{node}]]
    (let [current (last result)]
      (if (some #(not (empty? (dep/immediate-dependencies graph %))) current)
        (recur (conj result
                     (set (mapcat #(dep/immediate-dependencies graph %) current))))
        result))))

(defn unique-sets
  "Takes care that every element only exists once in a sequence of sets."
  [seq-of-sets]
  (->> seq-of-sets
       (reduce
        (fn [[existing result] set]
          [(set/union existing set) (conj result (set/difference set existing))])
        [#{} []])
       second))