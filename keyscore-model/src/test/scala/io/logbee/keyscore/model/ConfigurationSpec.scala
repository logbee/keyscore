package io.logbee.keyscore.model

import io.logbee.keyscore.model.configuration._
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.json4s.KeyscoreFormats
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FreeSpec, Matchers}
import org.json4s.native.Serialization.{read, write, writePretty}


@RunWith(classOf[JUnitRunner])
class ConfigurationSpec extends FreeSpec with Matchers {
  implicit val formats = KeyscoreFormats.formats

  "A Configuration" - {

    val booleanParameter = BooleanParameterDescriptor("booleanParameter")
    val textParameter = TextParameterDescriptor("textParameter")
    val expressionParameter = ExpressionParameterDescriptor("expressionParameter")
    val numberParameter = NumberParameterDescriptor("numberParameter")
    val decimalParameter = DecimalParameterDescriptor("decimalParameter")
    val fieldNameParameter = FieldNameParameterDescriptor("fieldNameParameter")
    val fieldParameter = FieldParameterDescriptor("fieldParameter")
    val textListParameter = TextListParameterDescriptor("textListParameter")
    val fieldNameListParameter = FieldNameListParameterDescriptor("fieldNameListParameter")
    val fieldListParameter = FieldListParameterDescriptor("fieldListParameter")
    val choiceParameter = ChoiceParameterDescriptor("choiceParameter")

    val configuration = Configuration(parameters = List(
      BooleanParameter("booleanParameter", true),
      TextParameter("textParameter", "Hello World"),
      ExpressionParameter("expressionParameter", ".*"),
      NumberParameter("numberParameter", 42),
      DecimalParameter("decimalParameter", 7.3),
      FieldNameParameter("fieldNameParameter", "specialField"),
      FieldParameter("fieldParameter", Field("aField", TextValue("Hello World"))),
      TextListParameter("textListParameter", Seq("foo", "bar")),
      FieldNameListParameter("fieldNameListParameter", Seq("fieldA", "fieldB")),
      FieldListParameter("fieldListParameter", Seq(Field("aText", TextValue("Yeah")), Field("aNumber", NumberValue(42)))),
      ChoiceParameter("choiceParameter", "BLUE")

    ))

    println(write(configuration))


    "should find all configured values" in {

      configuration.findValue(booleanParameter) shouldBe Option(true)
      configuration.getValueOrDefault(booleanParameter, false) shouldBe true

      configuration.findValue(textParameter) shouldBe Option("Hello World")
      configuration.getValueOrDefault(textParameter, "") shouldBe "Hello World"

      configuration.findValue(expressionParameter) shouldBe Option(".*")
      configuration.getValueOrDefault(expressionParameter, "") shouldBe ".*"

      configuration.findValue(numberParameter) shouldBe Option(42)
      configuration.getValueOrDefault(numberParameter, 0) shouldBe 42

      configuration.findValue(decimalParameter) shouldBe Option(7.3)
      configuration.getValueOrDefault(decimalParameter, 0) shouldBe 7.3

      configuration.findValue(fieldNameParameter) shouldBe Option("specialField")
      configuration.getValueOrDefault(fieldNameParameter, "") shouldBe "specialField"

      configuration.findValue(fieldParameter) shouldBe Option(Field("aField", TextValue("Hello World")))
      configuration.getValueOrDefault(fieldParameter, null) shouldBe Field("aField", TextValue("Hello World"))

      configuration.findValue(textListParameter) shouldBe Option(Seq("foo", "bar"))
      configuration.getValueOrDefault(textListParameter, Seq.empty) shouldBe Seq("foo", "bar")

      configuration.findValue(fieldNameListParameter) shouldBe Option(Seq("fieldA", "fieldB"))
      configuration.getValueOrDefault(fieldNameListParameter, Seq.empty) shouldBe Seq("fieldA", "fieldB")

      configuration.findValue(fieldListParameter) shouldBe Option(Seq(Field("aText", TextValue("Yeah")), Field("aNumber", NumberValue(42))))
      configuration.getValueOrDefault(fieldListParameter, Seq.empty) shouldBe Seq(Field("aText", TextValue("Yeah")), Field("aNumber", NumberValue(42)))

      configuration.findValue(choiceParameter) shouldBe Option("BLUE")
      configuration.getValueOrDefault(choiceParameter, "") shouldBe "BLUE"
    }
  }
}
