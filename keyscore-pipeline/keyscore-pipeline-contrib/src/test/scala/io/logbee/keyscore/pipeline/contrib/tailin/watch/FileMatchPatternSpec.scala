package io.logbee.keyscore.pipeline.contrib.tailin.watch

import java.nio.file.{Files, Path}

import io.logbee.keyscore.pipeline.contrib.tailin.util.TestUtil
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfterAll, FreeSpec, Matchers}
import org.scalatestplus.junit.JUnitRunner
import io.logbee.keyscore.pipeline.contrib.tailin.file.LocalFile
import java.nio.file.Paths
import io.logbee.keyscore.pipeline.contrib.tailin.file.FileHandle
import org.scalamock.scalatest.MockFactory
import io.logbee.keyscore.pipeline.contrib.tailin.file.DirHandle
import java.nio.file.FileSystems

@RunWith(classOf[JUnitRunner])
class FileMatchPatternSpec extends FreeSpec with Matchers with BeforeAndAfterAll with MockFactory {
  
  "A FileMatchPattern" - {
    
    case class TestSetup(filePattern: String,
                         expectedFixedPath: String,
                         expectedVariableIndex: Int,
                        )
    
    val testSetups = Seq(
        TestSetup(filePattern        = "/test/tailin.csv",
                  expectedFixedPath  = "/test/",
                  expectedVariableIndex = -1),
        
        TestSetup(filePattern        = "/**/tailin.csv",
                  expectedFixedPath  = "/",
                  expectedVariableIndex = 1),
        
        TestSetup(filePattern        = "/test/**/tailin.csv",
                  expectedFixedPath  = "/test/",
                  expectedVariableIndex = 6),
                  
        TestSetup(filePattern        = "/test/**/foo/**/tailin.csv",
                  expectedFixedPath  = "/test/",
                  expectedVariableIndex = 6),
                  
        TestSetup(filePattern        = "/test/*/tailin.csv",
                  expectedFixedPath  = "/test/",
                  expectedVariableIndex = 6),
                  
        TestSetup(filePattern        = "/test/*tailin.csv",
                  expectedFixedPath  = "/test/",
                  expectedVariableIndex = 6),
                  
//        TestSetup(filePattern        = "/test**/tailin.csv", //TODO do we support this? What should it do?
//                  expectedFixedPath  = "/",
//                  expectedVariableIndex = 5),
//                  
//        TestSetup(filePattern        = "/test/**tailin.csv", //not supported
//                  expectedFixedPath  = "/",
//                  expectedVariableIndex = 6),
                  
        TestSetup(filePattern        = "/test*/tailin.csv",
                  expectedFixedPath  = "/",
                  expectedVariableIndex = 5),
                  
        TestSetup(filePattern        = "/test?/tailin.csv",
                  expectedFixedPath  = "/",
                  expectedVariableIndex = 5),
                  
        TestSetup(filePattern        = "/tes*/tailin.csv",
                  expectedFixedPath  = "/",
                  expectedVariableIndex = 4),
                  
        TestSetup(filePattern        = "/tes[a,t]/tailin.csv",
                  expectedFixedPath  = "/",
                  expectedVariableIndex = 4),
                  
        TestSetup(filePattern        = "/tes[a-zA-Z]/tailin.csv",
                  expectedFixedPath  = "/",
                  expectedVariableIndex = 4),
                  
        TestSetup(filePattern        = "/te*t/tailin.csv",
                  expectedFixedPath  = "/",
                  expectedVariableIndex = 3),
                  
        TestSetup(filePattern        = "/*est/tailin.csv",
                  expectedFixedPath  = "/",
                  expectedVariableIndex = 1),
      )
    
    
    
    "should get the fixed directory's path" - {
      
      testSetups.foreach { testSetup =>
        
        s"${testSetup.filePattern} has fixed parent-dir: ${testSetup.expectedFixedPath}" in
        {
          val result = FileMatchPattern.extractInvariableDir(testSetup.filePattern)
          
          result shouldBe Some(testSetup.expectedFixedPath)
        }
      }
    }
    
    
    "should find the first variable index in" - {
      
      testSetups.foreach { testSetup =>
        
        s"${testSetup.filePattern} which should be at position ${testSetup.expectedVariableIndex}" in
        {
          val result = FileMatchPattern.findFirstVariableIndex(testSetup.filePattern)
          
          result shouldBe (
                            if (testSetup.expectedVariableIndex == -1)
                              -1
                            else
                              testSetup.expectedVariableIndex
                          )
        }
      }
    }
    
    
    
    "should match" -
    {
      val tests = Seq(
                  ("\\\\path\\t*\\log.txt",
                   "\\\\path\\to\\log.txt"),
                  ("/path/t*/log.txt",
                   "/path/to/log.txt"),
                  ("C:\\path\\t*\\log.txt",
                   "C:\\path\\to\\log.txt"),
                 )
      
      tests.foreach { test =>
        
        test._2 + " on " + test._1 in {
          val patternString = test._1
          val matchPattern = new FileMatchPattern(patternString)
          
          val fileName = test._2
          val file = mock[FileHandle]
          (file.absolutePath _)
            .expects()
            .returns(fileName)
          
          matchPattern.matches(file) shouldBe true
        }
      }
    }
    
    
    "should determine that a given directory is on the correct path (is a super-directory of matching files)" - {
      "" in {
        
        val patternString = "/path/to/test/log.txt"
        val matchPattern = new FileMatchPattern(patternString)
        
        val dirPath = "/path/to/"
        
        val dir = mock[DirHandle]
        (dir.absolutePath _)
          .expects()
          .returns(dirPath)
          
        matchPattern.isSuperDir(dir) shouldBe true
      }
    }
  }
}
