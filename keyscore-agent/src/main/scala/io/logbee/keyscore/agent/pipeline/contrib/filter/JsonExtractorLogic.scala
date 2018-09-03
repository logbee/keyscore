package io.logbee.keyscore.agent.pipeline.contrib.filter

import java.util.{Locale, UUID}

import akka.stream.FlowShape
import akka.stream.stage.StageLogging
import io.logbee.keyscore.agent.pipeline.stage.{FilterLogic, StageContext}
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.filter._
import org.json4s.JsonAST._
import org.json4s.native.JsonParser._


object JsonExtractorLogic extends Described {

  val filterName = "io.logbee.keyscore.agent.pipeline.contrib.filter.JsonExtractorLogic"
  val filterId: UUID = UUID.fromString("56c8a366-8625-46d2-9c0f-92dd24519989")

  override def describe: MetaFilterDescriptor = {
    MetaFilterDescriptor(filterId, filterName, Map(
      Locale.ENGLISH -> FilterDescriptorFragment(
        displayName = "Json Extractor",
        description = "Extracts each value from the json document in the specified source-field into new fields.",
        previousConnection = FilterConnection(isPermitted = true),
        nextConnection = FilterConnection(isPermitted = true),
        category = "JSON",
        parameters = List(
          TextParameterDescriptor("sourceFieldName", "Source Field", ""),
          BooleanParameterDescriptor("removeSourceField", "Remove Source Field", "")
        )
      )))
  }
}

class JsonExtractorLogic(context: StageContext, configuration: FilterConfiguration, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(context, configuration, shape) with StageLogging {

  private var sourceFieldName = "message"
  private var removeSourceField = false

  override def initialize(configuration: FilterConfiguration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: FilterConfiguration): Unit = {
    configuration.parameters.foreach {
      case TextParameter("sourceFieldName", value) => sourceFieldName = value
      case BooleanParameter("removeSourceField", value) => removeSourceField = value
      case _ =>
    }
  }

  override def onPush(): Unit = {
    val dataset = grab(in)

    val records = dataset.records.map(record => {

      val sourceField = record.fields.find(sourceFieldName == _.name)

      if (sourceField.isDefined && sourceField.get.isTextField) {

        val root = parse(sourceField.get.toTextField.value)
        val fields = extract(root, List.empty, List.empty)

        if (removeSourceField) {
          Record(record.fields.filter(sourceFieldName != _.name ) ++ fields)
        }
        else {
          Record(record.fields ++ fields)
        }
      }
      else {
        record
      }
    })

    push(out, Dataset(dataset.metadata, records))
  }

  override def onPull(): Unit = {
    pull(in)
  }

  private def extract(node: JValue, path: List[String], fields: List[Field] = List.empty): List[Field] = {
    node match {
      case obj: JObject =>
        obj.obj.foldLeft(fields) {
          case (fields, (name, jValue)) =>
            extract(jValue, path :+ name, fields)
        }

      case JArray(elements) =>
        elements.zipWithIndex.foldLeft(fields) {
          case (fields, (jValue, index)) =>
            extract(jValue, path :+ index.toString, fields)
        }

      case JBool(value) =>
        fields :+ Field(path.mkString("."), BooleanValue(value))

      case JInt(value) =>
        fields :+ Field(path.mkString("."), NumberValue(value.toLong))

      case JLong(value) =>
        fields :+ Field(path.mkString("."), NumberValue(value.toLong))

      case JDecimal(value) =>
        fields :+ Field(path.mkString("."), DecimalValue(value.toDouble))

      case JString(value) =>
        fields :+ Field(path.mkString("."), TextValue(value))

      case _ =>
        fields
    }
  }
}
