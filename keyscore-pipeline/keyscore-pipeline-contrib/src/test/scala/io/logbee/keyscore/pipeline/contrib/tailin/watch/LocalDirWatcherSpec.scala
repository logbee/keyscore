package io.logbee.keyscore.pipeline.contrib.tailin.watch

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file._

import io.logbee.keyscore.pipeline.contrib.tailin.file.local.{LocalDir, LocalFile}
import io.logbee.keyscore.pipeline.contrib.tailin.util.{SpecWithTempDir, TestUtil}
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Inside, OptionValues, ParallelTestExecution}
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class LocalDirWatcherSpec extends SpecWithTempDir with Matchers with MockFactory with Inside with OptionValues with ParallelTestExecution {
  
  implicit val charset = StandardCharsets.UTF_8

  trait DirWatcherParams {
    var provider = mock[WatcherProvider[LocalDir, LocalFile]]
    var dirPath = watchDir
    var matchPattern = new FileMatchPattern[LocalDir, LocalFile](s"$dirPath/*.txt")
  }
  
  
  "A LocalDirWatcher," - {
    "when a sub-directory" - {
      "is created, should create a DirWatcher for that sub-directory" in
      new DirWatcherParams {
        
        matchPattern = new FileMatchPattern(fullFilePattern = s"$watchDir/*/test.txt")
        val dirWatcher = new DirWatcher[LocalDir, LocalFile](LocalDir(dirPath), matchPattern, provider)
        
        val subDir = Paths.get(s"$watchDir/testDir/")
        subDir.toFile.mkdir
        TestUtil.waitForFileToExist(subDir.toFile)
        
        
        val subDirWatcher = stub[BaseDirWatcher]
        (provider.createDirWatcher _)
          .expects(LocalDir(subDir), matchPattern)
          .returning(subDirWatcher)
        
        
        dirWatcher.processChanges()
        
        (subDirWatcher.processChanges _).verify()
      }
    }
    
    
    "when a file" - {
      "is created, should create a FileEventHandler, for the file pattern" - {
        
        case class FilePatternSetup(pattern: String)
        
        val filePatterns = Seq(
                                FilePatternSetup(pattern="*.txt"),
                                FilePatternSetup(pattern="test.txt"),
                                FilePatternSetup(pattern="t?st.txt"),
                                FilePatternSetup(pattern="t[e,a]st.txt"),
                                FilePatternSetup(pattern="te[a-z]t.txt"),
                              )
        
        
        filePatterns.foreach { setup => //individual test cases
          
          setup.pattern in
          new DirWatcherParams {
            
            matchPattern = new FileMatchPattern(s"$watchDir/${setup.pattern}")
            val dirWatcher = new DirWatcher(LocalDir(dirPath), matchPattern, provider)
            
            val file = new File(s"$watchDir/test.txt")
            
            (provider.createFileEventHandler _).expects(LocalFile(file)).returning(stub[FileEventHandler])
            
            file.createNewFile()
            
            TestUtil.waitForFileToExist(file)
            
            dirWatcher.processChanges()
          }
        }
      }
      
      
      "is created in a sub-directory, should create a FileEventHandler, for the pattern" - {
        
        case class SubDirFilePatternSetup(startingPattern: String)
        
        val filePatterns = Seq(
                                SubDirFilePatternSetup(startingPattern="**/*.txt"),
                                SubDirFilePatternSetup(startingPattern="**/test.txt"),
                                SubDirFilePatternSetup(startingPattern="*/*.txt"),
                                SubDirFilePatternSetup(startingPattern="*/test.txt"),
                                SubDirFilePatternSetup(startingPattern="subD?r/test.txt"),
                                SubDirFilePatternSetup(startingPattern="s[a,u]bDir/test.txt"),
                                SubDirFilePatternSetup(startingPattern="sub[A-Z]ir/test.txt"),
                              )
        
        
        filePatterns.foreach { setup => //individual test cases
          
          setup.startingPattern ignore //TEST
          new DirWatcherParams {
            
            matchPattern = new FileMatchPattern(fullFilePattern = s"$watchDir/${setup.startingPattern}")
            
            val subDir = Paths.get(s"$watchDir/subDir/")
            Files.createDirectory(subDir)
            TestUtil.waitForFileToExist(subDir.toFile)
            val file = TestUtil.createFile(subDir, "test.txt", "testContent")
            val subDir2 = LocalDir(subDir)
            
            val subDirWatcher = new DirWatcher(subDir2, matchPattern, provider)
            (provider.createDirWatcher _).expects(subDir2, matchPattern).returns(subDirWatcher)
            val fileEventHandler = stub[FileEventHandler]
            (provider.createFileEventHandler _).expects(file).returns(fileEventHandler)
            
            val dirWatcher = new DirWatcher(LocalDir(dirPath), matchPattern, provider)
            
            
            dirWatcher.processChanges()
            
            
            
            
            
            (fileEventHandler.processChanges _).verify()
          }
        }
      }
      
      
      "is created, but doesn't match the file pattern, should NOT create a FileEventHandler" in
      new DirWatcherParams {
        
        val dirWatcher = new DirWatcher(LocalDir(dirPath), matchPattern, provider)
        
        val file = new File(s"$watchDir/test.foobar")
        
        (provider.createFileEventHandler _).expects(LocalFile(file)).never()
        
        file.createNewFile()
        
        TestUtil.waitForFileToExist(file)
        
        dirWatcher.processChanges()
      }
      
      
      "is modified, should notify the responsible FileEventHandlers that the file was modified" in
      new DirWatcherParams {
        
        val dirWatcher = new DirWatcher(LocalDir(dirPath), matchPattern, provider)
        
        val file = new File(s"$watchDir/test.txt")
        
        val subFileEventHandler = stub[FileEventHandler]
        (provider.createFileEventHandler _).expects(LocalFile(file)).returning(subFileEventHandler)
        
        file.createNewFile()
        TestUtil.waitForFileToExist(file)
        
        dirWatcher.processChanges()
        
        //write something to file
        TestUtil.writeStringToFile(file, "Hello World", StandardOpenOption.APPEND)
        
        TestUtil.waitForWatchService()
        
        dirWatcher.processChanges()
        
        (subFileEventHandler.processChanges _).verify().twice //twice, because DirWatcher calls this, too, when setting up the FileEventHandler
      }
    }
  }
}
