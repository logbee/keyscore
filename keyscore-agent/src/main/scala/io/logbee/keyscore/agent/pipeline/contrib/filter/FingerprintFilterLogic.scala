package io.logbee.keyscore.agent.pipeline.contrib.filter

import java.security.MessageDigest
import java.util.UUID.fromString
import java.util.{Locale, ResourceBundle}

import akka.stream.FlowShape
import akka.stream.stage.StageLogging
import com.google.common.base.Charsets
import com.google.common.io.BaseEncoding.base64
import io.logbee.keyscore.agent.pipeline.stage.{FilterLogic, StageContext}
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.filter._
import io.logbee.keyscore.model._

import scala.collection.mutable

object FingerprintFilterLogic extends Described {

  private val filterName = "io.logbee.keyscore.agent.pipeline.contrib.filter.FingerprintFilterLogic"
  private val bundleName = "io.logbee.keyscore.agent.pipeline.contrib.filter.FingerprintFilter"
  private val filterId = "ed3ab993-1eca-4651-857d-fd4f72355251"

  override def describe: MetaFilterDescriptor = {
    val descriptorMap = mutable.Map.empty[Locale, FilterDescriptorFragment]
    descriptorMap ++= Map(
      Locale.ENGLISH -> descriptor(Locale.ENGLISH),
      Locale.GERMAN -> descriptor(Locale.GERMAN)
    )

    MetaFilterDescriptor(fromString(filterId), filterName, descriptorMap.toMap)
  }

  private def descriptor(language: Locale): FilterDescriptorFragment = {
    val translatedText: ResourceBundle = ResourceBundle.getBundle(bundleName, language)
    FilterDescriptorFragment(
      displayName = translatedText.getString("displayName"),
      description = translatedText.getString("description"),
      previousConnection = FilterConnection(isPermitted = true),
      nextConnection = FilterConnection(isPermitted = true),
      parameters = List(
        TextParameterDescriptor("target", displayName = translatedText.getString("target"), description = translatedText.getString("targetDescription"), mandatory = false, validator = ".*"),
        BooleanParameterDescriptor("base64Encoding", displayName = translatedText.getString("base64Encoding"), description = translatedText.getString("base64EncodingDescription"), mandatory = false)
      ))
  }
}

class FingerprintFilterLogic(context: StageContext, configuration: FilterConfiguration, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(context, configuration, shape) with StageLogging {

  private var targetFieldName = "fingerprint"
  private var base64Encoding = false

  private val digest = MessageDigest.getInstance("MD5")

  override def configure(configuration: FilterConfiguration): Unit = {
    configuration.parameters.foreach {
      case TextParameter("target", value) if value != null && value.nonEmpty => targetFieldName = value
      case BooleanParameter("base64Encoding", value) => base64Encoding = value
      case _ =>
    }
  }

  override def onPush(): Unit = {

    val dataset = grab(in)
    val records = dataset.records.map(record => {

      var fingerprint: String = "<unknown>"
      val hashBytes = record.payload.values.foldLeft(digest) {
        case (md5, field) =>
          md5.update(s"${field.name}=${field.value}".getBytes(Charsets.UTF_8))
          md5
      }.digest()

      if (base64Encoding) {
        fingerprint = base64().encode(hashBytes)
      }
      else {
        fingerprint = hashBytes.map("%02x".format(_)).mkString
      }

      Record(record.id, record.payload + (targetFieldName -> TextField(targetFieldName, fingerprint)))
    })

    push(out, Dataset(dataset.metaData, records))
  }

  override def onPull(): Unit = {
    pull(in)
  }
}
