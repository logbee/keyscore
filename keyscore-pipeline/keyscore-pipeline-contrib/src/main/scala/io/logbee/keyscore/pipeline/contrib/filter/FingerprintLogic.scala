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
import io.logbee.keyscore.pipeline.commons.CommonCategories

import scala.collection.mutable

object FingerprintLogic extends Described {

  val fieldNameParameter = FieldNameParameterDescriptor(
    ref = "fingerprint.fieldName",
    info = ParameterInfo(
      displayName = TextRef("fingerprint.fieldName.displayName"),
      description = TextRef("fingerprint.fieldName.description")
    ),
    validator = StringValidator(
      expression = ".*",
    ),
    hint = FieldNameHint.AbsentField,
    defaultValue = "fingerprint"
  )

  val recomputeParameter = BooleanParameterDescriptor(
    ref = "fingerprint.recompute",
    info = ParameterInfo(
      displayName = TextRef("fingerprint.recompute.displayName"),
      description = TextRef("fingerprint.recompute.description")
    ),
    defaultValue = false,
    mandatory = true
  )

  val encodingParameter = BooleanParameterDescriptor(
    ref = "fingerprint.encoding",
    info = ParameterInfo(
      displayName = TextRef("fingerprint.encoding.displayName"),
      description = TextRef("fingerprint.encoding.description")
    ),
    defaultValue = false,
    mandatory = true
  )

  override def describe = Descriptor(
    ref = "ed3ab993-1eca-4651-857d-fd4f72355251",
    describes = FilterDescriptor(
      name = classOf[FingerprintLogic].getName,
      displayName = TextRef("fingerprint.displayName"),
      description = TextRef("fingerprint.description"),
      categories = Seq(CommonCategories.MISCELLANEOUS, CommonCategories.AUGMENT),
      parameters = Seq(
        fieldNameParameter,
        recomputeParameter,
        encodingParameter
      ),
      icon = Icon.fromClass(classOf[FingerprintLogic]),
      maturity = Maturity.Official
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.filter.Fingerprint",
      Locale.ENGLISH, Locale.GERMAN
    ) ++ CommonCategories.CATEGORY_LOCALIZATION
  )
}

class FingerprintLogic(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(parameters, shape) with StageLogging {

  private var fieldName = FingerprintLogic.fieldNameParameter.defaultValue
  private var recompute = FingerprintLogic.recomputeParameter.defaultValue
  private var encoding = FingerprintLogic.encodingParameter.defaultValue

  private val digest = MessageDigest.getInstance("MD5")

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: Configuration): Unit = {

    fieldName = configuration.getValueOrDefault(FingerprintLogic.fieldNameParameter, fieldName)
    recompute = configuration.getValueOrDefault(FingerprintLogic.recomputeParameter, recompute)
    encoding = configuration.getValueOrDefault(FingerprintLogic.encodingParameter, encoding)
  }

  override def onPush(): Unit = {

    val name = fieldName
    val dataset = grab(in)

    push(out, dataset.update(_.records := dataset.records.map(record => {

      record.update(_.fields := record.fields

        .foldLeft((mutable.HashSet.empty[Field], Option[String](null), digest)) {

          case ((result, _, digest), Field(`name`, TextValue(_, _))) if recompute =>
            (result, None, digest)

          case ((result, _, digest), Field(`name`, TextValue(fingerprint, _))) =>
            (result, Some(fingerprint), digest)

          case ((result, fingerprint, digest), field) =>
            digest.update(s"${field.name}=${field.value}".getBytes(Charsets.UTF_8))
            (result += field, fingerprint, digest)

        }
        .map {

          case (fields, None, digest) if encoding => fields += Field(fieldName, TextValue(base64().encode(digest.digest())))

          case (fields, None, digest) => fields += Field(fieldName, TextValue(digest.digest().map("%02x".format(_)).mkString))

          case (fields, Some(fingerprint), _) =>
            digest.reset()
            fields += Field(fieldName, TextValue(fingerprint))
        }
        .get.toList
      )
    })))
  }

  override def onPull(): Unit = {
    pull(in)
  }
}
