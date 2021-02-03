Elevator Simulation challenge
-------------------
Simulation of an elevator control system for a [job interview coding challenge](Challenge.pdf). 

### Algorithm

The algorithm aims to optimise an average time needed to fulfil a request, where request is a call button,
pushed on the floor, or destination button, pushed inside an elevator. The system has no inputs about actual passenger 
flow, and has no optimisations in that regard.

Algorithm can be roughly described like this:
* Elevator travels in the same direction while it has requests in that direction, 
  then it switches direction to the opposite
* If no more requests, elevator goes idle
* If an idle elevator gets requests, it goes in the direction of the farthest one 
* Requests to particular elevator are: 
    * it's drop-off requests
    * pick-up requests, for which this elevator is the closest
        * "closest" means minimum [ETA](src/main/scala/xko/elevators/Lift.scala#L67) to particular floor
        * this can change on every step, so these get reassigned on every step
* Elevator stops at the floor if it has drop-off requests there, or there are pick-up requests from this floor in its current 
  direction
* When stopped, it can resume only after 3 steps 

### Implementation

The public API is in [package object](/src/main/scala/xko/elevators/package.scala) of `xko.elevators` 
it follows the [challenge](Challenge.pdf) proposal, except the elevator status is a separate `Elevator` class. 
Implementation of the `Elevator` is `Lift` trait and its implementations for different states - all placed in 
the same [file](/src/main/scala/xko/elevators/Lift.scala). The `ControlSystem` is implemented by `Scheduler`

Other difference from proposed API, is immutable "functional" style. I.e. the request methods - `pickUp` and 
`dropOff` - as well as `proceed` (which advanced to the next step), all return 
a new copy of an object and no mutable state is stored anywhere. 

### Running and building

The project is [sbt](https://www.scala-sbt.org)-based. All sbt commands are available via included [starter
script](bin/sbt). E.g. `./bin/sbt test` 

