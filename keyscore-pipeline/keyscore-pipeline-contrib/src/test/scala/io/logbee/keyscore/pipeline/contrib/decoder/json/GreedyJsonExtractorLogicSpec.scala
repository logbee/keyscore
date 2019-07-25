package io.logbee.keyscore.pipeline.contrib.decoder.json

import io.logbee.keyscore.model.configuration._
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.pipeline.testkit.TestStreamForFilter
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.junit.JUnitRunner
import org.scalatest.{FreeSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class GreedyJsonExtractorLogicSpec extends FreeSpec with Matchers with ScalaFutures with TestSystemWithMaterializerAndExecutionContext {

  val samples = Seq(
    (
      """Pizza Order {"price": 15.42, "pizza" : "hawai", "amount": 2, "paid": true} by KESCORE""", Seq(
      Field("greedy.0.price", DecimalValue(15.42)),
      Field("greedy.0.pizza", TextValue("hawai")),
      Field("greedy.0.amount", NumberValue(2)),
      Field("greedy.0.paid", BooleanValue(true)))),

    (
      """Users {"name": "Elmar"}, {"name" : "Manuel"}""", Seq(
      Field("greedy.0.name", TextValue("Elmar")),
      Field("greedy.1.name", TextValue("Manuel"))
    )),

    (
      """Users {"name":{"surname": "Elmar", "lastname": "Schug"}}, {"name" : "Manuel"}""", Seq(
      Field("greedy.0.name.surname", TextValue("Elmar")),
      Field("greedy.0.name.lastname", TextValue("Schug")),
      Field("greedy.1.name", TextValue("Manuel"))
    )),

    ("""Users {""", Seq()),

    ("""Users }""", Seq()),

    ("""Users""", Seq()),

    ("""Users {"name" : "Emme"} {""", Seq(
      Field("greedy.0.name", TextValue("Emme"))
    )),

    (""" { Users {"name" : "Emme"}""", Seq(
      Field("greedy.0.name", TextValue("Emme"))
    )),

    (""" } Users {"name" : "Emme"}""", Seq(
      Field("greedy.0.name", TextValue("Emme"))
    )),

    ("""Users {"name": {"surname": "Elmar", { "lastname": "Schug"}}, {"name" : "Manuel"} }{""", Seq(
      Field("greedy.0.lastname", TextValue("Schug")),
      Field("greedy.0.name", TextValue("Manuel"))
    )),
  )

  "A GreedyJsonExtractor" - {

    val configuration = Configuration(parameterSet = ParameterSet(Seq(
      FieldNameParameter(GreedyJsonExtractorLogic.fieldNameParameter.ref, "message"),
      TextParameter(GreedyJsonExtractorLogic.prefixParameter.ref, "greedy")
    )))

    samples.zipWithIndex.foreach { case ((json, expectedFields), index) =>
      s"should extract json from example ${index + 1}" in new TestStreamForFilter[GreedyJsonExtractorLogic](configuration) {
        whenReady(filterFuture) { _ =>
          val messageField = Field("message", TextValue(json))

          source.sendNext(Dataset(Record(messageField)))
          sink.request(1)

          val result = sink.requestNext().records.head
          result.fields should contain only((expectedFields :+ messageField):_*)
        }
      }
    }

    "should remove configured field" in new TestStreamForFilter[GreedyJsonExtractorLogic]() {
      whenReady(filterFuture) { filterProxy =>
        val newConfiguration = configuration.update(_.parameterSet.parameters :+= BooleanParameter(GreedyJsonExtractorLogic.removeFieldParameter.ref, true))
        whenReady(filterProxy.configure(newConfiguration)) { _ =>
          source.sendNext(Dataset(Record(Field("message", TextValue(samples.head._1)))))

          sink.request(1)
          val result = sink.requestNext().records.head
          result.fields should contain only (samples.head._2:_*)
        }
      }
    }
  }
}