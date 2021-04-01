package xko

import scala.collection.immutable.SortedSet

package object elevators {
  /** State of an elevator at particular step */
  trait Elevator {
    /** Current floor */
    def floor: Int
    /** Current direction:  1 for UP, -1 for DOWN, 0 means elevator is idle  */
    def dir: Int
    /** True if elevator is currently stopped and passengers can board */
    def isStopped: Boolean
  }

  val Up: Int = 1
  val Down: Int = -1
  val Free: Int = 0

  /** State of an elevator control system at particular step */
  trait ControlSystem {
    def elevators: IndexedSeq[Elevator]
    /** Request a pick-up
     * @param floor where passenger is waiting
     * @param dir the direction in which passenger plans to go: 1 for UP, -1 for DOWN
     * @return new instance of ControlSystem with pick-up scheduled
     */
    def pickUp(floor: Int, dir: Int): ControlSystem
    /** Request a drop-off.
     * @return new instance of ControlSystem with drop-off scheduled
     */
    def dropOff(elevatorIndex: Int, floor: Int): ControlSystem

    /** True if system currently have no scheduled requests and all elevators are idle */
    def isIdle: Boolean

    /** Proceed to next step
     * @return new instance of ControlSystem representing the state at the next step
     */
    def proceed: ControlSystem

    /** Repeatedly transform this ControlSystem with f() */
    def iterate(f: ControlSystem => ControlSystem):Iterator[ControlSystem] = Iterator.iterate(this)(f)
    /** Skip number of steps
     * @return state of the system after <code>steps</code> steps
     */
    def proceed(steps: Int): ControlSystem= iterate(_.proceed).drop(steps).next()
  }

  object ControlSystem {
    /** Creates an new idle ControlSystem with elevators at specified floors */
    def apply(floors: Int*): ControlSystem = Scheduler(floors.toIndexedSeq.map(lift),Set.empty,0)
  }

  private[elevators] def lift(floor:Int) = Idle(floor)

  private[elevators] val StopDuration: Int = 3

  private[elevators] def reverse[T](s: SortedSet[T]) = s.map(identity)(s.ordering.reverse)

  private[elevators] implicit class IntUtil(v: Int) {
    def between(a: Int, b: Int): Boolean =  math.min(a, b) to math.max(a, b) contains v
    def dirSet(towards: Int): SortedSet[Int] = if(towards >= v) SortedSet.empty else reverse(SortedSet.empty)
  }


}
