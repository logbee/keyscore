package io.logbee.keyscore.pipeline.contrib.tailin.watch

import java.io.File
import java.nio.file._

import io.logbee.keyscore.pipeline.contrib.tailin._
import org.scalamock.scalatest.MockFactory
import org.scalatest._
import io.logbee.keyscore.pipeline.contrib.tailin.util.TestUtil

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import io.logbee.keyscore.pipeline.contrib.tailin.util.SpecWithTempDir
import io.logbee.keyscore.pipeline.contrib.tailin.file.LocalFile

@RunWith(classOf[JUnitRunner])
class LocalDirWatcherSpec extends SpecWithTempDir with Matchers with MockFactory with Inside with OptionValues with ParallelTestExecution {
  
  
  trait DirWatcherParams {
    var provider = mock[WatcherProvider[Path]]
    var dirPath = watchDir
    var matchPattern = DirWatcherPattern(watchDir + "/*.txt")
  }
  
  
  "A DirWatcher," - {
    "when a sub-directory" - {
      "is created, should create a DirWatcher for that sub-directory, which's processEvents() is called when the parent's processEvents() is called" in new DirWatcherParams {
        
        matchPattern = DirWatcherPattern(fullFilePattern = watchDir + "/*/test.txt", depth = 2)
        val dirWatcher = new LocalDirWatcher(dirPath, matchPattern, provider)
        
        val subDir = Paths.get(watchDir + "/testDir/")
        
        val subDirWatcher = stub[DirWatcher]
        (provider.createDirWatcher _)
          .expects(subDir, matchPattern.copy(subDirPattern = "*", depth=3))
          .returning(subDirWatcher)
        
        subDir.toFile.mkdir
        TestUtil.waitForFileToExist(subDir.toFile)
        
        
        dirWatcher.processFileChanges()
        
        //call another time to verify that it's called on the sub-DirWatcher
        dirWatcher.processFileChanges()
        (subDirWatcher.processFileChanges _).verify()
      }
      
      
      "is deleted, should notify the responsible DirWatcher" in new DirWatcherParams {
        
        matchPattern = DirWatcherPattern(fullFilePattern = watchDir + "/*/test.txt", depth=2)
        val dirWatcher = new LocalDirWatcher(dirPath, matchPattern, provider)
        
        //create and register a directory
        val subDir = Paths.get(watchDir + "/testDir/")
        
        val subDirWatcher = stub[DirWatcher]
        (provider.createDirWatcher _)
          .expects(subDir, matchPattern.copy(subDirPattern = "*", depth=3))
          .returning(subDirWatcher)
        
        subDir.toFile.mkdir()
        TestUtil.waitForFileToExist(subDir.toFile)
        
        dirWatcher.processFileChanges()
        
        subDir.toFile.delete()
        TestUtil.waitForFileToBeDeleted(subDir.toFile)
        
        dirWatcher.processFileChanges()
        (subDirWatcher.pathDeleted _).verify()
      }
    }
    
    
    "when a file" - {
      "is created, should create a FileEventHandler, for the file pattern" - {
        
        case class FilePatternSetup(pattern: String)
        
        val filePatterns = Seq(
                                FilePatternSetup(pattern="**.txt"),
                                FilePatternSetup(pattern="*.txt"),
                                FilePatternSetup(pattern="test.txt"),
                                FilePatternSetup(pattern="t?st.txt"),
                                FilePatternSetup(pattern="t[e,a]st.txt"),
                                FilePatternSetup(pattern="te[a-z]t.txt"),
                              )
        
        
        filePatterns.foreach { setup => //individual test cases
          
          setup.pattern in new DirWatcherParams {
            
            matchPattern = DirWatcherPattern(watchDir + "/" + setup.pattern)
            val dirWatcher = new LocalDirWatcher(dirPath, matchPattern, provider)
            
            val file = new File(watchDir + "/test.txt")
            
            (provider.createFileEventHandler _).expects(new LocalFile(file)).returning(stub[FileEventHandler])
            
            file.createNewFile()
            
            TestUtil.waitForFileToExist(file)
            
            dirWatcher.processFileChanges()
          }
        }
      }
      
      
      "is created in a sub-directory, should create a FileEventHandler, for the pattern" - {
        
        case class SubDirFilePatternSetup(startingPattern: String, subDirPattern: String)
        
        val filePatterns = Seq(
                                SubDirFilePatternSetup(startingPattern="**.txt",               subDirPattern="**"),
                                SubDirFilePatternSetup(startingPattern="**/*.txt",             subDirPattern="**"),
                                SubDirFilePatternSetup(startingPattern="**/test.txt",          subDirPattern="**"),
                                SubDirFilePatternSetup(startingPattern="*/*.txt",              subDirPattern="*"),
                                SubDirFilePatternSetup(startingPattern="*/test.txt",           subDirPattern="*"),
                                SubDirFilePatternSetup(startingPattern="subD?r/test.txt",      subDirPattern="subD?r"),
                                SubDirFilePatternSetup(startingPattern="s[a,u]bDir/test.txt",  subDirPattern="s[a,u]bDir"),
                                SubDirFilePatternSetup(startingPattern="sub[A-Z]ir/test.txt",  subDirPattern="sub[A-Z]ir"),
                              )
        
        
        filePatterns.foreach { setup => //individual test cases
          
          setup.startingPattern in new DirWatcherParams {
            
            provider = stub[WatcherProvider[Path]]
            matchPattern = DirWatcherPattern(fullFilePattern = watchDir + "/" + setup.startingPattern, depth=2)
            val dirWatcher = new LocalDirWatcher(dirPath, matchPattern, provider)
            
            
            val subDir = Paths.get(watchDir + "/subDir/")
            
            Files.createDirectory(subDir)
            TestUtil.waitForFileToExist(subDir.toFile)
            
            val file = TestUtil.createFile(subDir, "test.txt", "testContent")
            
            dirWatcher.processFileChanges()
            
            val fileEventHandler = stub[FileEventHandler]
            (provider.createFileEventHandler _).when(file).returns(fileEventHandler)
            
            val subMatchPattern = DirWatcherPattern(matchPattern.fullFilePattern, setup.subDirPattern, depth=3)
            val subDirWatcher = new LocalDirWatcher(subDir, subMatchPattern, provider)
            (provider.createDirWatcher _).verify(subDir, subMatchPattern).returns(subDirWatcher)
            
            (fileEventHandler.processFileChanges _).verify()
          }
        }
      }
      
      
      "is created, but doesn't match the file pattern, should NOT create a FileEventHandler" in new DirWatcherParams {
        
        val dirWatcher = new LocalDirWatcher(dirPath, matchPattern, provider)
        
        val file = new File(watchDir + "/test.foobar")
        
        (provider.createFileEventHandler _).expects(new LocalFile(file)).never()
        
        file.createNewFile()
        
        TestUtil.waitForFileToExist(file)
        
        dirWatcher.processFileChanges()
      }
      
      
      "is modified, should notify the responsible FileEventHandlers that the file was modified" in new DirWatcherParams {
        
        val dirWatcher = new LocalDirWatcher(dirPath, matchPattern, provider)
        
        val file = new File(watchDir + "/test.txt")
        
        val subFileEventHandler = stub[FileEventHandler]
        (provider.createFileEventHandler _).expects(new LocalFile(file)).returning(subFileEventHandler)
        
        file.createNewFile()
        TestUtil.waitForFileToExist(file)
        
        dirWatcher.processFileChanges()
        
        //write something to file
        TestUtil.writeStringToFile(file, "Hello World", StandardOpenOption.APPEND)
        
        TestUtil.waitForWatchService()
        
        dirWatcher.processFileChanges()
        
        (subFileEventHandler.processFileChanges _).verify().twice //twice, because DirWatcher calls this, too, when setting up the FileEventHandler
      }
      
      
      "is deleted, should notify the responsible FileEventHandler" in new DirWatcherParams {
        
        val dirWatcher = new LocalDirWatcher(dirPath, matchPattern, provider)
        
        val file = new File(watchDir + "/test.txt")
        
        val subFileEventHandler = stub[FileEventHandler]
        (provider.createFileEventHandler _).expects(new LocalFile(file)).returning(subFileEventHandler)
        
        file.createNewFile
        TestUtil.waitForFileToExist(file)
        
        dirWatcher.processFileChanges()
        
        file.delete()
        TestUtil.waitForFileToBeDeleted(file)
        
        dirWatcher.processFileChanges()
        
        (subFileEventHandler.pathDeleted _).verify()
      }
    }
    
    
    "when its configured directory doesn't exist, should throw an exception" in new DirWatcherParams {
      
      val watchDir = Paths.get("/abc/def/ghi/jkl/mno/pqr/stu/vwx")
      dirPath = watchDir
      
      
      assertThrows[InvalidPathException] {
        val dirWatcher = new LocalDirWatcher(dirPath, matchPattern, provider)
      }
    }
  }
}
