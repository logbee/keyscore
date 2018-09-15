package io.logbee.keyscore.pipeline.contrib.filter

import java.security.MessageDigest

import akka.stream.FlowShape
import akka.stream.stage.StageLogging
import com.google.common.base.Charsets
import com.google.common.io.BaseEncoding.base64
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.{Dataset, _}
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.{FilterLogic, LogicParameters}

object FingerprintFilterLogic extends Described {

  private val targetParameter = TextParameterDescriptor(
    ref = "fingerprint.target",
    info = ParameterInfo(
      displayName = TextRef("target"),
      description = TextRef("targetDescription")
    ),
    validator = StringValidator(
      expression = ".*",
      expressionType = ExpressionType.Glob
    )
  )

  private val encodingParameter = BooleanParameterDescriptor(
    ref = "fingerprint.encoding",
    info = ParameterInfo(
      displayName = TextRef("base64Encoding"),
      description = TextRef("base64EncodingDescription")
    )
  )

  override def describe = Descriptor(
    ref = "ed3ab993-1eca-4651-857d-fd4f72355251",
    describes = FilterDescriptor(
      name = classOf[FingerprintFilterLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(Category("fingerprint", TextRef("category.fingerprint.displayName"))),
      parameters = Seq(targetParameter,encodingParameter)
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.agent.pipeline.contrib.filter.FingerprintFilter",
      Locale.ENGLISH, Locale.GERMAN
    )
  )
}

class FingerprintFilterLogic(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(parameters, shape) with StageLogging {

  private var targetFieldName = "fingerprint"
  private var base64Encoding = false

  private val digest = MessageDigest.getInstance("MD5")

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: Configuration): Unit = {

    targetFieldName = configuration.getValueOrDefault(FingerprintFilterLogic.targetParameter, targetFieldName)
    base64Encoding = configuration.getValueOrDefault(FingerprintFilterLogic.encodingParameter, base64Encoding)
  }

  override def onPush(): Unit = {

    val dataset = grab(in)
    val records = dataset.records.map(record => {

      var fingerprint: String = "<unknown>"
      val hashBytes = record.fields.foldLeft(digest) {
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

      Record(Field(targetFieldName, TextValue(fingerprint)) +: record.fields)
    })

    push(out, Dataset(dataset.metadata, records))
  }

  override def onPull(): Unit = {
    pull(in)
  }
}
