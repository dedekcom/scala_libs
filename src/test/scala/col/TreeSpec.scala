package col

import org.scalatest.{FlatSpec, Matchers}

class TreeSpec extends FlatSpec with Matchers {

  case class Trexample1(number1: Int, name: String)

  val ex111: Trexample1 = Trexample1(100, "ex111")
  val ex112: Trexample1 = Trexample1(300, "ex112")
  val ex131: Trexample1 = Trexample1(500, "ex131")
  val ex11: Trexample1 = Trexample1(11, "ex11")
  val ex12: Trexample1 = Trexample1(21, "ex12")
  val ex13: Trexample1 = Trexample1(31, "ex13")
  val ex1: Trexample1 = Trexample1(1, "ex1")

  val tree =
    Tree(ex1, List(
      Tree(ex11, List(
        Tree(ex111, Nil),
        Tree(ex112, Nil)
      )),
      Tree(ex12, Nil),
      Tree(ex13, List(
        Tree(ex131, Nil)
      ))
    )
    )

  it should "tree fold top down" in {
    tree.foldTopDown(0) {
      (acc, el) => acc + el.number1
    } shouldEqual (1 + 11 + 21 + 31 + 100 + 300 + 500)
  }

  //println(tree)

  it should "tree fold top down with parent" in {
    tree.foldTopDown(0) {
      (acc, el) => acc + el.number1
    } shouldEqual (1 + 11 + 21 + 31 + 100 + 300 + 500)
  }

  it should "fold bottom up" in {
    tree.foldBottomUp(ex1, 0) { (acc, num) => acc + num.number1 } shouldEqual 1
    tree.foldBottomUp(tree.children(0).children(1).node, 0) { (acc, num) => {
      //println(s"test node ${num.name}")
      acc + num.number1
    } } shouldEqual (
      tree.node.number1 + tree.children(0).node.number1 + tree.children(0).children(1).node.number1)
  }

  it should "find and exists" in {
    tree.find(t => t.name == "x") shouldEqual None
    tree.find(t => t.number1 == 500) shouldEqual Some(ex131)
    tree.findSubTree(t => t.number1 == 500).map(_.node) shouldEqual Some(ex131)
    tree.exists(t => t.number1 == 31) shouldEqual true
  }

  it should "map, filter and remove" in {
    case class Mul2ex(sqr: Int)

    tree.map(t => Mul2ex(t.number1 * 2)) shouldEqual Tree(Mul2ex(2),
      List(Tree(Mul2ex(ex11.number1 * 2), List(
        Tree(Mul2ex(ex111.number1 * 2), Nil),
        Tree(Mul2ex(ex112.number1 * 2), Nil))
      ),
        Tree(Mul2ex(ex12.number1 * 2), Nil),
        Tree(Mul2ex(ex13.number1 * 2), List(
          Tree(Mul2ex(ex131.number1 * 2), Nil)
        )
        )))

    tree.filter(t => t.number1 == 11 || t.number1 == 100) shouldEqual Tree(ex1, List(Tree(ex11, List(Tree(ex111, Nil)))))

    tree.removeAll(ex111) shouldEqual Tree(ex1, List(
      Tree(ex11, List(
        Tree(ex112, Nil)
      )),
      Tree(ex12, Nil),
      Tree(ex13, List(
        Tree(ex131, Nil)
      ))
    ))
  }

  it should "calc size and levels" in {
    tree.size shouldEqual 7
    tree.levels shouldEqual 3
  }

  it should "zip with level" in {
    Tree(ex1, List(
      Tree(ex11, List(
        Tree(ex111, Nil),
        Tree(ex112, Nil)
      )),
      Tree(ex12, Nil),
      Tree(ex13, List(
        Tree(ex131, Nil)
      ))
    )).zipWithLevel shouldEqual Tree((ex1, 0), List(
      Tree((ex11, 1), List(
        Tree((ex111, 2), Nil),
        Tree((ex112, 2), Nil)
      )),
      Tree((ex12, 1), Nil),
      Tree((ex13, 1), List(
        Tree((ex131, 2), Nil)
      ))
    ))

    tree.zipWithLevelAndIndex shouldEqual Tree((ex1, (0, 0)), List(
      Tree((ex11, (1, 0)), List(
        Tree((ex111, (2, 0)), Nil),
        Tree((ex112, (2, 1)), Nil)
      )),
      Tree((ex12, (1, 1)), Nil),
      Tree((ex13, (1, 2)), List(
        Tree((ex131, (2, 0)), Nil)
      ))
    ))

  }

}
