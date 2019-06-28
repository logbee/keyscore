package io.logbee.keyscore.pipeline.contrib.decoder.json

import akka.stream.FlowShape
import akka.stream.stage.StageLogging
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.{Dataset, _}
import io.logbee.keyscore.model.descriptor.FieldNameHint.PresentField
import io.logbee.keyscore.model.descriptor.{BooleanParameterDescriptor, Descriptor, FieldNameParameterDescriptor, FilterDescriptor, ParameterInfo, _}
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.{FilterLogic, LogicParameters}
import io.logbee.keyscore.pipeline.contrib.CommonCategories.{CATEGORY_LOCALIZATION, DECODING, JSON}
import org.json4s.JsonAST._
import org.json4s.native.JsonParser._

import scala.collection.mutable

object JsonPathJsonExtractorLogic extends Described {

  val jsonpathParameter = TextParameterDescriptor(
    ref = "jsonpath",
    info = ParameterInfo(
      displayName = TextRef("jsonpath.displayName"),
      description = TextRef("jsonpath.description")
    ),
    defaultValue = "message",
    mandatory = true
  )

  val sourceFieldNameParameter = FieldNameParameterDescriptor(
    ref = "sourceFieldName",
    info = ParameterInfo(
      displayName = TextRef("sourceFieldName.displayName"),
      description = TextRef("sourceFieldName.description")
    ),
    defaultValue = "message",
    hint = PresentField,
    mandatory = true
  )

  val removeSourceFieldParameter = BooleanParameterDescriptor(
    ref = "removeSourceField",
    info = ParameterInfo(
      displayName = TextRef("removeSourceField.displayName"),
      description = TextRef("removeSourceField.description")
    ),
    defaultValue = false,
    mandatory = true
  )

  override def describe = Descriptor (
    ref = "dc58c2bf-0de4-459a-bbea-154b6715c734",
    describes = FilterDescriptor(
      name = classOf[JsonPathJsonExtractorLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(DECODING, JSON),
      parameters = Seq(jsonpathParameter, sourceFieldNameParameter, removeSourceFieldParameter),
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.decoder.json.JsonPathJsonExtractorLogic",
      Locale.ENGLISH, Locale.GERMAN
    ) ++ CATEGORY_LOCALIZATION
  )
}

class JsonPathJsonExtractorLogic(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(parameters, shape) with StageLogging {

  private var jsonpath = JsonPathJsonExtractorLogic.jsonpathParameter.defaultValue
  private var sourceFieldName = JsonPathJsonExtractorLogic.sourceFieldNameParameter.defaultValue
  private var removeSourceField = JsonPathJsonExtractorLogic.removeSourceFieldParameter.defaultValue

  private var paths = List.empty[String]

  override def initialize(configuration: Configuration): Unit = configure(configuration)

  override def configure(configuration: Configuration): Unit = {
    jsonpath = configuration.getValueOrDefault(JsonPathJsonExtractorLogic.jsonpathParameter, jsonpath)
    sourceFieldName = configuration.getValueOrDefault(JsonPathJsonExtractorLogic.sourceFieldNameParameter, sourceFieldName)
    removeSourceField = configuration.getValueOrDefault(JsonPathJsonExtractorLogic.removeSourceFieldParameter, removeSourceField)

    paths = jsonpath.split("\\.").toList
  }

  override def onPush(): Unit = {

    val dataset = grab(in)

    push(out, dataset.withRecords(dataset.records.foldLeft(mutable.ListBuffer.empty[Record]) { case (result, record) =>

      record.fields.find(sourceFieldName == _.name) match {
        case Some(field @ Field(_, TextValue(value))) =>
          result ++= evaluate(record, field.toTextField)
        case _ =>
          result += record
      }
    }.toList))
  }

  override def onPull(): Unit = {
    pull(in)
  }

  private def evaluate(record: Record, field: TextField): List[Record] = {
    val json = parse(field.value)
    List.empty
  }

  private def parseJson(json: JValue): List[Record] = {
    List.empty
  }
}
