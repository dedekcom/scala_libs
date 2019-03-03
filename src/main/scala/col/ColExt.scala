package col

import scala.annotation.tailrec

object ColExt {

  def foldUntil[A, B](tr: Traversable[A], initial: B)(fun: (B, A) => (Boolean, B)): B = {
    @tailrec
    def loop(curTr: Traversable[A], curRes: B): B = {
      if (curTr.isEmpty)
        curRes
      else {
        fun(curRes, curTr.head) match {
          case (true, res)  => res
          case (false, res) => loop(curTr.tail, res)
        }
      }
    }
    loop(tr, initial)
  }

}
