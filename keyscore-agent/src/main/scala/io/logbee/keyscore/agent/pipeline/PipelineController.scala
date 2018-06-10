package io.logbee.keyscore.agent.pipeline

import java.util.UUID

import io.logbee.keyscore.agent.pipeline.stage.ValveProxy
import io.logbee.keyscore.model.PipelineConfiguration
import io.logbee.keyscore.model.filter.FilterProxy
import io.logbee.keyscore.model.sink.SinkProxy
import io.logbee.keyscore.model.source.SourceProxy

case class PipelineController(pipeline: Pipeline, controllers: List[Controller]) {
  def configuration: PipelineConfiguration = pipeline.configuration
  def id: UUID = configuration.id
}

object Controller {

  def sourceController(sourceProxy: SourceProxy, valveProxy: ValveProxy): Controller = {
    new SourceController
  }

  def filterController(inValveProxy: ValveProxy, filterProxy: FilterProxy, outValveProxy: ValveProxy): Controller = {
    new FilterController
  }

  def sinkController(valveProxy: ValveProxy, sinkProxy: SinkProxy): Controller = {
    new SinkController
  }
}

abstract class Controller {

}

class SourceController extends Controller

class SinkController extends Controller

class FilterController extends Controller