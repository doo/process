(ns process.execution
  (:use [process.definition :only [get-component-dependencies
                                   get-missing-dependencies
                                   missing-component-dependencies]]
        [slingshot.slingshot :only [throw+]])
  (:require [clojure.set :as set]))

(defn- check-component-dependencies
  "Checks if all component dependencies are available."
  [fnc available-outputs]
  (let [mcd (missing-component-dependencies fnc available-outputs)]
    (when-not (empty? mcd)
      (throw+ {:type ::can-not-resolve-component-dependencies
               :missing-component-dependencies mcd
               :available-outputs-keys (keys available-outputs)}))))

(defn invoke-fnc
  "Invokes a process component function. The value for the function
   arguments are looked up in the available-outputs map, which should
   contain the outputs of the process components that has been
   executed so far."
  [fnc available-outputs]
  {:pre [(map? available-outputs)]}
  (check-component-dependencies fnc available-outputs)
  (let [dependencies (get-component-dependencies fnc)
        args (map available-outputs dependencies)]
    (apply fnc args)))

(defn check-for-missing-dependencies
  "Checks if all dependencies of a process definition are satisfied by the
   outputs of the process components."
  [process-definition existing-outputs outputs]
  (let [missing (get-missing-dependencies (merge process-definition existing-outputs) outputs)]
    (when (seq missing)
      (throw+ {:type ::missing-dependencies
               :missing-dependencies missing}))))