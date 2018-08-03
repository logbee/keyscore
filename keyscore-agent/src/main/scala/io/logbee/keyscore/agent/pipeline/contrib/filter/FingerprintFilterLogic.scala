package io.logbee.keyscore.agent.pipeline.contrib.filter

import java.security.MessageDigest
import java.util.UUID.fromString
import java.util.{Locale, ResourceBundle}

import akka.stream.FlowShape
import akka.stream.stage.StageLogging
import com.google.common.base.Charsets
import com.google.common.io.BaseEncoding.base64
import io.logbee.keyscore.agent.pipeline.stage.{FilterLogic, StageContext}
import io.logbee.keyscore.model.filter._
import io.logbee.keyscore.model.{Dataset, Described, Record, TextField}

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
      category = "Debug",
      parameters = List(
        TextParameterDescriptor("fingerprintFieldName", displayName = translatedText.getString("fingerprintFieldName"), description = "", mandatory = true, validator = ".*")
      ))
  }
}

class FingerprintFilterLogic(context: StageContext, configuration: FilterConfiguration, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(context, configuration, shape) with StageLogging {

  private var fingerprintFieldName = "fingerprint"

  override def configure(configuration: FilterConfiguration): Unit = {
    configuration.parameters.foreach {
      case TextParameter("fingerprintFieldName", value) => fingerprintFieldName = value
      case _ =>
    }
  }

  override def onPush(): Unit = {

    val digest = MessageDigest.getInstance("MD5")

    val dataset = grab(in)
    val records = dataset.records.map(record => {

      val base64MD5Hash = base64().encode(record.payload.values.foldLeft(digest) {
        case (md5, field) =>
          md5.update(s"${field.name}=${field.value}".getBytes(Charsets.UTF_8))
          md5
      }.digest())

      Record(record.id, record.payload + (fingerprintFieldName -> TextField(fingerprintFieldName, base64MD5Hash)))
    })

    push(out, Dataset(dataset.metaData, records))
  }

  override def onPull(): Unit = {
    pull(in)
  }
}
