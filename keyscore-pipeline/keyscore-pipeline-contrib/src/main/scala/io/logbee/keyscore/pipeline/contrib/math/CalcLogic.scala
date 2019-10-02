package io.logbee.keyscore.pipeline.contrib.math

import akka.stream.FlowShape
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.{FilterLogic, LogicParameters}
import io.logbee.keyscore.pipeline.commons.CommonCategories
import io.logbee.keyscore.pipeline.commons.CommonCategories.CATEGORY_LOCALIZATION
import org.nfunk.jep.JEP

object CalcLogic extends Described {

  val expressionParameter = TextParameterDescriptor(
    ParameterRef("expression"),
    ParameterInfo(
      displayName = TextRef("expression.displayName"),
      description = TextRef("expression.description"),
      importance = Importance.High
    )
  )

  val resultFieldNameParameter = FieldNameParameterDescriptor(
    ref = ParameterRef("resultFieldName"),
    info = ParameterInfo(
      displayName = TextRef("resultFieldName.displayName"),
      description = TextRef("resultFieldName.description"),
      importance = Importance.Higher
    )
  )

  val DecimalResult = "DECIMAL_RESULT"
  val NumberResult = "NUMBER_RESULT"

  val resultFieldTypeParameter = ChoiceParameterDescriptor(
    ref = "resultFieldType",
    info = ParameterInfo(
      displayName = TextRef("resultFieldType.displayName"),
      description = TextRef("resultFieldType.description"),
      importance = Importance.Lower
    ),
    min = 1,
    max = 1,
    choices = Seq(
      Choice(
        name = DecimalResult,
        displayName = TextRef("resultFieldType.decimal-result.displayName"),
        description = TextRef("resultFieldType.decimal-result.description")
      ),
      Choice(
        name = NumberResult,
        displayName = TextRef("resultFieldType.number-result.displayName"),
        description = TextRef("resultFieldType.number-result.description")
      )
    )
  )

  override def describe = Descriptor(
    ref = "0718f882-9da0-11e9-9f0d-5f0379a089c3",
    describes = FilterDescriptor(
      name = classOf[CalcLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(CommonCategories.MATH),
      parameters = Seq(expressionParameter, resultFieldNameParameter, resultFieldTypeParameter),
      icon = Icon.fromClass(classOf[CalcLogic]),
      maturity = Maturity.Experimental
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.math.CalcLogic",
      Locale.ENGLISH, Locale.GERMAN) ++ CATEGORY_LOCALIZATION
  )
}

class CalcLogic(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(parameters, shape) {

  private var expression = ""
  private var resultFieldName = "result"
  private var resultType = CalcLogic.DecimalResult

  override def initialize(configuration: Configuration): Unit = configure(configuration)

  override def configure(configuration: Configuration): Unit = {
    expression = configuration.getValueOrDefault(CalcLogic.expressionParameter, expression)
    resultFieldName = configuration.getValueOrDefault(CalcLogic.resultFieldNameParameter, resultFieldName)
    resultType = configuration.getValueOrDefault(CalcLogic.resultFieldTypeParameter, resultType)
  }

  override def onPush(): Unit = {

    val dataset = grab(shape.in)

    if (expression.nonEmpty) {

      push(out, dataset.update(_.records := dataset.records.map(record => {
        val jep = record.fields.foldLeft(new JEP()) {
          case (jep, Field(name, NumberValue(value))) =>
            jep.addVariable(name.replace(' ', '_'), value)
            jep
          case (jep, Field(name, DecimalValue(value))) =>
            jep.addVariable(name.replace(' ', '_'), value)
            jep
          case (jep, _) => jep
        }
        try {
          val result = jep.evaluate(jep.parse(expression))
          record.update(_.fields :+= Field(resultFieldName, toValue(result)))
        } catch {
          case _ : Throwable => record
        }
      })))
    }
    else {
      push(out, dataset)
    }
  }

  override def onPull(): Unit = {
    pull(in)
  }

  private def toValue(result: AnyRef): Value = {
    resultType match {
      case CalcLogic.DecimalResult =>
        DecimalValue(s"$result".toDouble)
      case CalcLogic.NumberResult =>
        NumberValue(s"$result".toDouble.toLong)
    }
  }
}
