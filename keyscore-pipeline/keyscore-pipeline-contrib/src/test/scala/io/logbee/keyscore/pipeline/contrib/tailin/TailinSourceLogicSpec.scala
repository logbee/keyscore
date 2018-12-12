package io.logbee.keyscore.pipeline.contrib.tailin

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, StandardOpenOption}
import java.util.UUID
import java.util.UUID.randomUUID

import akka.stream.SourceShape
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.TestSink
import akka.testkit.TestKit
import io.logbee.keyscore.model.configuration.{Configuration, TextParameter, ChoiceParameter}
import io.logbee.keyscore.model.data.{Dataset, TextValue}
import io.logbee.keyscore.pipeline.api.LogicParameters
import io.logbee.keyscore.pipeline.api.stage.{SourceStage, StageContext}
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FreeSpec, Matchers}

import scala.concurrent.duration._
import io.logbee.keyscore.pipeline.contrib.tailin.util.TestUtil
import io.logbee.keyscore.pipeline.contrib.tailin.file.ReadMode
import io.logbee.keyscore.model.configuration.NumberParameter

@RunWith(classOf[JUnitRunner])
class TailinSourceLogicSpec extends FreeSpec with Matchers with BeforeAndAfter with BeforeAndAfterAll with ScalaFutures with TestSystemWithMaterializerAndExecutionContext {

  var watchDir: Path = _
  var persistenceFile: File = _ //TODO actually pass this persistence file along in a parameter to be able to delete it during tests and maybe be easier to configure (though we probably don't actually want to expose this to the user

  before {
    watchDir = Files.createTempDirectory("watchTest")

    TestUtil.waitForFileToExist(watchDir.toFile)

    persistenceFile = new File(".keyscoreFileTailinPersistence_test")
    persistenceFile.createNewFile()
    TestUtil.waitForFileToExist(persistenceFile)
  }

  after {
    TestUtil.recursivelyDelete(watchDir)
    persistenceFile.delete()
  }

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  
  trait DefaultTailinSourceValues {
    val bufferSize = 1024

    val context = StageContext(system, executionContext)


    val configuration = Configuration(
      TextParameter(  TailinSourceLogic.directoryPath.ref,   watchDir.toString),
      TextParameter(  TailinSourceLogic.filePattern.ref,     "**.csv"),
      NumberParameter(TailinSourceLogic.recursionDepth.ref,  0),
      ChoiceParameter(TailinSourceLogic.readMode.ref,        ReadMode.LINE.toString),
      ChoiceParameter(TailinSourceLogic.encoding.ref,        StandardCharsets.UTF_8.toString),
      TextParameter(  TailinSourceLogic.rotationSuffix.ref,  ".[1-5]"),
    )

    val provider = (parameters: LogicParameters, shape: SourceShape[Dataset]) => new TailinSourceLogic(LogicParameters(randomUUID(), context, configuration), shape)

    val sourceStage = new SourceStage(LogicParameters(UUID.randomUUID(), context, configuration), provider)

    val (sourceFuture, sink) = Source.fromGraph(sourceStage).toMat(TestSink.probe[Dataset])(Keep.both).run()
  }
  

  "A TailinSource" - {
    "should push one available string for one available pull" in new DefaultTailinSourceValues {

      val text = "Hallo Welt"
      val file = TestUtil.createFile(watchDir, "tailin.csv", text)

      sink.request(1)
      var result = sink.expectNext(10.seconds)
      result.records.head.fields should have size 1
      result.records.head.fields.head.value shouldEqual TextValue("Hallo Welt")
    }

    "should push multiple available strings for multiple available pulls" in new DefaultTailinSourceValues {

      val text1 = "abcde"
      val text2 = "fghij"
      val text3 = "klmno"

      val file = TestUtil.createFile(watchDir, "tailin.csv", "")
      

      TestUtil.writeStringToFile(file, text1 + "\n", StandardOpenOption.APPEND)

      TestUtil.writeStringToFile(file, text2 + "\n", StandardOpenOption.APPEND)

      TestUtil.writeStringToFile(file, text3 + "\n", StandardOpenOption.APPEND)


      sink.request(3)
      val datasetText1 = sink.expectNext()
      val datasetText2 = sink.expectNext()
      val datasetText3 = sink.expectNext()

      datasetText1.records.head.fields.head.value shouldEqual TextValue(text1)
      datasetText2.records.head.fields.head.value shouldEqual TextValue(text2)
      datasetText3.records.head.fields.head.value shouldEqual TextValue(text3)
    }

    "should push multiple available strings for multiple delayed pulls" in new DefaultTailinSourceValues {

      val text1 = "abcde"
      val text2 = "fghij"
      val text3 = "klmno"

      val file = TestUtil.createFile(watchDir, "tailin.csv", "")
      

      TestUtil.writeStringToFile(file, text1 + "\n", StandardOpenOption.APPEND)

      sink.request(1)

      Thread.sleep(1500) //wait for processEvents to trigger once

      val datasetText1 = sink.expectNext()



      TestUtil.writeStringToFile(file, text2 + "\n", StandardOpenOption.APPEND)

      sink.request(1)

      Thread.sleep(1500) //wait for processEvents to trigger once

      val datasetText2 = sink.expectNext()



      TestUtil.writeStringToFile(file, text3 + "\n", StandardOpenOption.APPEND)

      sink.request(1)

      Thread.sleep(1500)

      val datasetText3 = sink.expectNext()



      datasetText1.records.head.fields.head.value shouldEqual TextValue(text1)
      datasetText2.records.head.fields.head.value shouldEqual TextValue(text2)
      datasetText3.records.head.fields.head.value shouldEqual TextValue(text3)

    }

    //    "should wait to push many available strings when it has to wait for pulls (buffering)" in new DefaultTailinSourceValues {
    //
    //    }
    //
    //    "should wait for strings to become available if no strings are available when it gets pulled" in new DefaultTailinSourceValues {
    //
    //    }
  }
}
