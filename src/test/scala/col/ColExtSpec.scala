package col

import org.scalatest.{FlatSpec, Matchers}

class ColExtSpec extends FlatSpec with Matchers {

  it should "fold until" in {
    ColExt.foldUntil(List(1, 2, 3, 4, 5), 0) {
      (acc, next) => if (next < 4) (false, acc + next) else (true, acc)
    } shouldEqual (1 + 2 + 3)
  }

}
