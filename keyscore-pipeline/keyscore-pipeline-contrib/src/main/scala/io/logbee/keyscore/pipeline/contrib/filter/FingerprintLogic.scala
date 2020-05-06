package io.logbee.keyscore.pipeline.contrib.filter

import java.security.MessageDigest

import akka.stream.FlowShape
import akka.stream.stage.StageLogging
import com.google.common.base.Charsets
import com.google.common.io.BaseEncoding.base64
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.{Dataset, _}
import io.logbee.keyscore.model.descriptor.FieldNameHint.PresentField
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.{FilterLogic, LogicParameters}
import io.logbee.keyscore.pipeline.commons.CommonCategories

import scala.Int.MaxValue
import scala.collection.mutable

object FingerprintLogic extends Described {

  import io.logbee.keyscore.model.util.ToOption.T2OptionT

  val includesParameter: FieldNameListParameterDescriptor = FieldNameListParameterDescriptor(
    ref = "includes",
    info = ParameterInfo(
      displayName = TextRef("includes.displayName"),
      description = TextRef("includes.description")
    ),
    descriptor = FieldNameParameterDescriptor(
      hint = PresentField
    ),
    min = 0,
    max = MaxValue
  )

  val excludesParameter: FieldNameListParameterDescriptor = FieldNameListParameterDescriptor(
    ref = "excludes",
    info = ParameterInfo(
      displayName = TextRef("excludes.displayName"),
      description = TextRef("excludes.description")
    ),
    descriptor = FieldNameParameterDescriptor(
      hint = PresentField
    ),
    min = 0,
    max = MaxValue
  )

  val fieldNameParameter: FieldNameParameterDescriptor = FieldNameParameterDescriptor(
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

  val recomputeParameter: BooleanParameterDescriptor = BooleanParameterDescriptor(
    ref = "fingerprint.recompute",
    info = ParameterInfo(
      displayName = TextRef("fingerprint.recompute.displayName"),
      description = TextRef("fingerprint.recompute.description")
    ),
    defaultValue = false,
    mandatory = true
  )

  val encodingParameter: BooleanParameterDescriptor = BooleanParameterDescriptor(
    ref = "fingerprint.encoding",
    info = ParameterInfo(
      displayName = TextRef("fingerprint.encoding.displayName"),
      description = TextRef("fingerprint.encoding.description")
    ),
    defaultValue = false,
    mandatory = true
  )

  val fingerprintFieldsEnabledParameter:BooleanParameterDescriptor = BooleanParameterDescriptor(
    ref = "fingerprint.fingerprintFields",
    info = ParameterInfo(
      displayName = TextRef("fingerprint.fingerprintFields.displayName"),
      description = TextRef("fingerprint.fingerprintFields.description")
    ),
    defaultValue = false,
    mandatory = false
  )

  override def describe: Descriptor = Descriptor(
    ref = "ed3ab993-1eca-4651-857d-fd4f72355251",
    describes = FilterDescriptor(
      name = classOf[FingerprintLogic].getName,
      displayName = TextRef("fingerprint.displayName"),
      description = TextRef("fingerprint.description"),
      categories = Seq(CommonCategories.MISCELLANEOUS, CommonCategories.AUGMENT),
      parameters = Seq(
        includesParameter,
        excludesParameter,
        fieldNameParameter,
        recomputeParameter,
        encodingParameter,
        fingerprintFieldsEnabledParameter
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

  private var includes = Seq.empty[String]
  private var excludes = Seq.empty[String]
  private var fieldName = FingerprintLogic.fieldNameParameter.defaultValue
  private var recompute = FingerprintLogic.recomputeParameter.defaultValue
  private var encoding = FingerprintLogic.encodingParameter.defaultValue
  private var fingerprintFieldsEnabled = FingerprintLogic.fingerprintFieldsEnabledParameter.defaultValue

  private val digest = MessageDigest.getInstance("MD5")

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: Configuration): Unit = {

    includes = configuration.getValueOrDefault(FingerprintLogic.includesParameter, includes)
    excludes = configuration.getValueOrDefault(FingerprintLogic.excludesParameter, excludes)
    fieldName = configuration.getValueOrDefault(FingerprintLogic.fieldNameParameter, fieldName)
    recompute = configuration.getValueOrDefault(FingerprintLogic.recomputeParameter, recompute)
    encoding = configuration.getValueOrDefault(FingerprintLogic.encodingParameter, encoding)
    fingerprintFieldsEnabled = configuration.getValueOrDefault(FingerprintLogic.fingerprintFieldsEnabledParameter, fingerprintFieldsEnabled)
  }

  override def onPush(): Unit = {

    import io.logbee.keyscore.model.util.Hashing.ExtendedDigest

    val name = fieldName
    val dataset = grab(in)

    push(out, dataset.update(_.records := dataset.records.map(record => {

      record.update(_.fields := record.fields

        .foldLeft((mutable.ListBuffer.empty[Field], mutable.ListBuffer.empty[String], Option[String](null), digest)) {

          case ((result, involved, _, digest), Field(`name`, TextValue(_, _))) if recompute =>
            (result, involved, None, digest)

          case ((result, involved, _, digest), Field(`name`, TextValue(fingerprint, _))) =>
            (result, involved, Some(fingerprint), digest)

          case ((result, involved, fingerprint, digest), field @ Field(name, _)) =>

            if (includes.isEmpty && excludes.isEmpty) {
              digest.update(field)
              (result += field, involved += name, fingerprint, digest)
            }
            else if (includes.isEmpty && !excludes.contains(field.name)) {
              digest.update(field)
              (result += field, involved += name, fingerprint, digest)
            }
            else if (includes.nonEmpty && includes.contains(field.name) && !excludes.contains(field.name)) {
              digest.update(field)
              (result += field, involved += name, fingerprint, digest)
            }
            else {
              (result += field, involved, fingerprint, digest)
            }
        }
        .map {

          case (fields, involved, None, digest) if encoding =>
            fields += Field(fieldName, TextValue(base64().encode(digest.digest())))
            maybeAddFingerprintFieldsField(fields, involved)

          case (fields, involved, None, digest) =>
            fields += Field(fieldName, TextValue(digest.digest().map("%02x".format(_)).mkString))
            maybeAddFingerprintFieldsField(fields, involved)

          case (fields, involved, Some(fingerprint), _) =>
            digest.reset()
            fields += Field(fieldName, TextValue(fingerprint))
            maybeAddFingerprintFieldsField(fields, involved)
        }
        .get.toList
      )
    })))
  }

  override def onPull(): Unit = {
    pull(in)
  }

  private def maybeAddFingerprintFieldsField(fields: mutable.ListBuffer[Field], fingerprintFields: mutable.ListBuffer[String]): mutable.ListBuffer[Field] = {
    if (fingerprintFieldsEnabled) {
      fields += Field(s"$fieldName.fields", TextValue(fingerprintFields.map(name => "\"" + name + "\"").mkString("[", ", ", "]")))
    }
    fields
  }
}
