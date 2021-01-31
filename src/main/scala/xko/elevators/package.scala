package xko

import scala.collection.immutable.SortedSet

package object elevators {
  trait Elevator {
    def floor: Int
    def dir: Int
    def isStopped: Boolean
  }

  trait ControlSystem[+E<:Elevator] {
    def elevators: IndexedSeq[E]
    def pickUp(floor: Int, dir: Int): ControlSystem[E]
    def dropOff(elevatorIndex: Int, floor: Int): ControlSystem[E]
    def isIdle: Boolean
    def proceed: ControlSystem[E]

    def proceed(steps: Int): ControlSystem[E] = Iterator.iterate(this)(_.proceed).drop(steps).next()
    def proceedTillIdle: ControlSystem[E] = Iterator.iterate(this)(_.proceed).dropWhile(!_.isIdle).next()
  }

  object ControlSystem {
    def apply(floors: Int*): ControlSystem[Elevator] = Scheduler(floors.toIndexedSeq.map(lift),Set.empty,0)
  }

  private[elevators] def lift(floor:Int) = Stopped(floor, SortedSet.empty, Set.empty, 0)

  private[elevators] val MinStopDuration: Long = 3

  private[elevators] def reverse[T](s: SortedSet[T]) = s.map(identity)(s.ordering.reverse)

  private[elevators] implicit class IntUtil(v: Int) {
    def between(a: Int, b: Int): Boolean =  math.min(a, b) to math.max(a, b) contains v
  }


}
