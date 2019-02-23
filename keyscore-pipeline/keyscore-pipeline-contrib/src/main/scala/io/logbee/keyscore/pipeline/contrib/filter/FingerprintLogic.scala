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

object FingerprintLogic extends Described {
  private val iconName = "io.logbee.keyscore.pipeline.contrib.icon/fingerprint.svg"


  private val fieldNameParameter = FieldNameParameterDescriptor(
    ref = "fingerprint.fieldName",
    info = ParameterInfo(
      displayName = TextRef("fieldName"),
      description = TextRef("fieldNameDescription")
    ),
    validator = StringValidator(
      expression = ".*",
    ),
    hint = FieldNameHint.AbsentField
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
      name = classOf[FingerprintLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(Category("fingerprint", TextRef("category.fingerprint.displayName"))),
      parameters = Seq(fieldNameParameter,encodingParameter),
      icon = Icon.fromClass(classOf[FingerprintLogic])
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.filter.Fingerprint",
      Locale.ENGLISH, Locale.GERMAN
    )
  )
}

class FingerprintLogic(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(parameters, shape) with StageLogging {

  private var fieldName = "fingerprint"
  private var base64Encoding = false

  private val digest = MessageDigest.getInstance("MD5")

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: Configuration): Unit = {

    base64Encoding = configuration.getValueOrDefault(FingerprintLogic.encodingParameter, base64Encoding)
    fieldName = configuration.getValueOrDefault(FingerprintLogic.fieldNameParameter, fieldName)
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

      Record(Field(fieldName, TextValue(fingerprint)) +: record.fields)
    })

    push(out, Dataset(dataset.metadata, records))
  }

  override def onPull(): Unit = {
    pull(in)
  }
}
