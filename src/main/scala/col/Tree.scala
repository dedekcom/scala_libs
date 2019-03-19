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

  def foldUntil[B](initial: B)(fun: (B, A) => (Boolean, B)): B = {
    def loop(acc: (Boolean, B), n: List[Tree[A]]): (Boolean, B) =
      if(acc._1)
        acc
      else
        n match {
          case Nil => acc
          case h :: tail =>
            loop(fun(acc._2, h.node), h.children) match {
              case (true, res)  => (true, res)
              case (false, res) => loop((false, res), tail)
            }
        }
    loop(fun(initial, node), children)._2
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

  def zipWithLabel: Tree[(A, String)] = {
    def loop(nd: Tree[A], label: String): Tree[(A, String)] =
      Tree((nd.node, label), nd.children.zipWithIndex.map {
        case (n, id) => loop(n, label + "." + id.toString)
      })
    loop(this, "0")
  }

  def isLeaf: Boolean = children.isEmpty

  def reverse: Tree[A] = Tree(node, children.reverse.map(_.reverse))

  def size: Int = foldTopDown(0) { (acc, _) => acc + 1 }

  def levels: Int = zipWithLevel.foldTopDown(0) {
    case (cnt, (_, level)) => if (level > cnt) level else cnt
  } + 1

  def toListOfLevels: List[List[A]] =
    zipWithLevel.foldTopDown(Map[Int, List[A]]()) {
      case (acc, (n, level)) => acc + (level -> (n :: acc.getOrElse(level, List[A]())))
    }.toList.map(_._2)

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
