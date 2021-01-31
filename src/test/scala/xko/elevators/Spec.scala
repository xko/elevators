package xko.elevators

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class Spec extends AnyFlatSpec with Matchers {
  it should "simply pickup" in {
    val before = ControlSystem(3,3,3)
    val after = before.pickUp(4,1).proceed
    after.elevators(0).floor should be(4)
    after.elevators(0).isStopped should be(true)
  }

  it should "collect way up" in {
    val before = ControlSystem(0).pickUp(1, 1).pickUp(2, 1).pickUp(4, 1)
    val after = before.proceed(5)
    after.elevators(0).floor should be(4)
  }
}
