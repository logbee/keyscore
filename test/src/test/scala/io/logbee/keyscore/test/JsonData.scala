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
  val JSONType = ".json"

  val BLUEPRINTS = "blueprints"
  val CONFIGURATIONS = "configurations"
  val DESCRIPTORS = "descriptors"

  val K2K = "kafkaToKafka"
  val K2E = "kafkaToElastic"
  val WORKFLOW = "workflow"

  def loadJson(resource: String, pipeline: String, name: String): String = {
    Using.using(getClass.getResourceAsStream(ResourcePath + resource + "/" + pipeline + "/" + name + JSONType)) { stream =>
      fromInputStream(stream).mkString
    }
  }

  def loadPipelineBlueprint(pipeline: String, filename: String): PipelineBlueprint = {
    read[PipelineBlueprint](loadJson(BLUEPRINTS, pipeline, filename))
  }

  def loadSourceBlueprint(pipeline: String, filename: String): SourceBlueprint = {
    read[SourceBlueprint](loadJson(BLUEPRINTS, pipeline, filename))
  }

  def loadFilterBlueprint(pipeline: String, filename: String): FilterBlueprint = {
    read[FilterBlueprint](loadJson(BLUEPRINTS, pipeline, filename))
  }

  def loadSinkBlueprint(pipeline: String, filename: String): SinkBlueprint = {
    read[SinkBlueprint](loadJson(BLUEPRINTS, pipeline, filename))
  }

  def loadConfiguration(pipeline: String, filename: String): Configuration = {
    read[Configuration](loadJson(CONFIGURATIONS, pipeline, filename))
  }

  def loadDescriptor(pipeline: String, filename: String): Descriptor = {
    read[Descriptor](loadJson(DESCRIPTORS, pipeline, filename))
  }


}
