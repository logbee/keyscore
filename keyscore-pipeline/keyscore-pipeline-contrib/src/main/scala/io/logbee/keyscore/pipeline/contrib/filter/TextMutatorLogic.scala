package io.logbee.keyscore.pipeline.contrib.filter

import akka.stream.FlowShape
import akka.stream.stage.StageLogging
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.FieldValueType.Text
import io.logbee.keyscore.model.data.{Dataset, FieldValueType}
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.pipeline.contrib.CommonCategories
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.{FilterLogic, LogicParameters}
import io.logbee.keyscore.pipeline.contrib.CommonCategories.CATEGORY_LOCALIZATION
import io.logbee.keyscore.model.descriptor.ToDescriptorRef.stringToDescriptorRef


object TextMutatorLogic extends Described {

  private val directiveSequence = FieldDirectiveSequenceParameterDescriptor(
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
    )
  )

  private val trimDirective = FieldDirectiveDescriptor(
    ref = DirectiveRef("ab31ce5f-d582-48e6-9a76-80436a733678"),
    info = ParameterInfo(
      displayName = "textmutator.trimDirective.displayName",
      description = "textmutator.trimDirective.description"
    ),
  )

  private val findAndReplaceDirective = FieldDirectiveDescriptor(
    ref = DirectiveRef("dea6e8a9-7bf9-4af5-a049-fc9a567ab3b4"),
    info = ParameterInfo(
      displayName = "textmutator.findAndReplaceDirective.displayName",
      description = "textmutator.findAndReplaceDirective.description"
    ),
    parameters = Seq(
      TextParameterDescriptor(
        ref = "textmutator.findAndReplaceDirective.find",
        info = ParameterInfo(
          displayName = "textmutator.findAndReplaceDirective.find.displayName",
          description = "textmutator.findAndReplaceDirective.find.description"
        ),
        mandatory = true
      ),
      TextParameterDescriptor(
        ref = "textmutator.findAndReplaceDirective.replace",
        info = ParameterInfo(
          displayName = "textmutator.findAndReplaceDirective.replace.displayName",
          description = "textmutator.findAndReplaceDirective.replace.description"
        ),
        mandatory = true
      )
    )
  )

  private val toTimestampDirective = FieldDirectiveDescriptor(
    ref = DirectiveRef("dea6e8a9-7bf9-4af5-a049-fc9a567ab3b4"),
    info = ParameterInfo(
      displayName = "textmutator.toTimestampDirective.displayName",
      description = "textmutator.toTimestampDirective.description"
    ),
    parameters = Seq(
      TextParameterDescriptor(
        ref = "textmutator.toTimestampDirective.pattern",
        info = ParameterInfo(
          displayName = "textmutator.toTimestampDirective.pattern.displayName",
          description = "textmutator.toTimestampDirective.pattern.description"
        ),
        mandatory = true
      )
    )
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

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: Configuration): Unit = {}

  override def onPush(): Unit = {
    val dataset = grab(in)
    log.info(s"$dataset")
    push(out, dataset)
  }

  override def onPull(): Unit = {
    pull(in)
  }
}
