package io.logbee.keyscore.pipeline.contrib.tailin

import java.io.File
import java.nio.charset.{Charset, StandardCharsets}
import java.nio.file.{Files, Path, StandardOpenOption}
import java.util.UUID

import akka.stream.SourceShape
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.TestSink
import akka.testkit.TestKit
import io.logbee.keyscore.model.configuration.{ChoiceParameter, Configuration, FieldNameParameter, TextParameter}
import io.logbee.keyscore.model.data.{Dataset, TextValue}
import io.logbee.keyscore.model.pipeline.StageSupervisor
import io.logbee.keyscore.pipeline.api.LogicParameters
import io.logbee.keyscore.pipeline.api.stage.{SourceStage, StageContext}
import io.logbee.keyscore.pipeline.contrib.tailin.file.local.LocalFile.openLocalFile2File
import io.logbee.keyscore.pipeline.contrib.tailin.read.ReadMode
import io.logbee.keyscore.pipeline.contrib.tailin.util.TestUtil
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.duration.DurationInt

@RunWith(classOf[JUnitRunner])
class LocalFileSourceLogicSpec extends AnyFreeSpec with Matchers with BeforeAndAfter with BeforeAndAfterAll with ScalaFutures with TestSystemWithMaterializerAndExecutionContext {

  implicit val defaultPatience = PatienceConfig(timeout = Span(2, Seconds), interval = Span(500, Millis))
  val expectNextTimeout = 10.seconds

  val persistenceFile = new File(".testKeyscorePersistenceFile")



  var watchDir: Path = null

  before {
    watchDir = Files.createTempDirectory("watchTest")

    TestUtil.waitForFileToExist(watchDir.toFile)
  }

