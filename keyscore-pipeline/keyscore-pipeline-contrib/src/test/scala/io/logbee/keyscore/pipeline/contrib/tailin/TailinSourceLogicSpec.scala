package io.logbee.keyscore.pipeline.contrib.tailin

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, StandardOpenOption}
import java.util.UUID

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
import io.logbee.keyscore.pipeline.contrib.tailin.file.ReadMode._
import java.nio.charset.Charset
import io.logbee.keyscore.model.conversion.TextValueConversion

@RunWith(classOf[JUnitRunner])
class TailinSourceLogicSpec extends FreeSpec with Matchers with BeforeAndAfter with BeforeAndAfterAll with ScalaFutures with TestSystemWithMaterializerAndExecutionContext {
  
  var watchDir: Path = _
  
  before {
    watchDir = Files.createTempDirectory("watchTest")
    TestUtil.waitForFileToExist(watchDir.toFile)
  }
  after {
    TestUtil.recursivelyDelete(watchDir)
  }
  
  override def afterAll = {
    TestKit.shutdownActorSystem(system)
  }
  
  
  
  "A TailinSource" - {
    
    case class FileWithContent(path: String, lines: Seq[String])

    case class TestSetup(
        files: Seq[FileWithContent],
        filePattern: String,
        readMode: ReadMode,
        encoding: Charset = StandardCharsets.UTF_8,
        rotationPattern: String = "",
        expectedData: Seq[String],
    )
    
    val testSetups = Seq(
        
      TestSetup( //line-wise
          files = Seq(FileWithContent(path="tailin.csv", lines=Seq("abcde", "fghij", "klmno"))),
          filePattern = "tailin.csv",
          readMode = ReadMode.LINE,
          expectedData = Seq("abcde", "fghij", "klmno"),
      ),
      TestSetup( //file-wise
          files = Seq(FileWithContent(path="tailin.csv", lines=Seq("abcde", "fghij", "klmno"))),
          filePattern = "tailin.csv",
          readMode = ReadMode.FILE,
          expectedData = Seq("abcde\n", "fghij\n", "klmno\n"),
      ),
      
      
      //test UTF-16 Little Endian and Big Endian separately, as just "UTF_16" causes the BufferedWriter in the test to write a Byte Order Mark (BOM) before each string that gets appended to the file (therefore failing tests where a file is written to multiple times)
      TestSetup(
          files = Seq(FileWithContent(path="tailin.csv", lines=Seq("abcde", "fghij", "klmnö"))),
          filePattern = "tailin.csv",
          readMode = ReadMode.LINE,
          encoding = StandardCharsets.UTF_16LE,
          expectedData = Seq("abcde", "fghij", "klmnö"),
      ),
      TestSetup(
          files = Seq(FileWithContent(path="tailin.csv", lines=Seq("abcde", "fghij", "klmnö"))),
          filePattern = "tailin.csv",
          readMode = ReadMode.LINE,
          encoding = StandardCharsets.UTF_16BE,
          expectedData = Seq("abcde", "fghij", "klmnö"),
      ),
    )
    
    
    
    testSetups.foreach { testSetup =>
      
      s"""with
        filePattern:     "${testSetup.filePattern}"
        readMode:        "${testSetup.readMode}"
        encoding:        "${testSetup.encoding}"
        rotationPattern: "${testSetup.rotationPattern}"
      """ - {
        
        
        trait DefaultTailinSourceValues {
          
          val context = StageContext(system, executionContext)
          
          val configuration = Configuration(
            TextParameter(  TailinSourceLogic.filePattern.ref,     watchDir + "/" + testSetup.filePattern),
            ChoiceParameter(TailinSourceLogic.readMode.ref,        testSetup.readMode.toString),
            ChoiceParameter(TailinSourceLogic.encoding.ref,        testSetup.encoding.toString),
            TextParameter(  TailinSourceLogic.rotationPattern.ref,  testSetup.rotationPattern),
            TextParameter(  TailinSourceLogic.fieldName.ref,       "output"),
          )
          
          val provider = (parameters: LogicParameters, shape: SourceShape[Dataset]) => new TailinSourceLogic(LogicParameters(UUID.randomUUID, context, configuration), shape)
          
          val sourceStage = new SourceStage(LogicParameters(UUID.randomUUID, context, configuration), provider)
          
          val (sourceFuture, sink) = Source.fromGraph(sourceStage).toMat(TestSink.probe[Dataset])(Keep.both).run()
        }
        
        
        
        "should push one available string for one available pull" in new DefaultTailinSourceValues {
          
          val file = TestUtil.createFile(watchDir, testSetup.files.head.path)
          
          val text = testSetup.files.head.lines.head
          TestUtil.writeStringToFile(file, text + "\n", encoding=testSetup.encoding)
          
          sink.request(1)
          var result = sink.expectNext()
          result.records.head.fields should have size 1
          result.records.head.fields.head.value shouldEqual TextValue(testSetup.expectedData.head)
        }
        
        
        "should push multiple available strings" - {
          
          Seq(false).foreach { waitFor_DirWatcher_processEvents =>
            (if (waitFor_DirWatcher_processEvents == true)
               "when it has to wait for pulls (buffering)"
             else
               "when pulls are made as it reads the data"
            ) in new DefaultTailinSourceValues {
              
              val file = TestUtil.createFile(watchDir, "tailin.csv")
              
              val texts = testSetup.files.head.lines
              
              texts.foreach { text =>
                TestUtil.writeStringToFile(file, text + "\n", encoding=testSetup.encoding)
                
                if (waitFor_DirWatcher_processEvents) {
                  Thread.sleep(1500)
                }
              }
              
              
              
              var concatenatedExpectedData = testSetup.expectedData.fold("")((string1, string2) => string1 + string2)
              
              sink.request(texts.size)
              
              var concatenatedReturnedData = ""
              
              for (i <- 1 to texts.size) {
                val datasets: Seq[Dataset] = sink.receiveWithin(max=3.seconds, messages=texts.size)

                datasets.foreach { dataset =>
                  concatenatedReturnedData += dataset.records.head.fields.head.value.asInstanceOf[TextValue].value
                }
              }
              
              concatenatedReturnedData shouldEqual concatenatedExpectedData
            }
          }
        }
        
        
        "should push multiple strings that become available in a delayed manner for multiple delayed pulls" in new DefaultTailinSourceValues {
          
          val file = TestUtil.createFile(watchDir, "tailin.csv")
          
          val texts = testSetup.files.head.lines
          
          texts.zip(testSetup.expectedData).foreach { case (text, expectedText) =>
            TestUtil.writeStringToFile(file, text + "\n", encoding=testSetup.encoding)
            
            sink.request(1)
            
            Thread.sleep(1500) //wait for processEvents to trigger once
            
            val datasetText = sink.expectNext()
            
            datasetText.records.head.fields.head.value shouldEqual TextValue(expectedText)
          }
        }
        
        
        "should wait for strings to become available if no strings are available when it gets pulled" in new DefaultTailinSourceValues {
          
          val file = TestUtil.createFile(watchDir, "tailin.csv")
          
          sink.request(1)
          
          Thread.sleep(3000)
          
          val text = testSetup.files.head.lines.head
          TestUtil.writeStringToFile(file, text + "\n", encoding=testSetup.encoding)
          
          val datasetText = sink.expectNext()
          
          datasetText.records.head.fields.head.value shouldEqual TextValue(testSetup.expectedData.head)
        }
      }
    }
  }
}
