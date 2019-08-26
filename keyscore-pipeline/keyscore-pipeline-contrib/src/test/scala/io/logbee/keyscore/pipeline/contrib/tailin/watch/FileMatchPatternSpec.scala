package io.logbee.keyscore.pipeline.contrib.tailin.watch

import java.nio.ByteBuffer

import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfterAll, FreeSpec, Matchers}
import org.scalatestplus.junit.JUnitRunner
import io.logbee.keyscore.pipeline.contrib.tailin.file.{DirChangeListener, DirHandle, FileHandle}
import org.scalamock.scalatest.MockFactory

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
          
          val file = fileHandleReturningAbsolutePath(test._2)
          
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
                              SuperPathSetup("C:\\path\\to",
                                             "C:\\path\\to\\test\\log.txt"),
                              SuperPathSetup("\\\\hostname\\share\\path\\to",
                                             "\\\\hostname\\share\\path\\to\\test\\log.txt"),
                     )
    
    superPathSetups.foreach { testSetup =>
      s"should determine that '${testSetup.dirPath}' is a super-directory of '${testSetup.patternString}'" in {
        val matchPattern = new FileMatchPattern(testSetup.patternString)

        val dir = dirHandleReturningAbsolutePath(testSetup.dirPath)

        matchPattern.isSuperDir(dir) shouldBe true
      }
    }
    
    "should not match files that match its exclusion pattern" in {
      val patternString = "/path/to/test/log*"
      val exclusionPatternString = "/path/to/test/log*_uploaded"
      
      val matchPattern = new FileMatchPattern(patternString, exclusionPatternString)
      
      //these are essentially mocked objects, providing absolutePath
      val fileMatchesInclusionOnly = fileHandleReturningAbsolutePath("/path/to/test/log.txt")
      val fileMatchesInclusionAndExclusion = fileHandleReturningAbsolutePath("/path/to/test/log.txt_uploaded")
      
      matchPattern.matches(fileMatchesInclusionOnly) shouldBe true
      matchPattern.matches(fileMatchesInclusionAndExclusion) shouldBe false
    }
  }


  /**
    * Basically a mock object which provides the given absolutePath.
    * (absolutePath is a val, so cannot be mocked.)
    */
  private def fileHandleReturningAbsolutePath(_absolutePath: String): FileHandle = {
    new FileHandle() {
      override val absolutePath: String = _absolutePath
      
      override val name: String = "name"
      override val parent: String = "parent"
      override def listRotatedFiles(rotationPattern: String): Seq[_ <: FileHandle] = ???
      override def length: Long = ???
      override def lastModified: Long = ???
      override def read(buffer: ByteBuffer, offset: Long): Int = ???
      override def delete(): Unit = ???
      override def move(newPath: String): Unit = ???
      override def tearDown(): Unit = ???
    }
  }
  
  
  /**
    * Basically a mock object which provides the given absolutePath.
    * (absolutePath is a val, so cannot be mocked.)
    */
  private def dirHandleReturningAbsolutePath(_absolutePath: String): DirHandle = {
    new DirHandle() {
      override val absolutePath: String = _absolutePath
      
      override def listDirsAndFiles: (Set[_ <: DirHandle], Set[_ <: FileHandle]) = ???
      override def getDirChangeListener(): DirChangeListener = ???
      override def tearDown(): Unit = ???
    }
  }
}
