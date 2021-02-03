package xko.elevators

import org.scalatest.Inside
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.{BeMatcher, BePropertyMatchResult, BePropertyMatcher, MatchResult, Matcher}
import org.scalatest.matchers.should.Matchers

class Spec extends AnyFlatSpec with Matchers with Inside{

  val stopped = BeMatcher[Elevator](e => MatchResult(e.isStopped,"was moving", "was stopped"))
  val moving = not(stopped)

  def beAt(floor: Int) =
    Matcher[Elevator](e => MatchResult(e.floor == floor, s"was at ${e.floor}", s"was at ${e.floor}"))

  val idle = BePropertyMatcher[ControlSystem] (cs => BePropertyMatchResult(cs.isIdle, "idle"))


  it should "simply pickup" in {
    val before = ControlSystem(3,3,3)
    val after = before.pickUp(4,1).proceed
    after.elevators(0) should (beAt(4) and be(stopped))
  }

  it should "collect way up" in {
    val start = ControlSystem(0).pickUp(1, Up).pickUp(2, Up).pickUp(4, Up)

    start.proceed(1).elevators(0)  should (beAt(1) and be(stopped) )
    start.proceed(5).elevators(0)  should (beAt(2) and be(stopped) )
    start.proceed(6).elevators(0)  should (beAt(2) and be(stopped) )
    start.proceed(9).elevators(0)  should (beAt(3) and be(moving) )
    start.proceed(10).elevators(0) should (beAt(4) and be(stopped) )
    start.proceed(14) shouldBe idle
  }

  it should "use 2nd elevator" in {
    val start = ControlSystem(2,1).pickUp(4,Down).pickUp(5,Up)

    start.proceed(2).elevators(0) should (beAt(4) and be(moving))
    start.proceed(3).elevators(0) should (beAt(5) and be(stopped))
    start.proceed(4).elevators(0) should (beAt(5) and be(stopped))

    start.proceed(2).elevators(1) should (beAt(2) and be(moving))
    start.proceed(3).elevators(1) should (beAt(3) and be(moving))
    start.proceed(4).elevators(1) should (beAt(4) and be(stopped))
    start.proceed(8) shouldBe idle
  }

  it should "keep direction" in {
    val start = ControlSystem(2).pickUp(4,Down) // will wait till up-requests are served
                                .pickUp(5,Up)   // served 1st
    val at5 = start.proceed(3)
    at5.elevators(0) should (beAt(5) and be(stopped))
    val at7 = at5.dropOff(0, 7)                 // served 2nd, the guy at 4th still waits
                 .proceed(StopDuration + 2)
    at7.elevators(0) should (beAt(7) and be(stopped))
    val at4 = at7.proceed(StopDuration + 3)
    at4.elevators(0) should (beAt(4) and be(stopped))
    at4.proceed(StopDuration+1) shouldBe idle
  }

  implicit class PassengerBehavior(cs:ControlSystem) {
    def ride(from: Int, to: Int): ControlSystem = { // board and request drop-off
      val i = cs.elevators.indexWhere (e => e.floor == from && e.isStopped && e.dir * (to - from) >= 0)
      if (i >= 0) cs.dropOff(i, to) else cs
    }

    def hijack(from: Int, to: Int): ControlSystem = {              // same, but ignore direction:
      val i = cs.elevators.indexWhere(e => e.floor == from && e.isStopped)   // request up in the downward lift too
      if (i >= 0) cs.dropOff(i, to) else cs
    }
  }

  it should "prefer local pickups" in {
    val start = ControlSystem(100,0).pickUp(125,Down).pickUp(126, Up).pickUp(81,Down)
                                    .pickUp(-2,Down).pickUp(12,Up).pickUp(8,Up)
    val steps = start.iterate { cs =>
      cs.ride(126, 130).ride(125, 80).ride(81, 75)
        .ride(12, 10).ride(8, 20).ride(-2, -3)
        .proceed
    }.take(200).toIndexedSeq

    steps.last shouldBe idle
    val upperStops = steps.filter(_.elevators(0).isStopped).map(_.elevators(0).floor).distinct
    val lowerStops = steps.filter(_.elevators(1).isStopped).map(_.elevators(1).floor).distinct
    upperStops should equal (IndexedSeq(100,126,130,125,81,80,75))
    lowerStops should equal (IndexedSeq(0,8,12,20,-2,-3))
  }

  it should "serve hijacks too, but later" in {
    val start = ControlSystem(100,0).pickUp(125,Down).pickUp(126, Up).pickUp(81,Down)
    val steps = start.iterate { cs =>
      cs.ride(126, 130).hijack(126,122)
        .ride(125, 80).ride(81, 75).ride(12, 10).ride(8, 20).ride(-2, -3)
        .proceed
    }.take(200).toIndexedSeq
    val upperStops = steps.filter(_.elevators(0).isStopped).map(_.elevators(0).floor).distinct
    upperStops should equal (IndexedSeq(100,126,130,125,122,81,80,75))
  }
}
