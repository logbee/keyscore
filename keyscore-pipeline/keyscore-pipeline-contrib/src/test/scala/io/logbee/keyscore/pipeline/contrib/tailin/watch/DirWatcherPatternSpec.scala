package io.logbee.keyscore.pipeline.contrib.tailin.watch

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.junit.runner.RunWith
import org.scalatest.BeforeAndAfterAll
import org.scalatest.FreeSpec
import org.scalatest.Matchers

import io.logbee.keyscore.pipeline.contrib.tailin.util.TestUtil
import org.scalatest.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class DirWatcherPatternSpec extends FreeSpec with Matchers with BeforeAndAfterAll {
  
  var tmpDir: Path = null
  
  override def afterAll() = {
    if (tmpDir != null) {
      TestUtil.recursivelyDelete(tmpDir)
    }
  }
  
  
  "A DirWatcherPattern's" - {
    
    tmpDir = Files.createTempDirectory("extractInvariableDir") //put everything into a temp-directory to be OS-agnostic and be able to create files and clean them up without problem
    TestUtil.waitForFileToExist(tmpDir.toFile)
    
    val testDir = tmpDir.resolve("test")
    Files.createDirectory(testDir)
    TestUtil.waitForFileToExist(testDir.toFile)
    
    case class TestSetup(filePattern: String,
                         expectedFixedPath: String,
                         expectedVariableIndex: Int,
                         expectedSubDirPath: String)
    
    val testSetups = Seq(
        TestSetup(filePattern        = "**/tailin.csv",
                  expectedFixedPath  = "",
                  expectedVariableIndex = 0,
                  expectedSubDirPath = "**/tailin.csv"),
        
        TestSetup(filePattern        = "test/**/tailin.csv",
                  expectedFixedPath  = "test",
                  expectedVariableIndex = 5,
                  expectedSubDirPath = "**/tailin.csv"),
                  
        TestSetup(filePattern        = "test/**/foo/**/tailin.csv",
                  expectedFixedPath  = "test",
                  expectedVariableIndex = 5,
                  expectedSubDirPath = "**/foo/**/tailin.csv"),
                  
        TestSetup(filePattern        = "test/*/tailin.csv",
                  expectedFixedPath  = "test",
                  expectedVariableIndex = 5,
                  expectedSubDirPath = "*/tailin.csv"),
                  
        TestSetup(filePattern        = "test/*tailin.csv",
                  expectedFixedPath  = "test",
                  expectedVariableIndex = 5,
                  expectedSubDirPath = "*tailin.csv"),
                  
//        TestSetup(filePattern        = "test**/tailin.csv", //TODO do we support this? What should it do?
//                  expectedFixedPath  = "",
//                  expectedVariableIndex = 4,
//                  expectedSubDirPath = "**/tailin.csv"),
//                  
//        TestSetup(filePattern        = "test/**tailin.csv", //TODO do we support this? What should it do?
//                  expectedFixedPath  = "",
//                  expectedVariableIndex = 5,
//                  expectedSubDirPath = "**tailin.csv"),
                  
        TestSetup(filePattern        = "test*/tailin.csv",
                  expectedFixedPath  = "",
                  expectedVariableIndex = 4,
                  expectedSubDirPath = "tailin.csv"),
                  
        TestSetup(filePattern        = "test?/tailin.csv",
                  expectedFixedPath  = "",
                  expectedVariableIndex = 4,
                  expectedSubDirPath = "tailin.csv"),
                  
        TestSetup(filePattern        = "tes*/tailin.csv",
                  expectedFixedPath  = "",
                  expectedVariableIndex = 3,
                  expectedSubDirPath = "tailin.csv"),
                  
        TestSetup(filePattern        = "tes[a,t]/tailin.csv",
                  expectedFixedPath  = "",
                  expectedVariableIndex = 3,
                  expectedSubDirPath = "tailin.csv"),
                  
        TestSetup(filePattern        = "tes[a-zA-Z]/tailin.csv",
                  expectedFixedPath  = "",
                  expectedVariableIndex = 3,
                  expectedSubDirPath = "tailin.csv"),
                  
        TestSetup(filePattern        = "te*t/tailin.csv",
                  expectedFixedPath  = "",
                  expectedVariableIndex = 2,
                  expectedSubDirPath = "tailin.csv"),
                  
        TestSetup(filePattern        = "*est/tailin.csv",
                  expectedFixedPath  = "",
                  expectedVariableIndex = 0,
                  expectedSubDirPath = "tailin.csv"),
                  
        TestSetup(filePattern        = "nonexistent-dir/**/tailin.csv",
                  expectedFixedPath  = null,
                  expectedVariableIndex = 16,
                  expectedSubDirPath = "**/tailin.csv"), //TODO it might not exist yet, but the user will probably want it monitored when it starts to exist later
      )
    
    
    
    "extractInvariableDir() should get the fixed directory's path" - {
      
      testSetups.foreach { testSetup =>
        
        s"${testSetup.filePattern} has fixed parent-dir: ${testSetup.expectedFixedPath}" in {
          val result = DirWatcherPattern.extractInvariableDir(tmpDir + "/" + testSetup.filePattern)
          
          result shouldBe (
                            if (testSetup.expectedFixedPath == null)
                              null
                            else
                              Paths.get(tmpDir + "/" + testSetup.expectedFixedPath)
                          )
        }
      }
    }
    
    
    "findFirstVariableIndex() should find the first index containing" - {
      
      testSetups.foreach { testSetup =>
        
        s"${testSetup.filePattern} has variable symbol at position ${testSetup.expectedVariableIndex}" in {
          val result = DirWatcherPattern.findFirstVariableIndex(testSetup.filePattern)
          
          result shouldBe (
                            if (testSetup.expectedVariableIndex == -1)
                              -1
                            else
                              testSetup.expectedVariableIndex
                          )
        }
      }
    }
    
    
    
    "removeFirstDirPrefixFromMatchPattern() should correctly remove the first part of a filePattern, transforming" - {
      
      testSetups.foreach { testSetup =>
        
        s"${testSetup.filePattern} into ${testSetup.expectedSubDirPath}" in {
          val result = DirWatcherPattern.removeFirstDirPrefixFromMatchPattern(testSetup.filePattern)
          
          result shouldBe (
                            if (testSetup.expectedSubDirPath == null)
                              null
                            else
                              testSetup.expectedSubDirPath
                          )
        }
      }
    }
  }
}