  after {
    TestUtil.recursivelyDelete(watchDir)
    persistenceFile.delete()
  }


  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }


  "A LocalFileSource" - {
    
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
          files = Seq(FileWithContent(path="file.csv", lines=Seq("abcde", "fghij", "klmno"))),
          filePattern = "file.csv",
          readMode = ReadMode.Line,
          expectedData = Seq("abcde", "fghij", "klmno"),
      ),
      TestSetup( //file-wise
          files = Seq(FileWithContent(path="file.csv", lines=Seq("abcde\n", "fghij\n", "klmno\n"))),
          filePattern = "file.csv",
          readMode = ReadMode.File,
          expectedData = Seq("abcde\n", "fghij\n", "klmno\n"),
      ),

      //test UTF-16 Little Endian and Big Endian separately, as just "UTF_16" causes the BufferedWriter in the test to write a Byte Order Mark (BOM) before each string that gets appended to the file (therefore failing tests where a file is written to multiple times)
      TestSetup(
          files = Seq(FileWithContent(path="file.csv", lines=Seq("abcde", "fghij", "klmnö"))),
          filePattern = "file.csv",
          readMode = ReadMode.Line,
          encoding = StandardCharsets.UTF_16LE, /*16BE*/
          expectedData = Seq("abcde", "fghij", "klmnö"),
      ),
    )

    testSetups.foreach { testSetup =>
      val rotationPatternDescription = if (testSetup.rotationPattern.isEmpty) "" else s"""rotationPattern: "${testSetup.rotationPattern}""""

      s"""with
        filePattern:     "${testSetup.filePattern}"
        readMode:        "${testSetup.readMode}"
        encoding:        "${testSetup.encoding}"
        $rotationPatternDescription
      """ - {

        trait DefaultLocalFileSourceValues {

          val context = StageContext(system, executionContext)

          implicit val charset = testSetup.encoding

          val configuration = Configuration(
            TextParameter(  FileSourceLogicBase.filePattern.ref,     s"$watchDir/${testSetup.filePattern}"),
            ChoiceParameter(FileSourceLogicBase.readMode.ref,        testSetup.readMode.toString),
            ChoiceParameter(FileSourceLogicBase.encoding.ref,        testSetup.encoding.toString),
            TextParameter(  FileSourceLogicBase.rotationPattern.ref, testSetup.rotationPattern),
            FieldNameParameter(  FileSourceLogicBase.fieldName.ref,  "output"),
          )

          val provider = (parameters: LogicParameters, shape: SourceShape[Dataset]) => new LocalFileSourceLogic(LogicParameters(UUID.randomUUID, StageSupervisor.noop, context, configuration), shape)
          val sourceStage = new SourceStage(LogicParameters(UUID.randomUUID, StageSupervisor.noop, context, configuration), provider)
          val (sourceFuture, sink) = Source.fromGraph(sourceStage).toMat(TestSink.probe[Dataset])(Keep.both).run()
        }

        "should push one available string for one available pull" in new DefaultLocalFileSourceValues {
          whenReady(sourceFuture) { _ =>
            TestUtil.withOpenLocalFile(watchDir, testSetup.files.head.path, testSetup.files.head.lines.head) { file =>

              sink.request(1)
              val result = sink.expectNext(expectNextTimeout)
              result.records.head.fields should have size 3
              result.records.head.fields.head.value shouldEqual TextValue(testSetup.expectedData.head)
            }
          }
        }

        "should push multiple available strings" - {

          Seq(true, false).foreach { waitFor_DirWatcher_processEvents =>
            (if (waitFor_DirWatcher_processEvents == true)
               "when it has to wait for pulls (buffering)"
             else
               "when pulls are made as it reads the data"
            ) in new DefaultLocalFileSourceValues {
              whenReady(sourceFuture) { _ =>
                TestUtil.withOpenLocalFile(watchDir, "file.csv", "") { file =>

                  val texts = testSetup.files.head.lines

                  texts.foreach { text =>
                    TestUtil.writeStringToFile(file, text, StandardOpenOption.APPEND)

                    if (waitFor_DirWatcher_processEvents) {
                      Thread.sleep(1500)
                    }
                  }

                  val concatenatedExpectedData = testSetup.expectedData.fold("")((string1, string2) => string1 + string2)

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
          }
        }

        "should push multiple strings that become available in a delayed manner for multiple delayed pulls" in
        new DefaultLocalFileSourceValues {
          whenReady(sourceFuture) { _ =>
            TestUtil.withOpenLocalFile(watchDir, "file.csv", "") { file =>

              val texts = testSetup.files.head.lines

              texts.zip(testSetup.expectedData).foreach { case (text, expectedText) =>
                TestUtil.writeStringToFile(file, text, StandardOpenOption.APPEND)

                sink.request(1)

                Thread.sleep(1500) //wait for processEvents to trigger once

                val datasetText = sink.expectNext(expectNextTimeout)

                datasetText.records.head.fields.head.value shouldEqual TextValue(expectedText)
              }
            }
          }
        }

        "should wait for strings to become available, if no strings are available when it gets pulled" in
        new DefaultLocalFileSourceValues {
          whenReady(sourceFuture) { _ =>
            TestUtil.withOpenLocalFile(watchDir, "file.csv", "") { file =>

              sink.request(1)

              Thread.sleep(3000)

              val text = testSetup.files.head.lines.head
              TestUtil.writeStringToFile(file, text)

              val datasetText = sink.expectNext(expectNextTimeout)

              datasetText.records.head.fields.head.value shouldEqual TextValue(testSetup.expectedData.head)
            }
          }
        }
      }
    }








    trait DefaultSource {
      val context = StageContext(system, executionContext)

      implicit val charset = StandardCharsets.UTF_8

      val configuration = Configuration(
        TextParameter(  FileSourceLogicBase.filePattern.ref,     s"$watchDir/file.csv"),
        ChoiceParameter(FileSourceLogicBase.readMode.ref,        ReadMode.Line.toString),
        ChoiceParameter(FileSourceLogicBase.encoding.ref,        charset.toString),
        TextParameter(  FileSourceLogicBase.rotationPattern.ref, "file.csv.[1-5]"),
        FieldNameParameter(  FileSourceLogicBase.fieldName.ref,  "output"),
      )

      val provider = (parameters: LogicParameters, shape: SourceShape[Dataset]) => new LocalFileSourceLogic(LogicParameters(UUID.randomUUID, StageSupervisor.noop, context, configuration), shape)
      val sourceStage = new SourceStage(LogicParameters(UUID.randomUUID, StageSupervisor.noop, context, configuration), provider)
      val (sourceFuture, sink) = Source.fromGraph(sourceStage).toMat(TestSink.probe[Dataset])(Keep.both).run()
    }



    "should push multiple logfiles with the same lastModified-timestamp in the correct order" in
    new DefaultSource {
      whenReady(sourceFuture) { _ =>
        val sharedLastModified = 1234567890
        TestUtil.withOpenLocalFile(watchDir, "file.csv", "0") { baseFile =>
          baseFile.setLastModified(sharedLastModified)
          TestUtil.withOpenLocalFile(watchDir, "file.csv.1", "11") { file1 =>
            file1.setLastModified(sharedLastModified)
            TestUtil.withOpenLocalFile(watchDir, "file.csv.2", "222") { file2 =>
              file2.setLastModified(sharedLastModified)

              sink.request(1)

              sink.expectNoMessage(5.seconds)

              TestUtil.writeStringToFile(baseFile, "0", StandardOpenOption.APPEND) //this should trigger things to be read out, as we don't read out files which share their lastModified-timestamp with the baseFile

              sink.request(1)

              val datasetText = sink.expectNext(expectNextTimeout)

              datasetText.records.head.fields.head.value shouldEqual TextValue("222")
            }
          }
        }
      }
    }

    "should push realistic log data with rotation" ignore //TODO doesn't work yet. Probably some problem in the way ReadScheduler deals with rotated files.
    new DefaultSource {
      whenReady(sourceFuture) { _ =>
        TestUtil.withOpenLocalFile(watchDir, "file.csv") { logFile =>
          val numberOfLines = 30
          val slf4j_rotatePattern = logFile.name + ".%i"

          TestUtil.writeLogToFileWithRotation(logFile, numberOfLines, slf4j_rotatePattern)

          Thread.sleep(1000) //wait one second and write something to the base-file, so that it doesn't share its lastModified with other files anymore (and we can therefore enumerate those other files to differentiate them for reading out - newerFilesWithSharedLastModified)
          TestUtil.writeStringToFile(logFile, "Hello", StandardOpenOption.APPEND)

          var concatenatedString = ""
          for (i <- 1 to numberOfLines) {
            sink.request(1)

            val datasetText = sink.expectNext(expectNextTimeout)

            concatenatedString += datasetText.records.head.fields.head.value.asInstanceOf[TextValue].value + "\n"
          }

          concatenatedString.linesIterator.length shouldEqual numberOfLines
        }
      }
    }
  }
}
