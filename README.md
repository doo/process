# process

Process is a library inspired by the core idea of Prismatic's
[Graph](http://blog.getprismatic.com/blog/2012/10/1/prismatics-graph-at-strange-loop.html)
library. For that reason it also has a lot of goals in common with
Stuart Sierra's [Flow](https://github.com/stuartsierra/flow) library.
You should read Prismatic's [blog
post](http://blog.getprismatic.com/blog/2012/10/1/prismatics-graph-at-strange-loop.html)
about Graph to get an idea what benefits you can leverage if you
use this fine-grained, composable abstraction
([FCA](http://blog.getprismatic.com/blog/2012/4/5/software-engineering-at-prismatic.html)).
What it comes down to is, that you can compose different steps of a
computation in a more flexible way than using a let for that
purpose. In a real life system these computations or rather processes
often involve a lot of steps. Normally you end up with a function
that contains a huge let statement (kind of a 'monster'-let) that
composes all these steps. This construct is totally inflexible, if you
like to do anything more than to execute this ball of mud. Especially,
if you have discovered a bug that is caused by some of the
composed steps or maybe just by some missing data. Most often the fastest
way is to add some `println` statements in between the steps of the
'monster'-let to do some ad-hoc debugging. The FCA of the process
library helps you to compose all these steps in a flexible way, so
that you can access each and every interim result of the
computation. It even allows you to evaluate the computation only up to
a certain point or to predefine the output of a step. The latter is
especially useful if the corresponding step represents a side effect
that you like to "mock out". In terms of the simplicity notion you
could say the FCA of the process library helps you to separate the
declarative part of how the steps of a process depend on each other
from the part of how the process is executed in the end. You could choose
to just execute the steps of a process sequentially like a let
statement or you can write an execution strategy that figures out which
steps can be calculated in parallel. Among other things we use the
process library to split up some complex event processing code
into simple steps. A lot of steps produces interim results that are
needed by different computations to calculate their end result. By using
process the interim results can easily be shared between the different
computations and the execution strategy can be adapted to higher
performance demands independently from the actual implementation of
the calculation. There are a whole lot of other benefits like
transparently enhancing the execution of a process with performance
monitoring stuff and so on, that are all described in the
aforementioned blog post by Prismatic about their Graph library. One
last point I like to mention is that this FCA really helps a lot to
separate the pure functional parts of a process from the impure parts
or rather steps with side effects.

## Usage

To define a step or rather component of a process you can use the
`fnc` macro that just adds a `:process.definition/dependencies` entry
(a vector of keywords) to the metadata of the function. So `(def f
(fnc [alpha beta] (+ alpha beta)))` just states that the function `f`
depends on two outputs named `alpha` and `beta` as inputs to do its
calculation. `alpha` or `beta` could either be an output of another
process component or they can be inputs for the process itself. Here
the example that is also used for the Flow library:

    (use 'process.definition)

    (def process-definition
     {:result (fnc [gamma delta epsilon] (+ gamma delta epsilon))
      :gamma (fnc [alpha beta] (+ alpha beta))
      :delta (fnc [alpha gamma] (+ alpha gamma))
      :epsilon (fnc [gamma delta] (+ gamma delta))})

    (use 'process.execution.sequential)
 
    > (execute-sequential process-definition {:alpha 1 :beta 2} :result)
    {:result 14, :epsilon 7, :delta 4, :gamma 3, :alpha 1, :beta 2}

In the example above `alpha` and `beta` are both inputs for the
process. The component `gamma` just depends on `alpha` and `beta`,
while the component `delta` needs the output of gamma to perform its
calculation. Therefore it is clear that `gamma` needs to be calculated
first before the `delta` component function can be invoked. The
`execute-sequential` function uses the dependency graph from the
org.clojure/tools.namespace library and a level order traversal to
find a sequence to execute the process, so that all outputs of the
component's dependencies are available when a component function is
invoked. Through the second parameter of the `execute-sequential`
function you define the 'existing' outputs of the process execution,
here we just say that the outputs of `alpha` and `beta` are already
calculated. The remaining arguments of the function define which
outputs you like to calculate. Here we just want to know what the
result is. However you could also only execute the process partially:

    > (execute-sequential process-definition {:alpha 1 :beta 2} :delta)
    {:delta 4, :gamma 3, :alpha 1, :beta 2}

As you can see above only the outputs are calculated to perform the
computation of the delta component. Furthermore you also can predefine
the output of components:

    > (execute-sequential process-definition {:alpha 1 :beta 2 :gamma 1} :delta)
    {:delta 2, :alpha 1, :beta 2, :gamma 1}

If gamma would perform some side effect - for example querying a
database - you can easily predefine the output to avoid the execution
of the side effect (e.g. in your test scenario).

To become more similar to a let the process macro has been added to
the library (still alpha):

    (def process-definition
      (process
       [alpha nil
        beta 2
        gamma (+ alpha beta)
        delta (+ alpha gamma)
        epsilon (+ gamma delta)
        result (+ gamma delta epsilon)]))

    > (execute-sequential process-definition {:alpha 1} :result)
    {:result 14, :epsilon 7, :delta 4, :gamma 3, :beta 2, :alpha 1}

In comparison to our first process-definition this version has much
less boilerplate code. The `process` macro needs to be aware of the
inputs so that it can figure out if you mean a dependency or just a
symbol from the context (a var defined in the current namespace or a
binding in a surrounding let). Here we define `alpha` as nil while
`beta` has a default value of `2`. Like any other component `beta`
can also be predefined via the existing-outputs parameter of the
`execute-sequential` function.

## License

Copyright © 2012 Maximilian Weber and doo GmbH

Distributed under the Eclipse Public License, the same as Clojure.
