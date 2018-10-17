package io.logbee.keyscore.pipeline.contrib.filter

import java.util.UUID

import akka.stream.FlowShape
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import io.logbee.keyscore.model.configuration.{Configuration, DirectiveConfiguration, FieldDirectiveSequenceConfiguration, FieldDirectiveSequenceParameter}
import io.logbee.keyscore.model.data.{Dataset, Field, Record, TextValue}
import io.logbee.keyscore.pipeline.api.LogicParameters
import io.logbee.keyscore.pipeline.api.stage.{FilterStage, StageContext}
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FreeSpec, Inside, Matchers}


@RunWith(classOf[JUnitRunner])
class TextMutatorLogicSpec extends FreeSpec with Matchers with ScalaFutures with MockFactory with Inside with TestSystemWithMaterializerAndExecutionContext {

  trait TestStream {
    val context = StageContext(system, executionContext)
    val provider = (parameters: LogicParameters, s: FlowShape[Dataset, Dataset]) => new TextMutatorLogic(parameters, s)
    val filterStage = new FilterStage(LogicParameters(UUID.randomUUID(), context, Configuration()), provider)

    val ((source, filterFuture), sink) = Source.fromGraph(TestSource.probe[Dataset])
      .viaMat(filterStage)(Keep.both)
      .toMat(TestSink.probe[Dataset])(Keep.both)
      .run()
  }

  "A TextMutator" - {

    val sampleData = Dataset(Record(
      Field("message", TextValue(" keyscore   ")),
      Field("temperature", TextValue("11,5"))
    ))

    "(when configured with a trim directive)" - {

      "should remove leading and trailing spaces of the configured field" in new TestStream {

        whenReady(filterFuture) { filter =>

          val configuration = Configuration(FieldDirectiveSequenceParameter(
            TextMutatorLogic.directiveSequence.ref, Seq(
              FieldDirectiveSequenceConfiguration(
                fieldName = "message",
                directives = Seq(
                  DirectiveConfiguration(
                    TextMutatorLogic.trimDirective.ref
                  )
                )
              )
            )
          ))

          whenReady(filter.configure(configuration)) { _ =>

            source sendNext sampleData

            sink request 1
            val result = sink requestNext
            val record = result.records.head

            inside(record.fields.head) { case Field("message", TextValue(text)) =>
              text shouldBe "keyscore"
            }
          }
        }
      }
    }
  }
}
