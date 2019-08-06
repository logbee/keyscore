package io.logbee.keyscore.pipeline.contrib.tailin.watch

import java.io.File
import java.nio.file._

import io.logbee.keyscore.pipeline.contrib.tailin.file.local.LocalFile
import io.logbee.keyscore.pipeline.contrib.tailin.util.{SpecWithTempDir, TestUtil}
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest._
import org.scalatestplus.junit.JUnitRunner
import io.logbee.keyscore.pipeline.contrib.tailin.file.local.LocalDir
import io.logbee.keyscore.pipeline.contrib.tailin.file.FileHandle
import io.logbee.keyscore.pipeline.contrib.tailin.file.DirHandle

@RunWith(classOf[JUnitRunner])
class LocalDirWatcherSpec extends SpecWithTempDir with Matchers with MockFactory with Inside with OptionValues with ParallelTestExecution {
  
  
  trait DirWatcherParams {
    var provider = mock[WatcherProvider]
    var dirPath = watchDir
    var matchPattern = new FileMatchPattern(s"$dirPath/*.txt")
  }
  
  
  "A LocalDirWatcher," - { //TODO these whole tests should be done with mocked FileHandle
    "when a sub-directory" - {
      "is created, should create a DirWatcher for that sub-directory" in
      new DirWatcherParams {
        
        matchPattern = new FileMatchPattern(fullFilePattern = s"$watchDir/*/test.txt")
        val dirWatcher = new DirWatcher(new LocalDir(dirPath), matchPattern, provider)
        
        val subDir = Paths.get(s"$watchDir/testDir/")
        subDir.toFile.mkdir
        TestUtil.waitForFileToExist(subDir.toFile)
        
        
        val subDirWatcher = stub[BaseDirWatcher]
        (provider.createDirWatcher _)
          .expects(new LocalDir(subDir), matchPattern)
          .returning(subDirWatcher)
        
        
        dirWatcher.processChanges()
        
        (subDirWatcher.processChanges _).verify()
      }
      
      
      "is deleted, should notify the responsible DirWatcher" in
      new DirWatcherParams {
        
        matchPattern = new FileMatchPattern(fullFilePattern = s"$watchDir/*/test.txt")
        val dirWatcher = new DirWatcher(new LocalDir(dirPath), matchPattern, provider)
        
        //create and register a directory
        val subDir = Paths.get(s"$watchDir/testDir/")
        subDir.toFile.mkdir()
        TestUtil.waitForFileToExist(subDir.toFile)
        
        val subDirWatcher = stub[BaseDirWatcher]
        (provider.createDirWatcher _)
          .expects(new LocalDir(subDir), matchPattern)
          .returning(subDirWatcher)
        
        
        dirWatcher.processChanges()
        
        subDir.toFile.delete()
        TestUtil.waitForFileToBeDeleted(subDir.toFile)
        
        dirWatcher.processChanges()
        (subDirWatcher.pathDeleted _).verify()
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
            val dirWatcher = new DirWatcher(new LocalDir(dirPath), matchPattern, provider)
            
            val file = new File(s"$watchDir/test.txt")
            
            (provider.createFileEventHandler _).expects(new LocalFile(file)).returning(stub[FileEventHandler])
            
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
            val subDir2 = new LocalDir(subDir)
            
            val subDirWatcher = new DirWatcher(subDir2, matchPattern, provider)
            (provider.createDirWatcher _).expects(subDir2, matchPattern).returns(subDirWatcher)
            val fileEventHandler = stub[FileEventHandler]
            (provider.createFileEventHandler _).expects(file).returns(fileEventHandler)
            
            val dirWatcher = new DirWatcher(new LocalDir(dirPath), matchPattern, provider)
            
            
            dirWatcher.processChanges()
            
            
            
            
            
            (fileEventHandler.processChanges _).verify()
          }
        }
      }
      
      
      "is created, but doesn't match the file pattern, should NOT create a FileEventHandler" in
      new DirWatcherParams {
        
        val dirWatcher = new DirWatcher(new LocalDir(dirPath), matchPattern, provider)
        
        val file = new File(s"$watchDir/test.foobar")
        
        (provider.createFileEventHandler _).expects(new LocalFile(file)).never()
        
        file.createNewFile()
        
        TestUtil.waitForFileToExist(file)
        
        dirWatcher.processChanges()
      }
      
      
      "is modified, should notify the responsible FileEventHandlers that the file was modified" in
      new DirWatcherParams {
        
        val dirWatcher = new DirWatcher(new LocalDir(dirPath), matchPattern, provider)
        
        val file = new File(s"$watchDir/test.txt")
        
        val subFileEventHandler = stub[FileEventHandler]
        (provider.createFileEventHandler _).expects(new LocalFile(file)).returning(subFileEventHandler)
        
        file.createNewFile()
        TestUtil.waitForFileToExist(file)
        
        dirWatcher.processChanges()
        
        //write something to file
        TestUtil.writeStringToFile(file, "Hello World", StandardOpenOption.APPEND)
        
        TestUtil.waitForWatchService()
        
        dirWatcher.processChanges()
        
        (subFileEventHandler.processChanges _).verify().twice //twice, because DirWatcher calls this, too, when setting up the FileEventHandler
      }
      
      
      "is deleted, should notify the responsible FileEventHandler" ignore //TEST
      new DirWatcherParams {
        
        val dirWatcher = new DirWatcher(new LocalDir(dirPath), matchPattern, provider)
        
        val file = new File(s"$watchDir/test.txt")
        
        val subFileEventHandler = stub[FileEventHandler]
        (provider.createFileEventHandler _).expects(new LocalFile(file)).returning(subFileEventHandler)
        
        file.createNewFile
        TestUtil.waitForFileToExist(file)
        
        dirWatcher.processChanges()
        
        file.delete()
        TestUtil.waitForFileToBeDeleted(file)
        
        dirWatcher.processChanges()
        
        (subFileEventHandler.pathDeleted _).verify()
      }
    }
  }
}
