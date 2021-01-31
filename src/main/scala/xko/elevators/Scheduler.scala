package xko.elevators

case class PickUp(floor: Int, dir: Int)

case class Scheduler(lifts: IndexedSeq[Lift[_]], pickups: Set[PickUp], now: Long) extends ControlSystem[Lift[_]] {

  override def dropOff(elevatorIndex: Int, floor: Int): ControlSystem[Lift[_]] =
    copy( lifts = (lifts.take(elevatorIndex) :+ lifts(elevatorIndex).dropOff(floor)) ++ lifts.drop(elevatorIndex+1) )

  override def pickUp(floor: Int, dir: Int): ControlSystem[Lift[_]] = copy(pickups = pickups + PickUp(floor, dir))

  override def elevators: IndexedSeq[Lift[_]] = lifts

  override def isIdle: Boolean = lifts.forall(_.plan.isEmpty) && pickups.isEmpty && lifts.forall(_.isStopped)

  def proceed: Scheduler = Scheduler( lifts.map(_.planPickups(pickups, lifts).proceed.stopOnReq(pickups)),
                                      pickups.filter(p => lifts.exists(_.willPickNow(p))),
                                      now + 1 )

}