package io.logbee.keyscore.pipeline.contrib.tailin.watch

import io.logbee.keyscore.pipeline.contrib.tailin.file.{OpenDirHandle, OpenFileHandle}
import io.logbee.keyscore.pipeline.contrib.tailin.util.TestUtil.{OpenableMockDirHandle, OpenableMockFileHandle}
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, FreeSpec, Matchers}
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class FileMatchPatternSpec extends FreeSpec with Matchers with BeforeAndAfterAll with MockFactory {

  "A FileMatchPattern" - {
    
    case class TestSetup(filePattern: String,
                         expectedFixedPath: String,
                         expectedVariableIndex: Int,
                        )
    
    val testSetups = Seq(
        TestSetup(filePattern        = "/test/file.csv",
                  expectedFixedPath  = "/test/",
                  expectedVariableIndex = -1),
        
        TestSetup(filePattern        = "/**/file.csv",
                  expectedFixedPath  = "/",
                  expectedVariableIndex = 1),
        
        TestSetup(filePattern        = "/test/**/file.csv",
                  expectedFixedPath  = "/test/",
                  expectedVariableIndex = 6),
                  
        TestSetup(filePattern        = "/test/**/foo/**/file.csv",
                  expectedFixedPath  = "/test/",
                  expectedVariableIndex = 6),
                  
        TestSetup(filePattern        = "/test/*/file.csv",
                  expectedFixedPath  = "/test/",
                  expectedVariableIndex = 6),
                  
        TestSetup(filePattern        = "/test/*file.csv",
                  expectedFixedPath  = "/test/",
                  expectedVariableIndex = 6),
                  
//        TestSetup(filePattern        = "/test**/file.csv", //not supported
//                  expectedFixedPath  = "/",
//                  expectedVariableIndex = 5),
//                  
//        TestSetup(filePattern        = "/test/**file.csv", //not supported
//                  expectedFixedPath  = "/",
//                  expectedVariableIndex = 6),
                  
        TestSetup(filePattern        = "/test*/file.csv",
                  expectedFixedPath  = "/",
                  expectedVariableIndex = 5),
                  
        TestSetup(filePattern        = "/test?/file.csv",
                  expectedFixedPath  = "/",
                  expectedVariableIndex = 5),
                  
        TestSetup(filePattern        = "/tes*/file.csv",
                  expectedFixedPath  = "/",
                  expectedVariableIndex = 4),
                  
        TestSetup(filePattern        = "/tes[a,t]/file.csv",
                  expectedFixedPath  = "/",
                  expectedVariableIndex = 4),
                  
        TestSetup(filePattern        = "/tes[a-zA-Z]/file.csv",
                  expectedFixedPath  = "/",
                  expectedVariableIndex = 4),
                  
        TestSetup(filePattern        = "/te*t/file.csv",
                  expectedFixedPath  = "/",
                  expectedVariableIndex = 3),
                  
        TestSetup(filePattern        = "/*est/file.csv",
                  expectedFixedPath  = "/",
                  expectedVariableIndex = 1),
      )
    
    
    
    "should get the fixed directory's path" - {
      
      testSetups.foreach { testSetup =>
        
        s"${testSetup.filePattern} has fixed parent-dir: ${testSetup.expectedFixedPath}" in
        {
          val result = FileMatchPattern.extractInvariableDir(testSetup.filePattern, "/")
          
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
          
          val openFileHandle = mock[OpenFileHandle]
          val file = new OpenableMockFileHandle(test._2, openFileHandle)
          
          matchPattern.matches(file) shouldBe true
        }
      }
    }
    
    
    case class SuperPathSetup(dirPath: String, patternString: String)
    val superPathSetups = Seq[SuperPathSetup](
                              SuperPathSetup("/path/to",
                                             "/path/to/test/log.txt"),
                              SuperPathSetup("/path/to//",
                                             "/path/to/test/log.txt"),
                              SuperPathSetup("/path/to/test/",
                                             "/path/*/*/log.txt"),
                              SuperPathSetup("path/to/test/",
                                             "path/*/*/log.txt"),
                              SuperPathSetup("C:\\path\\to",
                                             "C:\\path\\to\\test\\log.txt"),
                              SuperPathSetup("\\\\hostname\\share\\path\\to",
                                             "\\\\hostname\\share\\path\\to\\test\\log.txt"),
                     )
    
    superPathSetups.foreach { testSetup =>
      s"should determine that '${testSetup.dirPath}' is a super-directory of '${testSetup.patternString}'" in {
        val matchPattern = new FileMatchPattern[OpenableMockDirHandle, OpenableMockFileHandle](testSetup.patternString)

        val openDirHandle = mock[OpenDirHandle[OpenableMockDirHandle, OpenableMockFileHandle]]
        val dir = new OpenableMockDirHandle(openDirHandle)
        (openDirHandle.absolutePath _).expects().returning(testSetup.dirPath)

        assert(matchPattern.isSuperDir(dir))
      }
    }
    
    "should not match files that match its exclusion pattern" in {
      val patternString = "/path/to/test/log*"
      val exclusionPatternString = "/path/to/test/log*_uploaded"
      
      val matchPattern = new FileMatchPattern(patternString, exclusionPatternString)
      
      val openFileMatchesInclusionOnly = mock[OpenFileHandle]
      val absolutePath1 = "/path/to/test/log.txt"
      val fileMatchesInclusionOnly = new OpenableMockFileHandle(absolutePath1, openFileMatchesInclusionOnly)

      val openFileMatchesInclusionAndExclusion = mock[OpenFileHandle]
      val absolutePath2 = "/path/to/test/log.txt_uploaded"
      val fileMatchesInclusionAndExclusion = new OpenableMockFileHandle(absolutePath2, openFileMatchesInclusionAndExclusion)
      
      matchPattern.matches(fileMatchesInclusionOnly) shouldBe true
      matchPattern.matches(fileMatchesInclusionAndExclusion) shouldBe false
    }
  }
}
