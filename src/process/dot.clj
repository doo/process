(ns process.dot
  (:require [clojure.tools.namespace.dependency :as dep]))

(defn dot
  "Prints a representation of the flow to standard output,
  suitable for the Graphviz 'dot' program. graph-name is a symbol,
  must not be a Graphviz reserved word (such as 'graph')."
  [graph]
  (apply
   str
   (concat
    ["digraph " "G" " {" "\n"]
    (let [nodes-mapping (into {}
                              (map (fn [node alias]
                                     [node alias])
                                   (dep/nodes graph)
                                   (range)))]
      (for [sym (dep/nodes graph)
            dep (dep/immediate-dependents graph sym)]
        (let [sym-alias (get nodes-mapping sym)
              dep-alias (get nodes-mapping dep)]
          (str
           (str "  " sym-alias " [label=\"" sym "\"]" "\n")
           (str "  " dep-alias " [label=\"" dep "\"]" "\n")
           (str "  " sym-alias  '-> dep-alias \; "\n")))))
    ["}"])))

(defn write-dot [f graph]
  (spit f (dot graph)))