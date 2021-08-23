Elevator Simulation challenge
-------------------
Simulation of an elevator control system for a [job interview coding challenge](Challenge.pdf). 

### Algorithm

The algorithm aims to optimise an **average time needed to fulfil a request**. Where request is:
a call button, pushed on the floor (pick-ups), or destination button, 
pushed inside an elevator (drop-offs). The system has no inputs about actual passenger 
flow, so there are no optimisations in that regard.

Roughly, the algorithm is:
* On each step, the system iterates active pick-ups and assigns each to the *closest by ETA* elevator
    *  [ETA](src/main/scala/xko/elevators/Lift.scala#L51) is estimated number of steps to reach pick-up floor,
       considering the below details
* Particular elevators queue consists of its drops-off and the active pick-ups
    * drop-offs are kept in the queue until fulfilled, pick-ups are cleaned and reassigned anew each step
* Elevator keeps moving in the same direction while its queue contains requests in that direction
* Then, if there are requests in opposite direction, the direction is switched
* Elevator stops if:
    * it has drop-offs at current floor
    * system has pick-ups at current floor *in current direction* 
    * When stopped, it can resume after 3 steps
* Elevator with empty queue is idle and has no preferred direction
    * the new direction is chosen by the 1st request assigned 
        * if several assigned in one step - by majority of them
    
    
#### Motivation (Vs. First-Come-First-Served)

To explain, why this algorithm is (close to) optimal, let's consider distance in floors an elevator has to travel 
to fulfil certain request `R`. It depends on the floors `Q(...)` it has to visit before `R`, and can be expressed 
as a sum of the distances between consecutive floors, sth like (in pseudo-scala): 
`D = Q.sliding(2)( (prev,next)=>abs(next-prev) ).sum`. By moving elevators in the same direction as long as possible, 
serving requests *on the way*, we essentially order `Q` monotonously, thus minimizing every item of the sum. 

FCFS would order it only by time, making every item of the sum arbitrary large, i.e. an elevator would cover up to the entire
building height between each pair. 

### Implementation

The public API is in [package object](/src/main/scala/xko/elevators/package.scala) of `xko.elevators` 
it follows the [challenge](Challenge.pdf) proposal, except the elevator status is a separate `Elevator` class. 
All the API instances are immutable: the request methods - `pickUp` and `dropOff` - 
as well as `proceed` (advances to the next step), all return a new copy of `ControlSystem`, 
which should be used for further requests and advancing to the next step.

`Elevator` is implemented by `Lift` trait and its further implementations for different states - all placed in 
the same [file](/src/main/scala/xko/elevators/Lift.scala). The `ControlSystem` is implemented by 
[`Scheduler`](/src/main/scala/xko/elevators/Scheduler.scala)

### Building and testing

The project is [sbt](https://www.scala-sbt.org)-based. All sbt commands are available via included [starter
script](bin/sbt). E.g. `./bin/sbt test`. 

The tests are located in [Spec.scala](/src/test/scala/xko/elevators/Spec.scala)

