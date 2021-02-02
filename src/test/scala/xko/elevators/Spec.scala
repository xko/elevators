package xko.elevators

import org.scalatest.Inside
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.{BeMatcher, BePropertyMatchResult, BePropertyMatcher, MatchResult, Matcher}
import org.scalatest.matchers.should.Matchers

class Spec extends AnyFlatSpec with Matchers with Inside{

  object stopped extends BeMatcher[Elevator] {
    override def apply(left: Elevator): MatchResult = MatchResult(left.isStopped,"was moving", "was stopped")
  }
  val moving: BeMatcher[Elevator] = not(stopped)

  def beAt(floor: Int): Matcher[Elevator] =Matcher[Elevator] { e =>
    MatchResult(e.floor == floor, s"was at ${e.floor}", s"was at $floor")
  }

  val idle: BePropertyMatcher[ControlSystem[_]] =
    BePropertyMatcher[ControlSystem[_]] (cs => BePropertyMatchResult(cs.isIdle, "idle"))


  it should "simply pickup" in {
    val before = ControlSystem(3,3,3)
    val after = before.pickUp(4,1).proceed
    after.elevators(0).floor should be(4)
    after.elevators(0).isStopped should be(true)
  }

  it should "collect way up" in {
    val start = ControlSystem(0).pickUp(1, Up).pickUp(2, Up).pickUp(4, Up)
    val steps = Iterator.iterate(start)(_.proceed).take(15).toIndexedSeq
    steps(1).elevators(0)  should (beAt(1) and be(stopped) )
    steps(5).elevators(0)  should (beAt(2) and be(stopped) )
    steps(6).elevators(0)  should (beAt(2) and be(stopped) )
    steps(9).elevators(0)  should (beAt(3) and be(moving) )
    steps(10).elevators(0) should (beAt(4) and be(stopped) )
    steps(14) shouldBe idle
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

  it should "use 2nd elevator" in {
    val start = ControlSystem(2,1).pickUp(4,Down).pickUp(5,Up)
    start.proceed(2).elevators(0) should (beAt(4) and be(moving))
    start.proceed(2).elevators(1) should (beAt(2) and be(moving))
    start.proceed(3).elevators(0) should (beAt(5) and be(stopped))
    start.proceed(3).elevators(1) should (beAt(3) and be(moving))
    start.proceed(4).elevators(0) should (beAt(5) and be(stopped))
    start.proceed(4).elevators(1) should (beAt(4) and be(stopped))
    start.proceed(8) shouldBe idle
  }
}
