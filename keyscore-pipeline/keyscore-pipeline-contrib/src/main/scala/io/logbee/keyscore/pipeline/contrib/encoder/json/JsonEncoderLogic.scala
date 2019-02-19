package io.logbee.keyscore.pipeline.contrib.encoder.json

import akka.stream.FlowShape
import akka.stream.stage.StageLogging
import com.google.protobuf.util.Timestamps
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.json4s._
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.{FilterLogic, LogicParameters}
import io.logbee.keyscore.pipeline.contrib.CommonCategories.{CATEGORY_LOCALIZATION, ENCODING, JSON}
import org.json4s.native.Serialization.write

object JsonEncoderLogic extends Described {

  private[encoder] val fieldNameParameter = FieldNameParameterDescriptor(
    ref = "jsonEncoder.fieldName",
    info = ParameterInfo(
      displayName = TextRef("jsonEncoder.fieldName.displayName"),
      description = TextRef("jsonEncoder.fieldName.description")
    ),
    defaultValue = "json",
    mandatory = true
  )

  private[encoder] val KEEP_BATCH = "jsonEncoder.batch.KEEP_BATCH"
  private[encoder] val SPLIT_BATCH = "jsonEncoder.batch.SPLIT_BATCH"

  private[encoder] val batchStrategyParameter = ChoiceParameterDescriptor(
    ref = "jsonEncoder.batchStrategy",
    info = ParameterInfo(
      displayName = TextRef("jsonEncoder.batchStrategy.displayName"),
      description = TextRef("jsonEncoder.batchStrategy.description")
    ),
    min = 1,
    max = 1,
    choices = Seq(
      Choice(
        name = KEEP_BATCH,
        displayName = TextRef("jsonEncoder.batchStrategy.KEEP_BATCH.displayName"),
        description = TextRef("jsonEncoder.batchStrategy.KEEP_BATCH.description")
      ),
      Choice(
        name = SPLIT_BATCH,
        displayName = TextRef("jsonEncoder.batchStrategy.SPLIT_BATCH.displayName"),
        description = TextRef("jsonEncoder.batchStrategy.SPLIT_BATCH.description")
      )
    )
  )

  override def describe: Descriptor = Descriptor(
    ref = "93d8aa91-390e-4325-8fea-d75a5be25dc2",
    describes = FilterDescriptor(
      name = classOf[JsonEncoderLogic].getName,
      displayName = TextRef("jsonEncoder.displayName"),
      description = TextRef("jsonEncoder.description"),
      categories = Seq(ENCODING, JSON),
      parameters = Seq(fieldNameParameter, batchStrategyParameter),
      icon = Icon.fromClass(classOf[JsonEncoderLogic])
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.encoder.JsonEncoder",
      Locale.ENGLISH, Locale.GERMAN
    ) ++ CATEGORY_LOCALIZATION
  )
}

class JsonEncoderLogic(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(parameters, shape) with StageLogging {

  private implicit val formats = KeyscoreFormats.formats

  private var fieldName = JsonEncoderLogic.fieldNameParameter.defaultValue
  private var batchStrategy = JsonEncoderLogic.KEEP_BATCH

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: Configuration): Unit = {
    fieldName = configuration.getValueOrDefault(JsonEncoderLogic.fieldNameParameter, fieldName)
    batchStrategy = configuration.getValueOrDefault(JsonEncoderLogic.batchStrategyParameter, batchStrategy)
  }

  override def onPush(): Unit = {
    batchStrategy match {
      case JsonEncoderLogic.KEEP_BATCH => pushAll(grab(in))
      case JsonEncoderLogic.SPLIT_BATCH => pushEach(grab(in))
      case _ => pushAll(grab(in))
    }
  }

  override def onPull(): Unit = {
    pull(in)
  }

  private def pushAll(dataset: Dataset): Unit = {
    push(out, dataset.update(
      _.records := List(Record(Field(fieldName, TextValue(writeRecordList(dataset.records)))))
    ))
  }

  private def pushEach(dataset: Dataset): Unit = {
    push(out, dataset.update(
      _.records := dataset.records.map(record => Record(Field(fieldName, TextValue(writeRecord(record)))))
    ))
  }

  private def writeRecord(record: Record): String = write(toMap(record.fields))

  private def writeRecordList(records: List[Record]): String = write(records.map(record => toMap(record.fields)))

  private def toMap(fields: List[Field]): Map[String, Any] = {
    fields.map(field => (field.name, field.value)).foldLeft(Map.empty[String, Any]) {
      case (map, (name, TextValue(value))) => map + (name -> value)
      case (map, (name, NumberValue(value))) => map + (name -> value)
      case (map, (name, DecimalValue(value))) => map + (name -> value)
      case (map, (name, value: TimestampValue)) => map + (name -> Timestamps.toString(value))
      case (map, (name, value: DurationValue)) => map + (name ->  (value.seconds*1000000000 + value.nanos))
      case (map, (name, value: HealthValue)) => map + (name -> value.value.toString())
      case (map, (name, _)) => map + (name -> null)
      case (map, _) => map
    }
  }
}
