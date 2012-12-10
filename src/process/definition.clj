(ns process.definition
  "A process definition is an ordinary map where the key is the name for the output
   of a component and the value is the component itself (a function for example, see fnc)."
  (:require [clojure.tools.namespace.dependency :as dep]
            [clojure.set :as set]
            [clojure.walk :as walk]))

(defmacro fnc
  "A component function can be used as a component in a process definition. It
   adds additional metadata to an ordinary function that is used by the
   process execution to figure out the dependencies of this fnc or rather
   process component. Every argument of a fnc is a dependency to the output
   of the corresponding process component in the process definition."
  [dependencies & body]
  `(with-meta
     (fn ~dependencies
       ~@body)
     {:dependencies ~(vec (map keyword dependencies))
      :fnc true}))

(defn fnc?
  "Checks if the given function is a process component function."
  [f]
  (true? (:fnc (meta f))))

(defn get-component-dependencies
  "At the moment only functions are supported as process components. If
   the function is a process component function the dependencies are
   returned."
  [process-component]
  (:dependencies (meta process-component)))

(defn get-inputs
  "Gets the inputs of a process graph. An input of a process is a component that
   has no dependencies to other components. The process graph may only be
   traversed partially. The traversal stops, if every immediate and every
   transitive dependency of the given output is satisfied."
  [process-graph output]
  (->> (dep/transitive-dependencies process-graph output)
       (filter #(empty? (dep/immediate-dependencies process-graph %)))
       set))

(defn get-missing-dependencies
  "Gets all components of a process definition that dependencies can not be fully
   satisfied."
  [process-definition]
  (let [components (set (keys process-definition))
        dependencies (->> (filter fnc? (vals process-definition))
                          (mapcat get-component-dependencies)
                          set)]
    (set/difference dependencies components)))

(defn build-process-graph
  "Builds a dependency graph from a given process definition."
  [process-definition]
  (reduce
   (fn [process-graph [output component]]
     (reduce
      (fn [process-graph dependency]
        (dep/depend process-graph output dependency))
      process-graph
      (get-component-dependencies component)))
   (dep/graph)
   process-definition))

(defn missing-component-dependencies
  "Returns every dependency of the process component function that
   is not satisfied by the available-outputs map."
  [fnc available-outputs]
  (set/difference (set (get-component-dependencies fnc)) (set (keys available-outputs))))

(defn- extract-symbols
  [form]
  (let [symbols (atom [])
        prepared-form
        (walk/postwalk
         (fn [x]
           (if (symbol? x)
             (swap! symbols conj x)
             x)) form)]
    @symbols))

(defn unknown-symbols [refers symbols]
  (let [refers (set refers)
        symbols (set symbols)]
    (set/difference symbols refers)))

(defn- build-process-components
  [refers bindings]
  (->> bindings
       (partition 2)
       (map (fn [[output form]]
              [(keyword output)
               (let [symbols (extract-symbols form)
                     dependencies (vec (unknown-symbols refers symbols))]
                 (if (list? form)
                   (list 'process.definition/fnc dependencies
                         form)
                   form))]))
       (into {})))

(defmacro process
  "Alpha - subject to change.
  Allows to define the components of a process with the
  help of a short notation that is similar to the one of
  clojure.core/let. Example:

  (process [alpha 1
            beta (+ alpha 1)])
  will expand to:

  {:alpha 1
   :beta (process.definition/fnc [alpha] (+ alpha 1))}

  Destructuring is not supported at the moment."
  [bindings & {:keys [marker] :or {marker "$"}}]
  (let [refers (concat (keys (ns-refers *ns*))
                       (keys &env))]
    (build-process-components refers bindings)))