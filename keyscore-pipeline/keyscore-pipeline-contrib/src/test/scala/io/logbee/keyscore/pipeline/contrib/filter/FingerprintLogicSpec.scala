package io.logbee.keyscore.pipeline.contrib.filter

import io.logbee.keyscore.model.configuration.{BooleanParameter, Configuration, FieldNameListParameter}
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.descriptor.ToParameterRef.toRef
import io.logbee.keyscore.pipeline.testkit.TestStreamForFilter
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class FingerprintLogicSpec extends AnyFreeSpec with Matchers with ScalaFutures with TestSystemWithMaterializerAndExecutionContext {

  "A FingerprintLogic" - {

    val sampleA = Dataset(
      Record(TextField("message", "Hello World")),
      Record(TextField("message", "Bye Bye")),
      Record(TextField("message", "Hello World"))
    )

    val sampleB = Dataset(
      Record(TextField("message", "Hello World")),
    )

    "return a Descriptor" in {
      FingerprintLogic.describe should not be null
    }

    "with default configuration" - {

      "should compute different fingerprints for different record" in new TestStreamForFilter[FingerprintLogic]() {

        whenReady(filterFuture) { _ =>

          sink.request(1)
          source.sendNext(sampleA)

          val result = sink.requestNext()
          val record0 = result.records(0)
          val record1 = result.records(1)
          val record2 = result.records(2)

          record0.fields should contain only (
            Field("message", TextValue("Hello World")),
            Field("fingerprint", TextValue("7cdf34d3652fa02c411e2c6feb6a060b"))
          )
          record1.fields should contain only (
            Field("message", TextValue("Bye Bye")),
            Field("fingerprint", TextValue("6fb0887a6d7c1bbc08ff7094b8f29030"))
          )
          record2.fields should contain only (
            Field("message", TextValue("Hello World")),
            Field("fingerprint", TextValue("7cdf34d3652fa02c411e2c6feb6a060b"))
          )
        }
      }

      "should respect the mime-type of TextValues when computing the fingerprint" in new TestStreamForFilter[FingerprintLogic]() {

        import io.logbee.keyscore.model.util.ToOption.T2OptionT

        whenReady(filterFuture) { _ =>

          val sample1 = Dataset(Record(Field("message", TextValue("{ \"message\": \"HelloWorld\" }", MimeType("application", "json")))))
          val sample2 = Dataset(Record(Field("message", TextValue("{ \"message\": \"HelloWorld\" }", MimeType("application", "text")))))

          val expectedResult1 = Dataset(Record(
            Field("message", TextValue("{ \"message\": \"HelloWorld\" }", MimeType("application", "json"))),
            Field("fingerprint", TextValue("372bfb5abe0b3356527b092e0bdd5457")),
          ))

          val expectedResult2 = Dataset(Record(
            Field("message", TextValue("{ \"message\": \"HelloWorld\" }", MimeType("application", "text"))),
            Field("fingerprint", TextValue("4e87c28e3eed054d2ea829d481524ed0")),
          ))

          var result: Dataset = null

          source.sendNext(sample1)
          result = sink.requestNext()

          result shouldBe expectedResult1

          source.sendNext(sample2)
          result = sink.requestNext()

          result should not be expectedResult1 // due to different mime-type
          result shouldBe expectedResult2
        }
      }
    }

    "when configured to encode into base64" - {

      val configuration = Configuration(
        BooleanParameter(FingerprintLogic.encodingParameter, true)
      )

      "should compute a fingerprint encoded in base64" in new TestStreamForFilter[FingerprintLogic](configuration) {

        whenReady(filterFuture) { _ =>

          sink.request(1)
          source.sendNext(sampleB)

          sink.requestNext().records.head.fields should contain only(
            Field("message", TextValue("Hello World")),
            Field("fingerprint", TextValue("fN8002UvoCxBHixv62oGCw=="))
          )
        }
      }
    }

    "when configured to not recompute fingerprints" - {

      val configuration = Configuration(
        BooleanParameter(FingerprintLogic.recomputeParameter, false)
      )

      val sample = Dataset(
        Record(
          Field("message", TextValue("Hello World")),
          Field("fingerprint", TextValue("I'm a fingerprint :-P"))
        ),
        Record(Field("message", TextValue("Hello World")))
      )

      "should not touch records which already have a fingerprint" in new TestStreamForFilter[FingerprintLogic](configuration) {

        whenReady(filterFuture) { _ =>

          sink.request(1)
          source.sendNext(sample)

          val result = sink.requestNext()
          val record0 = result.records(0)
          val record1 = result.records(1)

          record0.fields should contain only (
            Field("message", TextValue("Hello World")),
            Field("fingerprint", TextValue("I'm a fingerprint :-P"))
          )
          record0.fields should have size 2

          record1.fields should contain only (
            Field("message", TextValue("Hello World")),
            Field("fingerprint", TextValue("7cdf34d3652fa02c411e2c6feb6a060b"))
          )
          record1.fields should have size 2
        }
      }
    }

    "when configured to recompute fingerprints" - {

      val configuration = Configuration(
        BooleanParameter(FingerprintLogic.recomputeParameter, true)
      )

      val sample = Dataset(
        Record(
          Field("message", TextValue("Hello World")),
          Field("fingerprint", TextValue("I'm a fingerprint :-P"))
        ),
        Record(Field("message", TextValue("Hello World")))
      )

      "should update the fingerprint field of a record" in new TestStreamForFilter[FingerprintLogic](configuration) {

        whenReady(filterFuture) { _ =>

          sink.request(1)
          source.sendNext(sample)

          val result = sink.requestNext()
          val record0 = result.records(0)
          val record1 = result.records(1)

          record0.fields should contain only (
            Field("message", TextValue("Hello World")),
            Field("fingerprint", TextValue("7cdf34d3652fa02c411e2c6feb6a060b"))
          )
          record0.fields should have size 2

          record1.fields should contain only (
            Field("message", TextValue("Hello World")),
            Field("fingerprint", TextValue("7cdf34d3652fa02c411e2c6feb6a060b"))
          )
          record1.fields should have size 2
        }
      }
    }

    "should compute the fingerprint of the configured fields" - {

      val sample = Dataset(
        Record(
          Field("message", TextValue("The weather is stormy.")),
          Field("timestamp", TextValue("1990-02-26 21:35:59.605"))
        )
      )

      "when one field is included" in new TestStreamForFilter[FingerprintLogic](
        Configuration(
          FieldNameListParameter(FingerprintLogic.includesParameter.ref, Seq("message"))
        ))
      {
        whenReady(filterFuture) { _ =>

          source.sendNext(sample)
          sink.requestNext() shouldBe Dataset(Record(
            Field("message", TextValue("The weather is stormy.")),
            Field("timestamp", TextValue("1990-02-26 21:35:59.605")),
            Field("fingerprint", TextValue("2fb0375b4bee4b475b94f7c567f238f3"))
          ))
        }
      }

      "when more fields are included" in new TestStreamForFilter[FingerprintLogic](
        Configuration(
          FieldNameListParameter(FingerprintLogic.includesParameter.ref, Seq("message", "timestamp"))
        ))
      {
        whenReady(filterFuture) { _ =>

          source.sendNext(sample)
          sink.requestNext() shouldBe Dataset(Record(
            Field("message", TextValue("The weather is stormy.")),
            Field("timestamp", TextValue("1990-02-26 21:35:59.605")),
            Field("fingerprint", TextValue("0e26e87312eeeefb3146d8d5fd6f7550"))
          ))
        }
      }

      "when a field is excluded" in new TestStreamForFilter[FingerprintLogic](
        Configuration(
          FieldNameListParameter(FingerprintLogic.excludesParameter.ref, Seq("timestamp"))
        ))
      {
        whenReady(filterFuture) { _ =>

          source.sendNext(sample)
          sink.requestNext() shouldBe Dataset(Record(
            Field("message", TextValue("The weather is stormy.")),
            Field("timestamp", TextValue("1990-02-26 21:35:59.605")),
            Field("fingerprint", TextValue("2fb0375b4bee4b475b94f7c567f238f3"))
          ))
        }
      }
    }

    "should create a 'fingerprint.fields' field (when enabled)" in new TestStreamForFilter[FingerprintLogic](
      Configuration(
        BooleanParameter(FingerprintLogic.fingerprintFieldsEnabledParameter, true)
      ))
    {
      val sample = Dataset(
        Record(
          Field("message", TextValue("The weather is stormy.")),
          Field("timestamp", TextValue("1990-02-26 21:35:59.605"))
        )
      )

      whenReady(filterFuture) { _ =>

        source.sendNext(sample)
        sink.requestNext shouldBe Dataset(Record(
          Field("message", TextValue("The weather is stormy.")),
          Field("timestamp", TextValue("1990-02-26 21:35:59.605")),
          Field("fingerprint", TextValue("0e26e87312eeeefb3146d8d5fd6f7550")),
          Field("fingerprint.fields", TextValue("[\"message\", \"timestamp\"]"))
        ))
      }
    }
  }
}
