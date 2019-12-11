package io.logbee.keyscore.pipeline.contrib.tailin

import java.io.File
import java.nio.charset.{Charset, StandardCharsets}
import java.util.UUID

import akka.stream.SourceShape
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.TestSink
import akka.testkit.TestKit
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.share.DiskShare
import io.logbee.keyscore.model.configuration.{BooleanParameter, ChoiceParameter, Configuration, GroupParameter, ParameterSet, TextParameter}
import io.logbee.keyscore.model.data.{Dataset, TextValue}
import io.logbee.keyscore.model.pipeline.StageSupervisor
import io.logbee.keyscore.pipeline.api.LogicParameters
import io.logbee.keyscore.pipeline.api.stage.{SourceStage, StageContext}
import io.logbee.keyscore.pipeline.contrib.tailin.read.ReadMode
import io.logbee.keyscore.pipeline.contrib.tailin.util.Manual_SpecWithSmbShare
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.duration._

/*
  * TODO Does not work yet. Potentially replace this with a Citrus test or similar.
  */
class Manual_SmbFileSourceLogicSpec extends Manual_SpecWithSmbShare with Matchers with BeforeAndAfter with BeforeAndAfterAll with ScalaFutures with TestSystemWithMaterializerAndExecutionContext with ParallelTestExecution {
  
  
  val connection = client.connect(hostName)
  
  val authContext = new AuthenticationContext(userName, password.toCharArray, domain)
  val session = connection.authenticate(authContext)
  
  implicit val share = session.connectShare(shareName).asInstanceOf[DiskShare]
  
  val superDir = "testDir\\"
  val watchDir = "testDir\\test"
  
  override def beforeAll = {
    createDir(superDir)
    createDir(watchDir)
  }
  override def afterAll = {
    share.rm(watchDir)
    share.rm(superDir)
    TestKit.shutdownActorSystem(system)
    
    if (share != null)
      share.close()
    
    if (connection != null)
      connection.close()
  }
  
  val persistenceFile = new File(".testKeyscorePersistenceFile")
  after {
    persistenceFile.delete()
  }
  

  "An SmbFileSource" - {
    
    case class FileWithContent(path: String, lines: Seq[String])

    case class TestSetup(
      files: Seq[FileWithContent],
      filePattern: String,
      readMode: ReadMode,
      encoding: Charset = StandardCharsets.UTF_8,
      rotationPattern: String = "",
      expectedData: Seq[String],
    )
    
    val testSetup = TestSetup( //line-wise
        files = Seq(FileWithContent(path="file.csv", lines=Seq("abcde", "fghij", "klmno"))),
        filePattern = "file.csv",
        readMode = ReadMode.Line,
        expectedData = Seq("abcde", "fghij", "klmno"),
    )


    s"""with
      filePattern:     "${testSetup.filePattern}"
      readMode:        "${testSetup.readMode}"
      encoding:        "${testSetup.encoding}"
      rotationPattern: "${testSetup.rotationPattern}"
    """ - {

      trait DefaultSmbFileSourceValues {

        val context = StageContext(system, executionContext)

        val configuration = Configuration(
          TextParameter(   SmbFileSourceLogic.hostName.ref,        hostName),
          TextParameter(   SmbFileSourceLogic.shareName.ref,       shareName),
          TextParameter(   SmbFileSourceLogic.domainName.ref,      domain),
          BooleanParameter(SmbFileSourceLogic.enableAuth.ref,      true),
          GroupParameter(  SmbFileSourceLogic.authGroup.ref,       Some(ParameterSet(Seq(
            TextParameter(   SmbFileSourceLogic.loginName.ref,       userName),
            TextParameter(   SmbFileSourceLogic.password.ref,        password),
          )))),
          TextParameter(  LocalFileSourceLogic.filePattern.ref,     s"$watchDir\\${testSetup.filePattern}"),
          ChoiceParameter(LocalFileSourceLogic.readMode.ref,        testSetup.readMode.toString),
          ChoiceParameter(LocalFileSourceLogic.encoding.ref,        testSetup.encoding.toString),
          TextParameter(  LocalFileSourceLogic.rotationPattern.ref, testSetup.rotationPattern),
          TextParameter(  LocalFileSourceLogic.fieldName.ref,       "output"),
        )

        val provider = (parameters: LogicParameters, shape: SourceShape[Dataset]) => new SmbFileSourceLogic(LogicParameters(UUID.randomUUID, StageSupervisor.noop, context, configuration), shape)
        val sourceStage = new SourceStage(LogicParameters(UUID.randomUUID, StageSupervisor.noop, context, configuration), provider)
        val (sourceFuture, sink) = Source.fromGraph(sourceStage).toMat(TestSink.probe[Dataset])(Keep.both).run()
      }

      "should push one available string for one available pull" in new DefaultSmbFileSourceValues {
        
        withSmbFile(watchDir + testSetup.files.head.path, testSetup.encoding.encode(testSetup.files.head.lines.head), { _ =>
          sink.request(1)
          val result = sink.expectNext(5.seconds)
          result.records.head.fields should have size 1
          result.records.head.fields.head.value shouldEqual TextValue(testSetup.expectedData.head)
        })
      }
    }
  }
}
