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
        * "closest" by [ETA](src/main/scala/xko/elevators/Lift.scala#L48), calculated considering requests already 
          in the queue and current direction 
        * ETA can change anytime, so pick-ups get reassigned on every step
* Elevator stops at the floor to fulfil drop-offs there and pick-ups from there *in the current direction* 
  (regardless of the queue)
    * When stopped, it can resume only after 3 steps 
    
#### Motivation (Vs. First-Come-First-Served)

Disclaimer: Although presented mathematically, the below is in no way a strict proof :)    

When the system receives request R, it has a queue Q of the requests to be served before R. Assume 
waiting time *Wt(R) = sum(D(q(i),q(i-1)))*, where *D* is distance in floors between consecutive requests in the queue. 
As FCFS orders the queue only by time, D can be arbitrary large, i.e. an elevator would cover up to the entire building 
height between each pair. By moving elevators in the same direction as long as possible, serving requests *on the way*, 
this system essentially orders *Q* monotonously by floor number, thus minimizing every *D* in the sum.

### Implementation

The public API is in [package object](/src/main/scala/xko/elevators/package.scala) of `relayr.elevators` 
it follows the [challenge](Challenge.pdf) proposal, except the elevator status is a separate `Elevator` class.  
Implementation of the `Elevator` is `Lift` trait and its implementations for different states - all placed in 
the same [file](/src/main/scala/xko/elevators/Lift.scala). The `ControlSystem` is implemented by `Scheduler`

Other difference from proposed API, is immutable "functional" style. I.e. the request methods - `pickUp` and 
`dropOff` - as well as `proceed` (which advances to the next step), all return 
a new copy of an object and no mutable state is stored anywhere. 

### Building and testing

The project is [sbt](https://www.scala-sbt.org)-based. All sbt commands are available via included [starter
script](bin/sbt). E.g. `./bin/sbt test` 

The tests are located in [Spec.scala](/src/test/scala/relayr/elevators/Spec.scala), can be run with `sbt test`

