package io.logbee.keyscore.pipeline.contrib.filter.textmutator

import akka.stream.FlowShape
import akka.stream.stage.StageLogging
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.configuration.{Configuration, DirectiveConfiguration}
import io.logbee.keyscore.model.data.FieldValueType.Text
import io.logbee.keyscore.model.data.{Dataset, Field, Icon, Record, TextValue}
import io.logbee.keyscore.model.descriptor.FieldNameHint.PresentField
import io.logbee.keyscore.model.descriptor.FieldNamePatternParameterDescriptor.PatternType
import io.logbee.keyscore.model.descriptor.Maturity.Experimental
import io.logbee.keyscore.model.descriptor.ToDescriptorRef.stringToDescriptorRef
import io.logbee.keyscore.model.descriptor.{ParameterGroupDescriptor, _}
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.directive.FieldDirective
import io.logbee.keyscore.pipeline.api.{FilterLogic, LogicParameters}
import io.logbee.keyscore.pipeline.commons.CommonCategories.{CATEGORY_LOCALIZATION, FIELDS}
import io.logbee.keyscore.pipeline.contrib.filter.textmutator.TextMutatorLogic.{findAndReplaceDirective, toTimestampDirective, trimDirective}

import scala.Int.MaxValue
import scala.collection.mutable


object TextMutatorLogic extends Described {

  val findPattern = TextParameterDescriptor(
    ref = "textmutator.findAndReplaceDirective.find",
    info = ParameterInfo(
      displayName = "textmutator.findAndReplaceDirective.find.displayName",
      description = "textmutator.findAndReplaceDirective.find.description"
    ),
    mandatory = true
  )

  val replacePattern = TextParameterDescriptor(
    ref = "textmutator.findAndReplaceDirective.replace",
    info = ParameterInfo(
      displayName = "textmutator.findAndReplaceDirective.replace.displayName",
      description = "textmutator.findAndReplaceDirective.replace.description"
    ),
    mandatory = true
  )

  val toTimestampPattern = TextParameterDescriptor(
    ref = "textmutator.toTimestampDirective.pattern",
    info = ParameterInfo(
      displayName = "textmutator.toTimestampDirective.pattern.displayName",
      description = "textmutator.toTimestampDirective.pattern.description"
    ),
    mandatory = true
  )

  val trimDirective = FieldDirectiveDescriptor(
    ref = DirectiveRef("ab31ce5f-d582-48e6-9a76-80436a733678"),
    info = ParameterInfo(
      displayName = "textmutator.trimDirective.displayName",
      description = "textmutator.trimDirective.description"
    ),
  )

  val findAndReplaceDirective = FieldDirectiveDescriptor(
    ref = DirectiveRef("dea6e8a9-7bf9-4af5-a049-fc9a567ab3b4"),
    info = ParameterInfo(
      displayName = "textmutator.findAndReplaceDirective.displayName",
      description = "textmutator.findAndReplaceDirective.description"
    ),
    parameters = Seq(
      findPattern,
      replacePattern
    )
  )

  val toTimestampDirective = FieldDirectiveDescriptor(
    ref = DirectiveRef("e5666f54-abda-4cb6-87ba-5ad4fe96b6a3"),
    info = ParameterInfo(
      displayName = "textmutator.toTimestampDirective.displayName",
      description = "textmutator.toTimestampDirective.description"
    ),
    parameters = Seq(
      toTimestampPattern
    )
  )

  val fieldNamePatternParameter = FieldNamePatternParameterDescriptor(
    ref = "textmutator.fieldName",
    info = ParameterInfo(
      displayName = "textmutator.fieldName.displayName",
      description = "textmutator.fieldName.description"
    ),
    hint = PresentField,
    supports = Seq(PatternType.RegEx),
    mandatory = true
  )

  val sequenceInplaceParameter = BooleanParameterDescriptor(
    ref = "textmutator.directiveSequence.inplace",
    info = ParameterInfo(
      displayName = "textmutator.sequenceInplaceParameter.displayName",
      description = "textmutator.sequenceInplaceParameter.description"
    ),
    defaultValue = true,
  )

  val mutatedFieldName = FieldNameParameterDescriptor(
    ref = "textmutator.directiveSequence.mutatedFieldName",
    info = ParameterInfo(
      displayName = "textmutator.mutatedFieldName.displayName",
      description = "textmutator.mutatedFieldName.description"
    ),
  )

  val conditionalInplaceParameters = ParameterGroupDescriptor(
    ref = "textmutator.group.mutatedFieldName",
    condition = BooleanParameterCondition(sequenceInplaceParameter.ref, negate = true),
    parameters = Seq(mutatedFieldName)
  )

