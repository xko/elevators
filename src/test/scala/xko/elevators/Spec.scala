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
    val start = ControlSystem(0).pickUp(1, Up).pickUp(2, Up).pickUp(4, Up)
    val steps = Iterator.iterate(start)(_.proceed).take(12).toIndexedSeq
    steps(1).elevators(0).floor should be(1)
    steps(1).elevators(0).isStopped should be(true)
    steps(5).elevators(0).floor should be(2)
    steps(6).elevators(0).isStopped should be(true)
    steps(7).elevators(0).isStopped should be(true)
    steps(9).elevators(0).floor should be(3)
    steps(9).elevators(0).isStopped should be (false)
    steps(10).elevators(0).floor should be(4)
    steps(11).isIdle should be (true)
  }

  it should "keep direction" in {
    val start = ControlSystem(2).pickUp(4,Down) // will wait till requests up are served
                                .pickUp(5,Up)   // served 1st
    val step4 = start.proceed(4)
    step4.elevators(0).floor should be(5)
    val step9 = step4.dropOff(0, 7)             // served 2nd
                     .proceed(5)
    step9.elevators(0).floor should be(7)
    val step15 = step9.proceed(6)
    step15.elevators(0).floor should be(4)
    step15.proceed.isIdle should be (true)
  }
}