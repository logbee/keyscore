package io.logbee.keyscore.pipeline.contrib.filter

import io.logbee.keyscore.model.configuration.{BooleanParameter, Configuration}
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.descriptor.ToParameterRef.toRef
import io.logbee.keyscore.pipeline.contrib.test.TestStreamFor
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FreeSpec, Matchers}

class FingerprintLogicSpec extends FreeSpec with Matchers with ScalaFutures with TestSystemWithMaterializerAndExecutionContext {

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

      "should compute different fingerprints for different record" in new TestStreamFor[FingerprintLogic]() {

        whenReady(filterFuture) { _ =>

          sink.request(1)
          source.sendNext(sampleA)

          val result = sink.requestNext()
          val record0 = result.records(0)
          val record1 = result.records(1)
          val record2 = result.records(2)

          record0.fields should contain only (
            Field("message", TextValue("Hello World")),
            Field("fingerprint", TextValue("63b06b1fa1b8436fcd215b3ac78b7783"))
          )
          record1.fields should contain only (
            Field("message", TextValue("Bye Bye")),
            Field("fingerprint", TextValue("caec3b55a0acd7383a95d0242f067cc9"))
          )
          record2.fields should contain only (
            Field("message", TextValue("Hello World")),
            Field("fingerprint", TextValue("63b06b1fa1b8436fcd215b3ac78b7783"))
          )
        }
      }
    }

    "when configured to encode into base64" - {

      val configuration = Configuration(
        BooleanParameter(FingerprintLogic.encodingParameter, true)
      )

      "should compute a fingerprint encoded in base64" in new TestStreamFor[FingerprintLogic](configuration) {

        whenReady(filterFuture) { _ =>

          sink.request(1)
          source.sendNext(sampleB)

          sink.requestNext().records.head.fields should contain only(
            Field("message", TextValue("Hello World")),
            Field("fingerprint", TextValue("Y7BrH6G4Q2/NIVs6x4t3gw=="))
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

      "should not touch records which already have a fingerprint" in new TestStreamFor[FingerprintLogic](configuration) {

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
            Field("fingerprint", TextValue("63b06b1fa1b8436fcd215b3ac78b7783"))
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

      "should update the fingerprint field of a record" in new TestStreamFor[FingerprintLogic](configuration) {

        whenReady(filterFuture) { _ =>

          sink.request(1)
          source.sendNext(sample)

          val result = sink.requestNext()
          val record0 = result.records(0)
          val record1 = result.records(1)

          record0.fields should contain only (
            Field("message", TextValue("Hello World")),
            Field("fingerprint", TextValue("63b06b1fa1b8436fcd215b3ac78b7783"))
          )
          record0.fields should have size 2

          record1.fields should contain only (
            Field("message", TextValue("Hello World")),
            Field("fingerprint", TextValue("63b06b1fa1b8436fcd215b3ac78b7783"))
          )
          record1.fields should have size 2
        }
      }
    }
  }
}
