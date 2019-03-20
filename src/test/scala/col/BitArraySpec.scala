package col

import org.scalatest.{FlatSpec, Matchers}

class BitArraySpec extends FlatSpec with Matchers {

  it should "store bits" in {
    val ar = new BitArray(16)
    ar.isSet(15) shouldEqual false
    ar.set(15)
    ar.isSet(15) shouldEqual true
    ar.unset(15)
    ar.isSet(15) shouldEqual false
  }

}
