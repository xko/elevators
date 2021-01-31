package xko.elevators

import scala.collection.immutable.SortedSet
import math.abs

trait Lift[State <: Lift[State]] extends Elevator { state: State =>
  def isStopped: Boolean
  def floor: Int
  def dropoffs: SortedSet[Int]
  lazy val dir: Int = dropoffs.ordering.max(1, -1)
  def pickups: Set[Int]

  def dropOff(floor: Int): Lift[State] = if(isStopped && floor == this.floor) this else updated(dropOffs = dropoffs + floor)

  def canPickNow(pup: PickUp): Boolean = pup.floor == floor && pup.dir == dir
  def willPickNow(pup: PickUp):Boolean = canPickNow(pup) && isStopped

  def updated(floor:Int = floor, dropOffs: SortedSet[Int] = dropoffs, pickups: Set[Int] = pickups): State

  lazy val plan: SortedSet[Int] = dropoffs ++ pickups
  lazy val planAhead: SortedSet[Int] = plan.rangeFrom(floor+1)
  lazy val farthestAhead: Int = planAhead.lastOption.getOrElse(floor)

  def eta(pup: PickUp): Long =
    if (pup.dir == dir && pup.floor.between(floor, farthestAhead))
      planAhead.rangeUntil(pup.floor).size * MinStopDuration + abs(pup.floor - floor)
    else
      planAhead.size * MinStopDuration + abs(farthestAhead - floor) + abs(farthestAhead - pup.floor)

  def planPickups(allPups: Iterable[PickUp], allLifts: Iterable[Lift[_]]): Lift[State] = updated(
    pickups = allPups.filter( p => allLifts.minBy(_.eta(p)) eq this )
                     .filterNot(willPickNow).map(_.floor).toSet
  )

  def forward: Lift[_] = Moving(floor + dir, dropoffs, pickups)

  def turn: Lift[_] = Moving(floor - dir, reverse(dropoffs), pickups)

  def stopOnReq(pups: Iterable[PickUp]): Lift[_]


  def proceed: Lift[_]
}

case class Stopped(floor: Int, dropoffs: SortedSet[Int], pickups: Set[Int], canGoIn: Long) extends Lift[Stopped] {
  val isStopped: Boolean = true

  override def updated(floor: Int, dropoffs: SortedSet[Int], pickups: Set[Int]): Stopped =
    copy(floor = floor, dropoffs = dropoffs, pickups = pickups)


  override def stopOnReq(pups: Iterable[PickUp]): Lift[_] = this

  override def proceed: Lift[_] = if (canGoIn > 0) copy(canGoIn = canGoIn - 1)
                                  else if (plan.rangeFrom(floor + 1).nonEmpty) forward
                                  else if (plan.rangeUntil(floor).nonEmpty) turn
                                  else this
}

case class Moving(floor: Int, dropoffs: SortedSet[Int], pickups: Set[Int]) extends Lift[Moving] {
  val isStopped: Boolean = false

  override def updated(floor: Int, dropoffs: SortedSet[Int], pickups: Set[Int]): Moving =
    copy(floor = floor, dropoffs = dropoffs, pickups = pickups)

  def stopOnReq(pups: Iterable[PickUp]): Lift[_] =
    if (pups.exists(canPickNow)) Stopped(floor, dropoffs, pickups, MinStopDuration)
    else if (dropoffs.contains(floor)) Stopped(floor, dropoffs - floor, pickups, MinStopDuration)
    else this

  override def proceed: Lift[_] = forward
}