  val directiveSequence = FieldDirectiveSequenceParameterDescriptor(
    ref = "textmutator.directiveSequence",
    info = ParameterInfo(
      displayName = "textmutator.directiveSequence.displayName",
      description = "textmutator.directiveSequence.description"
    ),
    fieldTypes = Seq(Text),
    parameters = Seq(
      fieldNamePatternParameter,
      sequenceInplaceParameter,
      conditionalInplaceParameters
    ),
    directives = Seq(
      trimDirective,
      findAndReplaceDirective,
      toTimestampDirective
    ),
    minSequences = 1,
    maxSequences = MaxValue
  )

  override def describe = Descriptor(
    ref = "bf9c0ff2-64d5-44ed-9957-8128a50ab567",
    describes = FilterDescriptor(
      name = classOf[TextMutatorLogic].getName,
      displayName = TextRef("textmutator.displayName"),
      description = TextRef("textmutator.description"),
      categories = Seq(FIELDS),
      parameters = Seq(directiveSequence),
      maturity = Experimental,
      icon = Icon.fromClass(classOf[TextMutatorLogic]),
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.filter.textmutator.TextMutatorLogic",
      Locale.ENGLISH, Locale.GERMAN
    ) ++ CATEGORY_LOCALIZATION
  )
}

class TextMutatorLogic(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(parameters, shape) with StageLogging {

  var sequences = Seq.empty[TextMutatorDirectiveSequence[FieldDirective]]

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: Configuration): Unit = {

    sequences = configuration.getValueOrDefault(TextMutatorLogic.directiveSequence, Seq.empty)
      .foldLeft(mutable.ListBuffer.empty[TextMutatorDirectiveSequence[FieldDirective]]) { case (result, sequenceConfiguration) =>

        val directives = sequenceConfiguration.directives.foldLeft(Seq.empty[FieldDirective]) {
          case (result, DirectiveConfiguration(toTimestampDirective.ref, instance, parameters)) =>
            val timestampPattern = parameters.getValueOrDefault(TextMutatorLogic.toTimestampPattern, "")
            result :+ TextToTimestampDirective(timestampPattern)
          case (result, DirectiveConfiguration(findAndReplaceDirective.ref, instance, parameters)) =>
            val findPattern = parameters.getValueOrDefault(TextMutatorLogic.findPattern, "")
            val replacePattern = parameters.getValueOrDefault(TextMutatorLogic.replacePattern, "")
            result :+ FindReplaceDirective(findPattern, replacePattern)
          case (result, DirectiveConfiguration(trimDirective.ref, instance, parameters)) =>
            result :+ TrimDirective()
          case (result, _) =>
            result
        }

        val config = (
          sequenceConfiguration.parameters.findValue(TextMutatorLogic.fieldNamePatternParameter),
          sequenceConfiguration.parameters.getValueOrDefault(TextMutatorLogic.sequenceInplaceParameter, TextMutatorLogic.sequenceInplaceParameter.defaultValue),
          sequenceConfiguration.parameters.findValue(TextMutatorLogic.mutatedFieldName),
        )
        config match {
          case (Some(pattern), false, Some(name)) if directives.nonEmpty =>
            result += TextMutatorDirectiveSequence(pattern, inplace = false, name, directives)
          case (Some(pattern), true, _) if directives.nonEmpty =>
            result += TextMutatorDirectiveSequence(pattern, inplace = true, "", directives)
          case _ => result
        }
      }.toSeq
  }

  override def onPush(): Unit = {

    if (sequences.nonEmpty) {

      val dataset = grab(in)

      push(out, dataset.update(_.records := dataset.records.map((_, sequences)).foldLeft(mutable.ListBuffer.empty[Record]) {
        case (records, (record, sequences)) =>
          records += sequences.foldLeft(record) { case (record, sequence) =>
            record.update(_.fields := record.fields.foldLeft(mutable.ListBuffer.empty[Field]) {
              case (fields, field @ Field(fieldName, TextValue(_))) if sequence.fieldNamePattern.matches(fieldName) =>
                val mutated = sequence.directives.foldLeft(field) { case (field, directive) =>
                  directive.invoke(field)
                }
                if (sequence.inplace) {
                  fields += mutated.withName(fieldName)
                }
                else {
                  fields ++= Seq(field, mutated.withName(sequence.mutatedFieldName))
                }

              case (fields, field) => fields += field
            }.toList)
          }
      }.toList))
    }
    else {
      push(out, grab(in))
    }
  }

  override def onPull(): Unit = {
    pull(in)
  }
}
