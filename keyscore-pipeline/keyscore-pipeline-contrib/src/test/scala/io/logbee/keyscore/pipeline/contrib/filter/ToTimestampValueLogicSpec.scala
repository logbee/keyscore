package io.logbee.keyscore.pipeline.contrib.filter

import io.logbee.keyscore.model.configuration.{ChoiceParameter, Configuration, FieldNameParameter, ParameterSet, TextParameter}
import io.logbee.keyscore.model.data.{Dataset, DecimalValue, Field, Record, TextValue, TimestampValue}
import io.logbee.keyscore.pipeline.testkit.{TestActorSystem, TestStreamForFilter}
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FreeSpec, Inside, Matchers}
import org.scalatestplus.junit.JUnitRunner
import io.logbee.keyscore.pipeline.contrib.filter.ToTimestampValueLogic.{formatParameter, sourceFieldNameParameter, sourceTimeZoneParameter}

@RunWith(classOf[JUnitRunner])
class ToTimestampValueLogicSpec extends FreeSpec with Matchers with Inside with ScalaFutures with TestActorSystem {
  
  val configuration1 = Configuration(parameterSet = ParameterSet(Seq(
    FieldNameParameter(sourceFieldNameParameter.ref, "input"),
    TextParameter(formatParameter.ref, "yyyy.MM.dd HH:mm:ss.SSS"),
  )))
  
  val configuration2 = Configuration(parameterSet = ParameterSet(Seq(
    FieldNameParameter(sourceFieldNameParameter.ref, "input"),
    TextParameter(formatParameter.ref, "yyyy.MM.dd HH:mm:ss.SSS"),
    ChoiceParameter(sourceTimeZoneParameter.ref, "GMT+1"),
  )))
  
  val sampleDataset = Dataset(Record(
      Field("input", TextValue("2019.08.21 14:32:53.123")),
      Field("bar", TextValue("Hello World!"))
    ))

  val expectedDataset1 = Dataset(Record(
    Field("input", TimestampValue(1566397973, 123000000)),
    Field("bar", TextValue("Hello World!"))
  ))
  
  val expectedDataset2 = Dataset(Record(
    Field("input", TimestampValue(1566394373, 123000000)),
    Field("bar", TextValue("Hello World!"))
  ))
  
  
  val nonMatchingDataset = Dataset(
    Record(
      Field("foo", DecimalValue(42.0)),
      Field("bar", TextValue("Hello World!"))
    ),
    Record(
      Field("foo", DecimalValue(5.3)),
      Field("bar", TextValue("Bye Bye!"))
    )
  )
  
  
  
  
  "A ToTimestampValueLogic" - {
    "should passthrough datasets which do not have the specified source field" in new TestStreamForFilter[ToTimestampValueLogic](configuration1) {
      whenReady(filterFuture) { _ =>
        source.sendNext(nonMatchingDataset)
        
        sink.request(1)
        val result = sink.requestNext()
        result.records shouldBe nonMatchingDataset.records
      }
    }
    
    "should parse a date" in new TestStreamForFilter[ToTimestampValueLogic](configuration1) {
      whenReady(filterFuture) { _ =>
        source.sendNext(sampleDataset)
        
        sink.request(1)
        
        val result = sink.requestNext()
        result shouldBe expectedDataset1
      }
    }
    
    "should convert a date to UTC" in new TestStreamForFilter[ToTimestampValueLogic](configuration2) {
      whenReady(filterFuture) { _ =>
        source.sendNext(sampleDataset)
        
        sink.request(1)
        
        val result = sink.requestNext()
        result shouldBe expectedDataset2
      }
    }
  }
}
