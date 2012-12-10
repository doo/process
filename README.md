# process

Process is a library inspired by Prismatic's Graph and Stuart Sierra's Flow.

### See

* http://blog.getprismatic.com/blog/2012/10/1/prismatics-graph-at-strange-loop.html
* https://github.com/stuartsierra/flow

for background information.

## Usage

    (def process-definition
      {:result (fnc [gamma delta epsilon] (+ gamma delta epsilon))
       :gamma (fnc [alpha beta] (+ alpha beta))
       :delta (fnc [alpha gamma] (+ alpha gamma))
       :epsilon (fnc [gamma delta] (+ gamma delta))})
 
    > (execute-sequential process-definition {:alpha 1 :beta 2} :result)
    {:result 14, :epsilon 7, :delta 4, :gamma 3, :alpha 1, :beta 2}

There is also a short notation (still alpha) to define a process:

    (def process-definition
      (process
       [result (+ gamma delta epsilon)
        gamma (+ alpha beta)
        delta (+ alpha gamma)
        epsilon (+ gamma delta)]))

    > (execute-sequential process-definition {:alpha 1 :beta 2} :result)
    {:result 14, :epsilon 7, :delta 4, :gamma 3, :alpha 1, :beta 2}

## TODOs

* Comprehensive documentation

## License

Copyright © 2012 Maximilian Weber and doo GmbH

Distributed under the Eclipse Public License, the same as Clojure.
