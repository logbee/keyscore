package io.logbee.keyscore.pipeline.contrib

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.{FlowShape, SourceShape}
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import com.typesafe.config.ConfigFactory
import io.logbee.keyscore.model.configuration._
import io.logbee.keyscore.model.data.{Dataset, Field, TextValue}
import io.logbee.keyscore.pipeline.api.LogicParameters
import io.logbee.keyscore.pipeline.api.stage.{FilterStage, SourceStage, StageContext}
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FreeSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class ConstantSourceLogicSpec extends FreeSpec with Matchers with ScalaFutures with TestSystemWithMaterializerAndExecutionContext {

  val configuration = Configuration(
    parameterSet = ParameterSet(Seq(
      FieldNameParameter(ConstantSourceLogic.fieldNameParameter.ref, "input"),
      TextListParameter(ConstantSourceLogic.inputParameter.ref, Seq(
        "Hello World", "Bye Bye"
      )),
    ))
  )

  val context = StageContext(system, executionContext)
  val provider = (parameters: LogicParameters, s: SourceShape[Dataset]) => new ConstantSourceLogic(parameters, s)
  val sourceStage = new SourceStage(LogicParameters(UUID.randomUUID(), context, configuration), provider)

  val (sourceFuture, sink) = Source.fromGraph(sourceStage)
    .toMat(TestSink.probe[Dataset])(Keep.both)
    .run()

  "A ConstantSourceLogic" - {

    "should output the configured text" in {

      whenReady(sourceFuture) { source =>

        sink.request(3)

        var record = sink.requestNext().records.head

        record.fields should contain only Field("input", TextValue("Hello World"))

        record = sink.requestNext().records.head

        record.fields should contain only Field("input", TextValue("Bye Bye"))

        record = sink.requestNext().records.head

        record.fields should contain only Field("input", TextValue("Hello World"))
      }
    }
  }
}
