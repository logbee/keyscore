package io.logbee.keyscore.pipeline.contrib.tailin.file

import org.scalatest.FreeSpec
import org.scalatest.Matchers
import java.nio.file.Path
import org.scalatest._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.nio.file.Paths
import java.nio.file.Files
import io.logbee.keyscore.pipeline.contrib.tailin.util.TestUtil


@RunWith(classOf[JUnitRunner])
class DirWatcherPatternSpec extends FreeSpec with Matchers with BeforeAndAfterAll {
  
  var tmpDir: Path = null
  
  override def afterAll() = {
    if (tmpDir != null) {
      TestUtil.recursivelyDelete(tmpDir)
    }
  }
  
  
  "extractInvariableDir() should get the fixed directory's path" - {
    
    tmpDir = Files.createTempDirectory("extractInvariableDir") //put everything into a temp-directory to be OS-agnostic and be able to create files and clean them up without problem
    TestUtil.waitForFileToExist(tmpDir.toFile)
    
    val testDir = tmpDir.resolve("test")
    Files.createDirectory(testDir)
    TestUtil.waitForFileToExist(testDir.toFile)
    
    case class TestSetup(filePattern: String, expectedFixedPath: String)
    
    val testSetups = Seq(
      TestSetup("/test/**/tailin.csv",
                "/test"),
                
      TestSetup("/test/**/foo/**/tailin.csv",
                "/test"),
                
      TestSetup("/test/*/tailin.csv",
                "/test"),
                
      TestSetup("/test/*tailin.csv",
                "/test"),
                
      TestSetup("/test**/tailin.csv",
                "/"),
                
      TestSetup("/test*/tailin.csv",
                "/"),
                
      TestSetup("/test?/tailin.csv",
                "/"),
                
      TestSetup("/tes*/tailin.csv",
                "/"),
                
      TestSetup("/tes[a,t]/tailin.csv",
                "/"),
                
      TestSetup("/tes[a-zA-Z]/tailin.csv",
                "/"),
                
      TestSetup("/te*t/tailin.csv",
                "/"),
                
      TestSetup("/*est/tailin.csv",
                "/"),
                
      TestSetup("/nonexistent-dir/**/tailin.csv",
                null), //TODO it might not exist yet, but the user will probably want it monitored when it starts to exist later
    )
    
    testSetups.foreach { testSetup =>
      
      s"${testSetup.filePattern} has fixed parent-dir: ${testSetup.expectedFixedPath}" in {
        val result = DirWatcherPattern.extractInvariableDir(tmpDir + testSetup.filePattern)
        
        result shouldBe (
                          if (testSetup.expectedFixedPath == null)
                            null
                          else
                            Paths.get(tmpDir + testSetup.expectedFixedPath)
                        )
      }
    }
  }
}