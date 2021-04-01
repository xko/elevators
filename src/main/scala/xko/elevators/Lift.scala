package xko.elevators

import scala.collection.immutable.SortedSet
import math.abs

trait Lift extends Elevator {
  def requestDrop(floor: Int): Lift
  def requestPicks(pickups: Iterable[PickUp]): Lift
  def willPickNow(pup: PickUp): Boolean
  def eta(pup: PickUp): Long

  def proceed: Lift
  def mayStop(pups: Iterable[PickUp]): Lift

}

case class Idle(floor: Int) extends Lift {
  override def dir: Int = 0
  override def isStopped: Boolean = true

  override def requestDrop(floor: Int): Lift = Stopped(floor, this.floor.dirSet(floor) + floor, Set.empty, 0)

  override def requestPicks(pickups: Iterable[PickUp]): Lift = {
    val (below,above) = pickups.filterNot(_.floor == this.floor).partition(_.floor < this.floor)
    if(below.isEmpty && above.isEmpty) this
    else if(below.size > above.size) Stopped(floor, floor.dirSet(floor-1), pickups.toSet, 0)
    else Stopped(floor, floor.dirSet(floor+1), pickups.toSet, 0)
  }

  override def willPickNow(pup: PickUp): Boolean = pup.floor == this.floor
  override def eta(pup: PickUp): Long = abs(pup.floor - floor)

  override def proceed: Lift = this
  override def mayStop(pups: Iterable[PickUp]): Lift = this
}


trait Active extends Lift {
  def dropoffs: SortedSet[Int]
  override val dir: Int = dropoffs.ordering.max(1, -1)
  def pickups: Set[PickUp]

  def canPickNow(pup: PickUp): Boolean = pup.floor == floor && (pup.dir == dir || planAhead.isEmpty)

  lazy val planAhead: SortedSet[Int] = (dropoffs ++ pickups.map(_.floor)).rangeFrom(floor+1)
  lazy val farthestAhead: Int = planAhead.lastOption.getOrElse(floor)
  lazy val stopsAhead: SortedSet[Int] = (dropoffs ++ pickups.filter(_.dir == dir).map(_.floor)).rangeFrom(floor+1)

  lazy val planBehind: SortedSet[Int] = (dropoffs ++ pickups.map(_.floor)).rangeUntil(floor)

  override def eta(pup: PickUp): Long =
    if (pup.dir == dir && pup.floor.between(floor, farthestAhead))
      stopsAhead.rangeUntil(pup.floor).size * StopDuration + abs(pup.floor - floor)
    else
      stopsAhead.size * StopDuration + abs(farthestAhead - floor) + abs(farthestAhead - pup.floor)

  override def proceed: Lift =  if (planAhead.nonEmpty) Moving(floor + dir, dropoffs, pickups)
                                else if (planBehind.nonEmpty) Moving(floor - dir, reverse(dropoffs), pickups)
                                else Idle(floor)
}

case class Stopped(floor: Int, dropoffs: SortedSet[Int], pickups: Set[PickUp], canGoIn: Long) extends Active {
  val isStopped: Boolean = true

  override def requestDrop(floor: Int): Stopped = if(floor == this.floor) this else copy(dropoffs = dropoffs + floor)

  override def requestPicks(pickups: Iterable[PickUp]): Lift = copy(pickups= pickups.filterNot(canPickNow).toSet)

  override def willPickNow(pup: PickUp): Boolean = canPickNow(pup)
  override def eta(pup: PickUp): Long = super.eta(pup) + canGoIn

  override def proceed: Lift = if (canGoIn > 0) copy(canGoIn = canGoIn - 1) else super.proceed
  override def mayStop(pups: Iterable[PickUp]): Stopped = this
}

case class Moving(floor: Int, dropoffs: SortedSet[Int], pickups: Set[PickUp]) extends Active {
  val isStopped: Boolean = false

  override def requestDrop(floor: Int): Moving = copy(dropoffs = dropoffs + floor)

  override def requestPicks(pickups: Iterable[PickUp]): Moving = copy(pickups = pickups.toSet)

  override def willPickNow(pup: PickUp): Boolean = false

  override def mayStop(pups: Iterable[PickUp]): Active =
    if (pups.exists(canPickNow)) Stopped(floor, dropoffs - floor, pickups, StopDuration)
    else if (dropoffs.contains(floor)) Stopped(floor, dropoffs - floor, pickups, StopDuration)
    else this
}