package io.logbee.keyscore.pipeline.contrib.filter.batch

import java.util.regex.Pattern

import akka.stream.FlowShape
import akka.stream.stage.StageLogging
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.descriptor.FieldNameHint.PresentField
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.{FilterLogic, LogicParameters}
import io.logbee.keyscore.pipeline.contrib.CommonCategories
import io.logbee.keyscore.pipeline.contrib.CommonCategories.CATEGORY_LOCALIZATION

import scala.Int.MaxValue

object SpreaderLogic extends Described {

  val fieldNamesParameter = FieldNameListParameterDescriptor(
    ParameterRef("fieldNames"),
    info = ParameterInfo(
      displayName = TextRef("fieldNames.displayName"),
      description = TextRef("fieldNames.description")
    ),
    descriptor = FieldNameParameterDescriptor(
      hint = PresentField
    ),
    max = MaxValue
  )

  override def describe = Descriptor(
    ref = "079037e5-87d8-49c3-a7ea-30d7dae31daa",
    describes = FilterDescriptor(
      name = classOf[SpreaderLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(CommonCategories.BATCH_COMPOSITION),
      parameters = List(fieldNamesParameter)
    ),
    localization = Option(Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.filter.SpreaderLogic",
      Locale.ENGLISH, Locale.GERMAN
    ) ++ CATEGORY_LOCALIZATION)
  )
}

class SpreaderLogic(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(parameters, shape) with StageLogging{

  private var fieldNames = Seq.empty[String]
  private var patterns = Seq.empty[Pattern]

  override def initialize(configuration: Configuration): Unit = configure(configuration)

  override def configure(configuration: Configuration): Unit = {
    fieldNames = configuration.getValueOrDefault(SpreaderLogic.fieldNamesParameter, fieldNames)
    patterns = fieldNames.map(_.r.pattern)
  }

  override def onPush(): Unit = {

    var dataset = grab(in)
    val fields = dataset.records.flatMap(_.fields).filter(field => patterns.exists(pattern => pattern.matcher(field.name).matches()))

    if (fields.nonEmpty) {
      dataset = dataset.update(_.records := dataset.records.map(record => record.update(_.fields := Set(record.fields ++ fields:_*).toList)))
    }

    push(out, dataset)
  }

  override def onPull(): Unit = {
    pull(in)
  }


}

