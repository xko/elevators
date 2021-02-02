package xko.elevators

case class PickUp(floor: Int, dir: Int)

case class Scheduler(lifts: IndexedSeq[Lift], pickups: Set[PickUp], now: Long) extends ControlSystem[Lift] {

  override def dropOff(elevatorIndex: Int, floor: Int): ControlSystem[Lift] =
    copy(lifts = (lifts.take(elevatorIndex) :+ lifts(elevatorIndex).requestDrop(floor)) ++ lifts.drop(elevatorIndex+1))

  override def pickUp(floor: Int, dir: Int): ControlSystem[Lift] = copy(pickups = pickups + PickUp(floor, dir))

  override def elevators: IndexedSeq[Lift] = lifts

  override def isIdle: Boolean = lifts.forall(_.dir == Free) && pickups.isEmpty

  def proceed: Scheduler = {
    val npickups = pickups.filterNot(p => lifts.exists(_.willPickNow(p)))
    Scheduler(lifts.map { l =>
                  l.requestPicks(npickups.filter(p => lifts.minBy(_.eta(p)) eq l))
               }.map(_.proceed.mayStop(npickups)),
              npickups, now + 1)
  }

}