package xko

import scala.collection.immutable.SortedSet

package object elevators {
  trait Elevator {
    def floor: Int
    def dir: Int
    def isStopped: Boolean
  }

  val Up: Int = 1
  val Down: Int = -1
  val Free: Int = 0

  trait ControlSystem {
    def elevators: IndexedSeq[Elevator]
    def pickUp(floor: Int, dir: Int): ControlSystem
    def dropOff(elevatorIndex: Int, floor: Int): ControlSystem
    def isIdle: Boolean
    def proceed: ControlSystem

    def iterate: (ControlSystem => ControlSystem) => Iterator[ControlSystem] = Iterator.iterate(this)
    def proceed(steps: Int): ControlSystem= iterate(_.proceed).drop(steps).next
  }

  object ControlSystem {
    def apply(floors: Int*): ControlSystem = Scheduler(floors.toIndexedSeq.map(lift),Set.empty,0)
  }

  private[elevators] def lift(floor:Int) = Idle(floor)

  private[elevators] val StopDuration: Int = 3

  private[elevators] def reverse[T](s: SortedSet[T]) = s.map(identity)(s.ordering.reverse)

  private[elevators] implicit class IntUtil(v: Int) {
    def between(a: Int, b: Int): Boolean =  math.min(a, b) to math.max(a, b) contains v
    def towards(t: Int): SortedSet[Int] = if(t >= v) SortedSet.empty else reverse(SortedSet.empty)
  }


}
