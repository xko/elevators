Elevator Simulation challenge
-------------------
Simulation of an elevator control system for a [job interview coding challenge](Challenge.pdf). 

### Running and building

The project is [sbt](https://www.scala-sbt.org)-based. All sbt commands are available via included [starter
script](bin/sbt). E.g. `./bin/sbt test` 

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
        * "closest" means minimum [ETA](src/main/scala/xko/elevators/Lift.scala#:~:text={def eta}) to particular floor
        * this can change on every step, so these get reassigned on every step
* Elevator stops at the floor if it has drop-off requests there, or there are pick-up requests from this floor in its current 
  direction
* When stopped, it can resume only after 3 steps 

### Implementation

TBD
    