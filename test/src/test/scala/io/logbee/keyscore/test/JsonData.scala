package io.logbee.keyscore

import io.logbee.keyscore.model.blueprint.{FilterBlueprint, PipelineBlueprint, SinkBlueprint, SourceBlueprint}
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.descriptor.Descriptor
import io.logbee.keyscore.model.json4s.KeyscoreFormats
import io.logbee.keyscore.model.util.Using
import org.json4s.native.Serialization.read

import scala.io.Source.fromInputStream

object JsonData {

  implicit val formats = KeyscoreFormats.formats

  val ResourcePath = "/JSONFiles/"
  val ResourceType = ".json"

  val PipelineBlueprintPath = "blueprints/pipelineBlueprint"
  val SourceBlueprintPath = "blueprints/sourceBlueprint"
  val FilterBlueprintPath = "blueprints/filterBlueprint"
  val SinkBlueprintPath = "blueprints/sinkBlueprint"

  val KafkaSourceConfigurationPath = "configurations/sourceConfig"
  val FilterConfigurationPath = "configurations/removeFieldsFilterConfig"
  val KafkaSinkConfigurationPath = "configurations/sinkConfig"

  val KafkaSourceDescriptorPath = "descriptors/KafkaSourceLogic"
  val FilterDescriptorPath = "descriptors/RemoveFieldsFilter"
  val KafkaSinkDescriptorPath = "descriptors/KafkaSinkLogic"

  def loadJson(path: String): String = {
    Using.using(getClass.getResourceAsStream(ResourcePath + path + ResourceType)) { stream =>
      fromInputStream(stream).mkString
    }
  }

  def loadExamplePipelineBlueprint: PipelineBlueprint = {
    read[PipelineBlueprint](loadJson(PipelineBlueprintPath))
  }

  def loadExampleSourceBlueprint: SourceBlueprint = {
    read[SourceBlueprint](loadJson(SourceBlueprintPath))
  }

  def loadExampleFilterBlueprint: FilterBlueprint = {
    read[FilterBlueprint](loadJson(FilterBlueprintPath))
  }

  def loadExampleSinkBlueprint: SinkBlueprint = {
    read[SinkBlueprint](loadJson(SinkBlueprintPath))
  }

  def loadExampleSourceConfiguration: Configuration = {
    read[Configuration](loadJson(KafkaSourceConfigurationPath))
  }

  def loadExampleFilterConfiguration: Configuration = {
    read[Configuration](loadJson(FilterConfigurationPath))

  }

  def loadExampleSinkConfiguration: Configuration = {
    read[Configuration](loadJson(KafkaSinkConfigurationPath))

  }

  def loadExampleSourceDescriptor: Descriptor = {
    read[Descriptor](loadJson(KafkaSourceDescriptorPath))

  }

  def loadExampleFilterDescriptor: Descriptor = {
    read[Descriptor](loadJson(FilterDescriptorPath))

  }

  def loadExampleSinkDescriptor: Descriptor = {
    read[Descriptor](loadJson(KafkaSinkDescriptorPath))

  }

}
