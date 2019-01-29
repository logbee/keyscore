package io.logbee.keyscore.pipeline.contrib.filter.textmutator

import java.util.UUID

import akka.stream.FlowShape
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import io.logbee.keyscore.model.configuration._
import io.logbee.keyscore.model.data._
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
      Field("temperature", TextValue("11,5")),
      Field("timestamp", TextValue("2018-12-24T00:00:00")),
      Field("timestampWOTime", TextValue("2018-12-24")),
      Field("timestampWZone", TextValue("2018-12-24T10:15:30+01:00"))
    ))

    "when configured with a trim directive" - {

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
    "when configured with a find and replace directive" - {
      "should replace all occurrences of configured find string with configured replace string" in new TestStream {
        whenReady(filterFuture) { filter =>
          val configuration = Configuration(FieldDirectiveSequenceParameter(
            TextMutatorLogic.directiveSequence.ref, Seq(
              FieldDirectiveSequenceConfiguration(
                fieldName = "message",
                directives = Seq(
                  DirectiveConfiguration(TextMutatorLogic.findAndReplaceDirective.ref, null, ParameterSet(Seq(
                    TextParameter(TextMutatorLogic.findPattern.ref, "keyscore"),
                    TextParameter(TextMutatorLogic.replacePattern.ref, "test success")
                  )))
                )
              )
            )
          ))

          whenReady(filter.configure(configuration)) { _ =>
            source sendNext sampleData

            sink request 1
            val result = sink.requestNext
            val record = result.records.head

            inside(record.fields.head) { case Field("message", TextValue(text)) =>
              text shouldBe " test success   "
            }
          }
        }
      }
    }
    "when configured with a to timestamp directive" - {
      "should convert timestamp string with date and time to timestamp value" in new TestStream {
        whenReady(filterFuture) { filter =>
          val configuration = Configuration(FieldDirectiveSequenceParameter(
            TextMutatorLogic.directiveSequence.ref, Seq(
              FieldDirectiveSequenceConfiguration(
                fieldName = "timestamp",
                directives = Seq(
                  DirectiveConfiguration(TextMutatorLogic.toTimestampDirective.ref, null, ParameterSet(Seq(
                    TextParameter(TextMutatorLogic.toTimestampPattern.ref, "yyyy-MM-dd'T'HH:mm:ss")
                  )))
                )
              )
            )
          ))

          whenReady(filter.configure(configuration)) { _ =>
            source sendNext sampleData

            sink request 1
            val result = sink.requestNext
            val record = result.records.head

            inside(record.fields.filter(field => field.name.equals("timestamp")).head) {
              case Field("timestamp", TimestampValue(seconds, nanos)) =>
                seconds shouldBe 1545609600
                nanos shouldBe 0
            }
          }
        }
      }
    }
    "when configured with a to timestamp directive" - {
      "should convert timestamp string with only date to timestamp value" in new TestStream {
        whenReady(filterFuture) { filter =>
          val configuration = Configuration(FieldDirectiveSequenceParameter(
            TextMutatorLogic.directiveSequence.ref, Seq(
              FieldDirectiveSequenceConfiguration(
                fieldName = "timestampWOTime",
                directives = Seq(
                  DirectiveConfiguration(TextMutatorLogic.toTimestampDirective.ref, null, ParameterSet(Seq(
                    TextParameter(TextMutatorLogic.toTimestampPattern.ref, "yyyy-MM-dd")
                  )))
                )
              )
            )
          ))

          whenReady(filter.configure(configuration)) { _ =>
            source sendNext sampleData

            sink request 1
            val result = sink.requestNext
            val record = result.records.head

            inside(record.fields.filter(field => field.name.equals("timestampWOTime")).head) {
              case Field("timestampWOTime", TimestampValue(seconds, nanos)) =>
                seconds shouldBe 1545609600
                nanos shouldBe 0
            }
          }
        }
      }
    }
    "when configured with a to timestamp directive" - {
      "should convert timestamp string with date time and zone to timestamp value" in new TestStream {
        whenReady(filterFuture) { filter =>
          val configuration = Configuration(FieldDirectiveSequenceParameter(
            TextMutatorLogic.directiveSequence.ref, Seq(
              FieldDirectiveSequenceConfiguration(
                fieldName = "timestampWZone",
                directives = Seq(
                  DirectiveConfiguration(TextMutatorLogic.toTimestampDirective.ref, null, ParameterSet(Seq(
                    TextParameter(TextMutatorLogic.toTimestampPattern.ref, "yyyy-MM-dd'T'HH:mm:ssXXX")
                  )))
                )
              )
            )
          ))

          whenReady(filter.configure(configuration)) { _ =>
            source sendNext sampleData

            sink request 1
            val result = sink.requestNext
            val record = result.records.head

            inside(record.fields.filter(field => field.name.equals("timestampWZone")).head) {
              case Field("timestampWZone", TimestampValue(seconds, nanos)) =>
                seconds shouldBe 1545642930
                nanos shouldBe 0
            }
          }
        }
      }
    }
    "when configured with a to timestamp directive with wrong pattern" - {
      "should return the field without any changes" in new TestStream {
        whenReady(filterFuture) { filter =>
          val configuration = Configuration(FieldDirectiveSequenceParameter(
            TextMutatorLogic.directiveSequence.ref, Seq(
              FieldDirectiveSequenceConfiguration(
                fieldName = "timestampWZone",
                directives = Seq(
                  DirectiveConfiguration(TextMutatorLogic.toTimestampDirective.ref, null, ParameterSet(Seq(
                    TextParameter(TextMutatorLogic.toTimestampPattern.ref, "yyyy-MM-dd'T'HH:mm:ss")
                  )))
                )
              )
            )
          ))

          whenReady(filter.configure(configuration)) { _ =>
            source sendNext sampleData

            sink request 1
            val result = sink.requestNext
            val record = result.records.head

            inside(record.fields.filter(field => field.name.equals("timestampWZone")).head) {
              case Field("timestampWZone", TextValue(value)) =>
                value shouldBe "2018-12-24T10:15:30+01:00"
            }
          }
        }
      }
    }
  }
}


