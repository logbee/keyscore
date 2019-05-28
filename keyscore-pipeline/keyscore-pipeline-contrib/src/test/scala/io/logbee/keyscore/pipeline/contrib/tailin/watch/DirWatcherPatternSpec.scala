package io.logbee.keyscore.pipeline.contrib.tailin.watch

import java.nio.file.{Files, Path}

import io.logbee.keyscore.pipeline.contrib.tailin.util.TestUtil
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfterAll, FreeSpec, Matchers}
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DirWatcherPatternSpec extends FreeSpec with Matchers with BeforeAndAfterAll {
  
  var tmpDir: Path = null
  
  override def afterAll() = {
    if (tmpDir != null) {
      TestUtil.recursivelyDelete(tmpDir)
    }
  }
  
  
  "A DirWatcherPattern" - {
    
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
        TestSetup(filePattern        = "test/tailin.csv",
                  expectedFixedPath  = "test/",
                  expectedVariableIndex = -1,
                  expectedSubDirPath = "tailin.csv"),
        
        TestSetup(filePattern        = "**/tailin.csv",
                  expectedFixedPath  = "",
                  expectedVariableIndex = 0,
                  expectedSubDirPath = "**/tailin.csv"),
        
        TestSetup(filePattern        = "test/**/tailin.csv",
                  expectedFixedPath  = "test/",
                  expectedVariableIndex = 5,
                  expectedSubDirPath = "**/tailin.csv"),
                  
        TestSetup(filePattern        = "test/**/foo/**/tailin.csv",
                  expectedFixedPath  = "test/",
                  expectedVariableIndex = 5,
                  expectedSubDirPath = "**/foo/**/tailin.csv"),
                  
        TestSetup(filePattern        = "test/*/tailin.csv",
                  expectedFixedPath  = "test/",
                  expectedVariableIndex = 5,
                  expectedSubDirPath = "*/tailin.csv"),
                  
        TestSetup(filePattern        = "test/*tailin.csv",
                  expectedFixedPath  = "test/",
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
      )
    
    
    
    "should get the fixed directory's path" - {
      
      testSetups.foreach { testSetup =>
        
        s"${testSetup.filePattern} has fixed parent-dir: ${testSetup.expectedFixedPath}" in
        {
          val result = DirWatcherPattern.extractInvariableDir(tmpDir + "/" + testSetup.filePattern)
          
          result shouldBe Some(tmpDir + "/" + testSetup.expectedFixedPath)
        }
      }
    }
    
    
    "should find the first index containing" - {
      
      testSetups.foreach { testSetup =>
        
        s"${testSetup.filePattern} has variable symbol at position ${testSetup.expectedVariableIndex}" in
        {
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
    
    
    
    "should correctly remove the first part of a filePattern, transforming" - {
      
      testSetups.foreach { testSetup =>
        
        s"${testSetup.filePattern} into ${testSetup.expectedSubDirPath}" in
        {
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
    
    
    "transform an SMB-path into a Unix-like path" in
    {
      val fullFilePattern = "\\\\some.host.name\\share\\file\\path"
      val result = DirWatcherPattern.getUnixLikePath(fullFilePattern)
      
      result shouldBe "/some.host.name/share/file/path"
    }
  }
}
