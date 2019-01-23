package col


case class Tree[+A](node: A, children: List[Tree[A]]) {

  def foldTopDown[B](initial: B)(fun: (B, A) => B): B = {
    def loop(acc: B, n: List[Tree[A]]): B = n match {
      case Nil => acc
      case h :: tail =>
        val res = h.foldTopDown(acc)(fun)
        loop(res, tail)
    }
    loop(fun(initial, node), children)
  }

  def find(fun: A => Boolean): Option[A] = {
    def loop(n: List[Tree[A]]): Option[A] = {
      if (fun(node)) Some(node) else n match {
        case Nil => None
        case h :: tail => h.find(fun) match {
          case Some(res) => Some(res)
          case None => loop(tail)
        }
      }
    }
    loop(children)
  }

  def findSubTree(fun: A => Boolean): Option[Tree[A]] = {
    def loop(n: List[Tree[A]]): Option[Tree[A]] = {
      if (fun(node)) Some(this) else n match {
        case Nil => None
        case h :: tail => h.findSubTree(fun) match {
          case Some(res) => Some(res)
          case None => loop(tail)
        }
      }
    }
    loop(children)
  }

  def add[B >: A](child: B): Tree[B] = Tree(node, Tree(child, Nil) :: children)

  def add[B >: A](child: Tree[B]): Tree[B] = Tree(node, child :: children)

  def append[B >: A](child: B): Tree[B] = Tree(node, children :+ Tree(child, Nil))

  def append[B >: A](child: Tree[B]): Tree[B] = Tree(node, children :+ child)

  def exists(fun: A => Boolean): Boolean = find(fun).isDefined

  def map[B](fun: A => B): Tree[B] = Tree(fun(node), children.map(_.map(fun)))

  def filter(fun: A => Boolean): Tree[A] = Tree(node, children.filter(e => fun(e.node)).map(_.filter(fun)))

  def removeAll[B >: A](element: B): Tree[A] = filter(nd => nd != element)

  def zipWithLevel: Tree[(A, Int)] = {
    def loop(nd: Tree[A], level: Int): Tree[(A, Int)] =
      Tree((nd.node, level), nd.children.map(n => loop(n, level + 1)))
    loop(this, 0)
  }

  def zipWithLevelAndIndex: Tree[(A, (Int, Int))] = {
    def loop(nd: Tree[A], level: Int, index: Int): Tree[(A, (Int, Int))] = {
      Tree((nd.node, (level, index)),
        nd.children.foldLeft((List[Tree[(A, (Int, Int))]](), 0)) {
          case ((acc, id), n) => (loop(n, level + 1, id) :: acc, id+1)
        }._1.reverse
      )
    }
    loop(this, 0, 0)
  }

  def isLeaf: Boolean = children.isEmpty

  def reverse: Tree[A] = Tree(node, children.reverse.map(_.reverse))

  def size: Int = foldTopDown(0) { (acc, _) => acc + 1 }

  def levels: Int = zipWithLevel.foldTopDown(0) {
    case (cnt, (_, level)) => if (level > cnt) level else cnt
  } + 1

  def foldBottomUp[B >: A, C](element: B, initial: C)(fun: (C, B) => C): C =
    loopFoldTop(initial, children, element, found = false)(fun)._1

  private def loopFoldTop[B >: A, C](acc: C, n: List[Tree[B]], element: B, found: Boolean)(fun: (C, B) => C): (C, Boolean) = {
    if (found || element == node) {
      (fun(acc, node), true)
    } else {
      n match {
        case Nil => (acc, found)
        case h :: tail =>
          val (res, fnd) = h.loopFoldTop(acc, h.children, element, found)(fun)
          if (fnd) {
            (fun(res, node), true)
          } else {
            loopFoldTop(acc, tail, element, found)(fun)
          }
      }
    }
  }

}
