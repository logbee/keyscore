package io.logbee.keyscore.pipeline.contrib.filter.encoder

import akka.stream.FlowShape
import akka.stream.stage.StageLogging
import com.google.protobuf.util.Timestamps
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.{FilterLogic, LogicParameters}
import io.logbee.keyscore.pipeline.contrib.CommonCategories.{CATEGORY_LOCALIZATION, ENCODING, JSON}
import org.json4s.native.Serialization.write
import io.logbee.keyscore.model.json4s._

object JsonEncoderLogic extends Described {

  override def describe: Descriptor = Descriptor(
    ref = "93d8aa91-390e-4325-8fea-d75a5be25dc2",
    describes = FilterDescriptor(
      name = classOf[JsonEncoderLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(ENCODING, JSON),
      parameters = Seq()
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.filter.JsonEncoder",
      Locale.ENGLISH, Locale.GERMAN
    ) ++ CATEGORY_LOCALIZATION
  )
}

class JsonEncoderLogic(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(parameters, shape) with StageLogging {

  implicit val formats = KeyscoreFormats.formats

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: Configuration): Unit = {

  }

  override def onPush(): Unit = {
    val dataset = grab(in)

    val records = scala.collection.mutable.ListBuffer.empty[Record]

    dataset.records.foreach(record => {
      records += Record(TextField("_JSON", writeRecord(record)))
    })

    val encodedDataset = Dataset(dataset.metadata, records.toList)
    push(out, encodedDataset)
  }

  override def onPull(): Unit = {
    pull(in)
  }

  private def writeRecord(record: Record): String = {

    val message = record.fields.map(field => (field.name, field.value)).foldLeft(Map.empty[String, Any]) {
      case (map, (name, TextValue(value))) => map + (name -> value)
      case (map, (name, NumberValue(value))) => map + (name -> value)
      case (map, (name, DecimalValue(value))) => map + (name -> value)
      case (map, (name, value: TimestampValue)) => map + (name -> Timestamps.toString(value))
      case (map, (name, value: DurationValue)) => map + (name ->  (value.seconds*1000000000 + value.nanos))
      case (map, (name, value: HealthValue)) => map + (name -> value.value.toString())
      case (map, (name, _)) => map + (name -> null)
      case (map, _) => map
    }

    write(message)
  }


}
