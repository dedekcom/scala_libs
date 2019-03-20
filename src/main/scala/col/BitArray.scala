package col

class BitArray(val length: Int) {

  val array: Array[Long] =
    index(length) match {
      case (pos, 0) => Array.fill(pos)(0L)
      case (pos, _) => Array.fill(pos + 1)(0L)
    }

  def index(id: Int): (Int, Int) = (id / 64, id % 64)

  def set(id: Int): Unit = index(id) match {
    case (pos, shift) => array(pos) = array(pos) | getMask(shift)
  }

  def unset(id: Int): Unit = index(id) match {
    case (pos, shift) => array(pos) = array(pos) & (~getMask(shift))
  }

  def nonSet(id: Int): Boolean = index(id) match {
    case (pos, shift) => (array(pos) & getMask(shift)) == 0L
  }

  def isSet(id: Int): Boolean = !nonSet(id)

  def getMask(bitPos: Int): Long = 1L << bitPos

}
