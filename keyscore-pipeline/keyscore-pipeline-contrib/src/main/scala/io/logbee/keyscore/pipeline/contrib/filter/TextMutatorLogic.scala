package io.logbee.keyscore.pipeline.contrib.filter

import akka.stream.FlowShape
import akka.stream.stage.StageLogging
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.configuration.{Configuration, DirectiveConfiguration}
import io.logbee.keyscore.model.data.{Dataset, Field}
import io.logbee.keyscore.model.data.FieldValueType.Text
import io.logbee.keyscore.model.descriptor.ToDescriptorRef.stringToDescriptorRef
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.{FilterLogic, LogicParameters}
import io.logbee.keyscore.pipeline.contrib.CommonCategories
import io.logbee.keyscore.pipeline.contrib.CommonCategories.CATEGORY_LOCALIZATION
import io.logbee.keyscore.pipeline.contrib.filter.TextMutatorLogic.{directiveSequence, findAndReplaceDirective, toTimestampDirective, trimDirective}

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
    ref = DirectiveRef("dea6e8a9-7bf9-4af5-a049-fc9a567ab3b4"),
    info = ParameterInfo(
      displayName = "textmutator.toTimestampDirective.displayName",
      description = "textmutator.toTimestampDirective.description"
    ),
    parameters = Seq(
      toTimestampPattern
    )
  )

  val directiveSequence = FieldDirectiveSequenceParameterDescriptor(
    ref = "textmutator.directiveSequence",
    info = ParameterInfo(
      displayName = "textmutator.directiveSequence.displayName",
      description = "textmutator.directiveSequence.description"
    ),
    fieldTypes = Seq(Text),
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
      name = classOf[LoggerFilter].getName,
      displayName = TextRef("textmutator.displayName"),
      description = TextRef("textmutator.description"),
      categories = Seq(CommonCategories.FIELDS),
      parameters = Seq(directiveSequence)
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.filter.TextMutatorLogic",
      Locale.ENGLISH, Locale.GERMAN
    ) ++ CATEGORY_LOCALIZATION
  )
}

class TextMutatorLogic(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(parameters, shape) with StageLogging {

  val seqs = mutable.ListBuffer.empty[FieldSequence]

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: Configuration): Unit = {
    val sequences = configuration.findValue(directiveSequence).get

    sequences.foreach(sequence => {


      val directives: Seq[TextMutatorDirective] = sequence.directives.map {
        case DirectiveConfiguration(toTimestampDirective.ref, parameters, order) =>
          val timestampPattern = parameters.getValueOrDefault(TextMutatorLogic.toTimestampPattern, "")
          ToTimestampDirective(timestampPattern)
        case DirectiveConfiguration(findAndReplaceDirective.ref, parameters, order) =>
          val findPattern = parameters.getValueOrDefault(TextMutatorLogic.findPattern, "")
          val replacePattern = parameters.getValueOrDefault(TextMutatorLogic.replacePattern, "")
          FindReplaceDirective(findPattern, replacePattern)
        case DirectiveConfiguration(trimDirective.ref, parameters, order) =>
          TrimDirective()
      }

      seqs.append(FieldSequence(sequence.fieldName, directives))
    })
  }

  override def onPush(): Unit = {
    val dataset = grab(in)
    log.info(s"$dataset")
    push(out, dataset)
  }

  override def onPull(): Unit = {
    pull(in)
  }

  case class FieldSequence(fieldName: String, directives: Seq[TextMutatorDirective])

  trait TextMutatorDirective {
    def process(field: Field): Field
  }

  case class TrimDirective() extends TextMutatorDirective {
    def process(field: Field): Field = {
      field
    }
  }

  case class FindReplaceDirective(find: String, replace: String) extends TextMutatorDirective {
    def process(field: Field): Field = {
      field
    }
  }

  case class ToTimestampDirective(pattern: String) extends TextMutatorDirective {
    def process(field: Field): Field = {
      field
    }
  }
}
