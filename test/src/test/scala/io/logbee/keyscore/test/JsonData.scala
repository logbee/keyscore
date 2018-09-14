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

  val K2KBlueprintsPath = "blueprints/kafkaToKafka/"
  val K2KConfigurationsPath = "configurations/kafkaToKafka/"
  val K2KDescriptorsPath = "descriptors/"

  val K2EBlueprintsPath = "blueprints/kafkaToElastic/"
  val K2EConfigurationsPath = "configurations/kafkaToElastic/"

  val PipelineBlueprintPath = "pipelineBlueprint"
  val SourceBlueprintPath = "sourceBlueprint"
  val FilterBlueprintPath = "filterBlueprint"
  val SinkBlueprintPath = "sinkBlueprint"

  val KafkaSourceConfigurationPath = "sourceConfig"
  val FilterConfigurationPath = "removeFieldsFilterConfig"
  val KafkaSinkConfigurationPath = "sinkConfig"

  val KafkaSourceDescriptorPath = "KafkaSourceLogic"
  val FilterDescriptorPath = "RemoveFieldsFilter"
  val KafkaSinkDescriptorPath = "KafkaSinkLogic"

  def loadJson(pipeline: String, name: String): String = {
    Using.using(getClass.getResourceAsStream(ResourcePath + pipeline + name + ResourceType)) { stream =>
      fromInputStream(stream).mkString
    }
  }

  //Kafka To Kafka Pipeline
  def loadK2KPipelineBlueprint: PipelineBlueprint = {
    read[PipelineBlueprint](loadJson(K2KBlueprintsPath, PipelineBlueprintPath))
  }

  def loadK2KSourceBlueprint: SourceBlueprint = {
    read[SourceBlueprint](loadJson(K2KBlueprintsPath, SourceBlueprintPath))
  }

  def loadK2KFilterBlueprint: FilterBlueprint = {
    read[FilterBlueprint](loadJson(K2KBlueprintsPath, FilterBlueprintPath))
  }

  def loadK2KSinkBlueprint: SinkBlueprint = {
    read[SinkBlueprint](loadJson(K2KBlueprintsPath, SinkBlueprintPath))
  }

  def loadK2KSourceConfiguration: Configuration = {
    read[Configuration](loadJson(K2KConfigurationsPath, KafkaSourceConfigurationPath))
  }

  def loadK2KFilterConfiguration: Configuration = {
    read[Configuration](loadJson(K2KConfigurationsPath, FilterConfigurationPath))
  }

  def loadK2KSinkConfiguration: Configuration = {
    read[Configuration](loadJson(K2KConfigurationsPath, KafkaSinkConfigurationPath))
  }

  def loadK2KSourceDescriptor: Descriptor = {
    read[Descriptor](loadJson(K2KDescriptorsPath, KafkaSourceDescriptorPath))
  }

  def loadK2KExampleFilterDescriptor: Descriptor = {
    read[Descriptor](loadJson(K2KDescriptorsPath, FilterDescriptorPath))
  }

  def loadK2KExampleSinkDescriptor: Descriptor = {
    read[Descriptor](loadJson(K2KDescriptorsPath, KafkaSinkDescriptorPath))
  }

  //Kafka To Elastic Pipeline
  def loadK2EPipelineBlueprint: PipelineBlueprint = {
    read[PipelineBlueprint](loadJson(K2KBlueprintsPath, PipelineBlueprintPath))
  }

  def loadK2ESourceBlueprint: SourceBlueprint = {
    read[SourceBlueprint](loadJson(K2KBlueprintsPath, SourceBlueprintPath))
  }

  def loadK2EFilterBlueprint: FilterBlueprint = {
    read[FilterBlueprint](loadJson(K2KBlueprintsPath, FilterBlueprintPath))
  }

  def loadK2ESinkBlueprint: SinkBlueprint = {
    read[SinkBlueprint](loadJson(K2KBlueprintsPath, SinkBlueprintPath))
  }

  def loadK2ESourceConfiguration: Configuration = {
    read[Configuration](loadJson(K2KConfigurationsPath, KafkaSourceConfigurationPath))
  }

  def loadK2EFilterConfiguration: Configuration = {
    read[Configuration](loadJson(K2KConfigurationsPath, FilterConfigurationPath))
  }

  def loadK2ESinkConfiguration: Configuration = {
    read[Configuration](loadJson(K2KConfigurationsPath, KafkaSinkConfigurationPath))
  }

}
