Elevator Simulation challenge
-------------------
Simulation of an elevator control system for a [job interview coding challenge](Challenge.pdf). 

### Algorithm

The algorithm aims to optimise an **average time needed to fulfil a request**. Where request is:
a call button, pushed on the floor (pick-ups), or destination button, 
pushed inside an elevator (drop-offs). The system has no inputs about actual passenger 
flow, so there are no optimisations in that regard.

Roughly, the algorithm is:
* Elevator travels in the same direction while its queue contains requests in that direction
* Then, if there are requests in opposite direction, it goes there
* If the queue is empty, elevator goes idle
    * An idle elevator has no direction ('.dir' returns 0)
    * it can still accept both pick-ups and drop-offs (i.e. doors are open)
* If an idle elevator gets request, it starts traveling in its direction
    * If there are several, the direction of the farthest one is chosen
* Requests queue of particular elevator consists of: 
    * it's drop-offs
    * pick-ups, for which, of all the elevators, this one is the closest
        * "closest" by [ETA](src/main/scala/xko/elevators/Lift.scala#L48), considering requests already 
          in the queue and current direction 
        * ETA can change anytime, so pick-ups get reassigned on every step
* Elevator stops at the floor to fulfil drop-offs there and pick-ups from there in the current direction 
  (regardless of the queue)
    * When stopped, it can resume only after 3 steps 

### Implementation

The public API is in [package object](/src/main/scala/xko/elevators/package.scala) of `xko.elevators` 
it follows the [challenge](Challenge.pdf) proposal, except the elevator status is a separate `Elevator` class. 
Implementation of the `Elevator` is `Lift` trait and its implementations for different states - all placed in 
the same [file](/src/main/scala/xko/elevators/Lift.scala). The `ControlSystem` is implemented by `Scheduler`

Other difference from proposed API, is immutable "functional" style. I.e. the request methods - `pickUp` and 
`dropOff` - as well as `proceed` (which advances to the next step), all return 
a new copy of an object and no mutable state is stored anywhere. 

The tests are located in [Spec.scala](/src/test/scala/xko/elevators/Spec.scala)

### Running and building

The project is [sbt](https://www.scala-sbt.org)-based. All sbt commands are available via included [starter
script](bin/sbt). E.g. `./bin/sbt test` 

