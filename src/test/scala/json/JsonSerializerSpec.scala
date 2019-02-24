package json

import org.scalatest.{FlatSpec, Matchers}
import json.JsonSerializer._

class JsonSerializerSpec extends FlatSpec with Matchers {

  case class TestClass1(value: Int)
  class TestClass2(values: List[Int], maps: Map[Int, String], myDouble: Double) {
    def get: (List[Int], Map[Int, String], Double) = (values, maps, myDouble)
  }
  case class TestClass3(x: String, tc1: TestClass1)
  case class TestClass4(testClass3: TestClass3, testClass2: TestClass2)

  sealed trait Flags
  case object Flag_abc extends Flags
  case object Flag_xyz extends Flags
  case class ClassFlags(flags: Map[String, Flags])
  case class ClassFlagsList(flagsList: List[Flags])
  class AllFlags(flags: ClassFlags, flagsList: ClassFlagsList) { def get: (ClassFlags, ClassFlagsList) = (flags, flagsList) }

  it should "throw exception on AnyVal, String and collections" in {
    assertThrows[IllegalArgumentException] {
      toMapStd(5)
    }

    assertThrows[IllegalArgumentException] {
      toMapStd("test")
    }

    assertThrows[IllegalArgumentException] {
      toMapStd(List(1,2,3))
    }
  }

  it should "convert simple case class" in {
    toMapStd(TestClass1(5)) shouldEqual Map("value"->5)
  }

  it should "convert collections" in {
    val m1 = Map(1->"one", 2->"two")
    val d1 = 0.55

    toMapStd(new TestClass2(List(1,2,3), m1, d1)) shouldEqual Map("values"->List(1,2,3), "maps"->Map(1->"one", 2->"two"), "myDouble"->0.55)
  }

  it should "convert nested classes" in {
    toMapStd(TestClass4(TestClass3("test3", TestClass1(3)), new TestClass2(List(), Map(), 0.0))) shouldEqual Map(
      "testClass3"->Map("x"->"test3", "tc1"->Map("value"->3)),
      "testClass2"->Map("values"->List(), "maps"->Map(), "myDouble"->0.0)
      )
  }

  it should "apply special treatment" in {
    toMap(TestClass4(TestClass3("test3", TestClass1(3)), new TestClass2(List(), Map(), 0.0)), bNameCaseClasses = false)(
      filterField = StandardFieldFilter,
      specialFieldTreatment = (acc, field, any) => (field.getName, any) match {
        case ("x", _) => (acc, Some("special x"))
        case (_, _: TestClass2) => (acc, Some("tc2 body"))
        case _ => (acc, None)
      }
    ) shouldEqual Map(
      "testClass3"->Map("x"->"special x", "tc1"->Map("value"->3)),
      "testClass2"->"tc2 body"
    )
  }

  it should "convert nested classes with case classes" in {
    toMapCaseClassNameStd(TestClass4(TestClass3("test3", TestClass1(3)), new TestClass2(List(), Map(), 0.0))) shouldEqual Map(
      "TestClass3"->Map("x"->"test3", "TestClass1"->Map("value"->3)),
      "testClass2"->Map("values"->List(), "maps"->Map(), "myDouble"->0.0)
    )
  }

  it should "convert collection of case objects" in {

    val flags = Map("flag18"->Flag_abc, "flag24"->Flag_xyz)
    toMapCaseClassNameStd(new AllFlags(ClassFlags(flags), ClassFlagsList(List(Flag_xyz, Flag_abc)))) shouldEqual Map(
      "ClassFlags"->Map(
        "flags"->Map(
          "flag18"->"Flag_abc",
          "flag24"->"Flag_xyz")
      ),
      "ClassFlagsList"->Map(
        "flagsList"->List("Flag_xyz", "Flag_abc")
      )
    )
  }

  it should "process special field filtering" in {

    val flags = Map("flag18"->Flag_abc, "flag24"->Flag_xyz)
    val allFlags = new AllFlags(ClassFlags(flags), ClassFlagsList(List(Flag_xyz, Flag_abc)))

    val res = toMap(allFlags, bNameCaseClasses = true) {
      case (_, _: ClassFlagsList) => false
      case (field, _) if isInnerCaseClassField(field) => false
      case _ => true
    }

    res shouldEqual Map(
      "ClassFlags"->Map(
        "flags"->Map(
          "flag18"->"Flag_abc",
          "flag24"->"Flag_xyz"
        )
      )
    )
  }

  it should "provide special treatment for selected fields" in {

    val flags = Map("flag18"->Flag_abc, "flag24"->Flag_xyz)
    val allFlags = new AllFlags(ClassFlags(flags), ClassFlagsList(List(Flag_xyz, Flag_abc)))

    val res = toMap(allFlags, bNameCaseClasses = true) (filterField = StandardFieldFilter, specialFieldTreatment =
      (acc, field, v) => (field.getClass.getSimpleName, v) match {
      case (_, list: ClassFlagsList) => (acc, Some(list.flagsList.zipWithIndex.map { case (_, i) => i }))
      case (_, flags: ClassFlags)  => (acc, Some(
        flags.flags.collect {
          case (k, vmap) => k -> vmap.toString.drop("Flag_".length) }
        ))
      case _ => (acc, None)
    })

    res shouldEqual Map(
      "ClassFlagsList" -> List(0, 1),
      "ClassFlags"->Map(
        "flag18"->"abc",
        "flag24"->"xyz"
      )
    )
  }

  it should "filter selected case classes" in {

    val tc = TestClass4(TestClass3("test3", TestClass1(3)), new TestClass2(List(), Map(), 0.0))

    val res = toMap(tc, bNameCaseClasses = true) {
      case (_, _: TestClass2) => false
      case (field, _) if isInnerCaseClassField(field) => false
      case (field, _) if field.getName.endsWith("1") => false
      case _ => true
    }

    res shouldEqual Map(
      "TestClass3"->Map("x"->"test3")
    )
  }

}
