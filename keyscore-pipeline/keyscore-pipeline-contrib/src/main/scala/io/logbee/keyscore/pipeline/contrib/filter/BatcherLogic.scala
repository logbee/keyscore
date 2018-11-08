package io.logbee.keyscore.pipeline.contrib.filter

import akka.stream.FlowShape
import akka.stream.stage.StageLogging
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.descriptor.Descriptor
import io.logbee.keyscore.pipeline.api.{FilterLogic, LogicParameters}

object BatcherLogic extends Described {

  //TODO Make a item for this filter

  //1.1 FieldNameParameter Descriptor for the name of the field that contains the ~start-message

  //1.2 ExpressionParameter Descriptor or TextParameter Descriptor for the pattern that matches the ~start-message

  //1.1 FieldNameParameter Descriptor for the name of the field that contains the ~end-message

  //1.2 ExpressionParameter Descriptor or TextParameter Descriptor for the pattern that matches the ~end-message

  override def describe: Descriptor = ???

  //TODO Fill the Localization Bundles
}

/**
  * LOOK AT THE LoggerFilter | RetainFieldsFilterLogic | GrokFilterLogic and their Specs for examples
  */

/**
  * The Batcher collects all Datasets for a specific Range.
  * It starts collecting for the first Dataset whose Field matches the given ~starting-pattern.
  * It then pushes all following Datasets (incl. the starting one) on a stack.
  * It finishes collecting with the Dataset whose Field matches the given ~end-pattern.
  *
  * After the last dataset is pushed on the stack, a new Dataset - containing all records on the stack -
  * is created and is pushed to the next actor.
  *
  * Then he looks for the next Dataset that matches the ~staring-pattern to begin collecting again.
  *
  * @param parameters -
  * @param shape -
  */

class BatcherLogic(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(parameters, shape) with StageLogging {

  //TODO Write a spec

  /**
    * This function is called when the filter BEFORE this filter is pushing this Dataset to this Batcher filter
    */
  override def onPush(): Unit = ???
  //1. Collect the Dataset from the filter before
  //2. The Logic
  //3. If the Stack is complete, create a new Dataset and give it the the next filter | If not, request a new Dataset from the filter before

  /**
    * This function is called when the filter AFTER this filter is requesting a Dataset from this Batcher filter
    */
  override def onPull(): Unit = ???
  //Request a new Dataset from the filter before

  //Initial Setup
  override def initialize(configuration: Configuration): Unit = ???
  //Just configure this filter

  //Configuration of this filter
  override def configure(configuration: Configuration): Unit = ???
  //Get the necessary parameters from the given configuration
  //Create variables for the start and ending pattern
  //Create an empty Stack for incoming Datasets

  //The actual Logic
  private def someBatchLogic: Unit = ???

  //Combine datasets from the stack to one single new one
  private def someCombineLogic: Dataset = ???

}
