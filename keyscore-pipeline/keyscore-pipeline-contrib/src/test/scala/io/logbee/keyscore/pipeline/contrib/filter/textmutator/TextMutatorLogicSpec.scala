package io.logbee.keyscore.pipeline.contrib.filter.textmutator

import java.util.UUID

import akka.stream.FlowShape
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import io.logbee.keyscore.model.configuration._
import io.logbee.keyscore.model.data.{Field, _}
import io.logbee.keyscore.model.descriptor.FieldNamePatternParameterDescriptor
import io.logbee.keyscore.model.descriptor.FieldNamePatternParameterDescriptor.PatternType
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

  class TestStream(configuration: Configuration) {
    val context = StageContext(system, executionContext)
    val provider = (parameters: LogicParameters, s: FlowShape[Dataset, Dataset]) => new TextMutatorLogic(parameters, s)
    val filterStage = new FilterStage(LogicParameters(UUID.randomUUID(), context, configuration), provider)

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

    "when configured without any directive" - {

      val configuration = Configuration(FieldDirectiveSequenceParameter(
        TextMutatorLogic.directiveSequence.ref, Seq(
          FieldDirectiveSequenceConfiguration(
            id = "1",
            parameters = ParameterSet(Seq(
              FieldNamePatternParameter(
                TextMutatorLogic.fieldNamePatternParameter.ref,
                value = "message",
                patternType = PatternType.None
              ),
            )),
            directives = Seq.empty
          )
        )
      ))

      "should pass datasets unmodified" in new TestStream(configuration) {

        sink.request(1)
        source.sendNext(Dataset(Record(Field("message", TextValue("   hello world   ")))))

        sink.requestNext().records.head.fields should contain only (
          Field("message", TextValue("   hello world   "))
        )
      }
    }

    "when configured inplace" - {

      val configuration = Configuration(FieldDirectiveSequenceParameter(
        TextMutatorLogic.directiveSequence.ref, Seq(
          FieldDirectiveSequenceConfiguration(
            id = "1",
            parameters = ParameterSet(Seq(
              FieldNamePatternParameter(
                TextMutatorLogic.fieldNamePatternParameter.ref,
                value = "message",
                patternType = PatternType.None
              ),
              BooleanParameter(TextMutatorLogic.sequenceInplaceParameter.ref, true),
            )),
            directives = Seq(DirectiveConfiguration(TextMutatorLogic.trimDirective.ref))
          )
        )
      ))

      "should replace the configured field" in new TestStream(configuration) {

        sink.request(1)
        source.sendNext(Dataset(Record(Field("message", TextValue("   hello world   ")))))

        sink.requestNext().records.head.fields should contain only (
          Field("message", TextValue("hello world"))
        )
      }
    }

    "when configured _not_ inplace" - {

      val configuration = Configuration(FieldDirectiveSequenceParameter(
        TextMutatorLogic.directiveSequence.ref, Seq(
          FieldDirectiveSequenceConfiguration(
            id = "1",
            parameters = ParameterSet(Seq(
              FieldNamePatternParameter(
                TextMutatorLogic.fieldNamePatternParameter.ref,
                value = "message",
                patternType = PatternType.None
              ),
              BooleanParameter(TextMutatorLogic.sequenceInplaceParameter.ref, false),
              FieldNameParameter(TextMutatorLogic.mutatedFieldName.ref, "mutated_message")
            )),
            directives = Seq(DirectiveConfiguration(TextMutatorLogic.trimDirective.ref))
          )
        )
      ))

      "should add a new field with the configured name" in new TestStream(configuration) {

        sink.request(1)
        source.sendNext(Dataset(Record(Field("message", TextValue("   hello world   ")))))

        sink.requestNext().records.head.fields should contain only (
          Field("message", TextValue("   hello world   ")),
          Field("mutated_message", TextValue("hello world"))
        )
      }
    }

    "when configured with a regex pattern" - {

      val configuration = Configuration(FieldDirectiveSequenceParameter(
        TextMutatorLogic.directiveSequence.ref, Seq(
          FieldDirectiveSequenceConfiguration(
            id = "1",
            parameters = ParameterSet(Seq(
              FieldNamePatternParameter(
                TextMutatorLogic.fieldNamePatternParameter.ref,
                value = ".*[Mm]essage$",
                patternType = PatternType.RegEx
              ),
            )),
            directives = Seq(DirectiveConfiguration(TextMutatorLogic.trimDirective.ref))
          )
        )
      ))

      val sample = Dataset(Record(
        Field("UserMessage", TextValue(" Hello World")),
        Field("system-message", TextValue(" Something went wrong!   ")),
        Field("temperature", TextValue("11"))
      ))

      "should mutated matching fields only" in new TestStream(configuration) {

        sink.request(1)
        source.sendNext(sample)

        sink.requestNext().records.head.fields should contain only (
          Field("UserMessage", TextValue("Hello World")),
          Field("system-message", TextValue("Something went wrong!")),
          Field("temperature", TextValue("11"))
        )
      }
    }

    /*
      "when configured with a find and replace directive" - {
      "should replace all occurrences of configured find string with configured replace string" ignore new TestStream {
        whenReady(filterFuture) { filter =>
          val configuration = Configuration(FieldDirectiveSequenceParameter(
            TextMutatorLogic.directiveSequence.ref, Seq(
              FieldDirectiveSequenceConfiguration(
                id = "1",
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
      "should convert timestamp string with date and time to timestamp value" ignore new TestStream {
        whenReady(filterFuture) { filter =>
          val configuration = Configuration(FieldDirectiveSequenceParameter(
            TextMutatorLogic.directiveSequence.ref, Seq(
              FieldDirectiveSequenceConfiguration(
                id = "1",
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
      "should convert timestamp string with only date to timestamp value" ignore new TestStream {
        whenReady(filterFuture) { filter =>
          val configuration = Configuration(FieldDirectiveSequenceParameter(
            TextMutatorLogic.directiveSequence.ref, Seq(
              FieldDirectiveSequenceConfiguration(
                id = "1",
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
      "should convert timestamp string with date time and zone to timestamp value" ignore new TestStream {
        whenReady(filterFuture) { filter =>
          val configuration = Configuration(FieldDirectiveSequenceParameter(
            TextMutatorLogic.directiveSequence.ref, Seq(
              FieldDirectiveSequenceConfiguration(
                id = "1",
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
      "should return the field without any changes" ignore new TestStream {
        whenReady(filterFuture) { filter =>
          val configuration = Configuration(FieldDirectiveSequenceParameter(
            TextMutatorLogic.directiveSequence.ref, Seq(
              FieldDirectiveSequenceConfiguration(
                id = "1",
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
    }*/
  }
}


