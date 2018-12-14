package io.logbee.keyscore.pipeline.contrib.tailin.file

import java.io.File
import java.nio.file._

import io.logbee.keyscore.pipeline.contrib.tailin._
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest._
import org.scalatest.junit.JUnitRunner
import io.logbee.keyscore.pipeline.contrib.tailin.util.TestUtil

//@RunWith(classOf[JUnitRunner])
class DirWatcherSpec extends FreeSpec with BeforeAndAfter with Matchers with MockFactory with Inside with OptionValues with ParallelTestExecution {

  var watchDir: Path = null

  before {
    watchDir = Files.createTempDirectory("watchTest")

    TestUtil.waitForFileToExist(watchDir.toFile)
  }

  after {
    TestUtil.recursivelyDelete(watchDir)
  }

  
  trait DirWatcherParams {
    var provider = mock[WatcherProvider]
    var configuration = DirWatcherConfiguration(dirPath = watchDir, filePattern = "*.txt", recursionDepth = 0)
    var callback = (_: String) => ()
  }

  
  "A DirWatcher," - {
    "when a sub-directory" - {
      "is created, should create a DirWatcher for that sub-directory, which's processEvents() is called when the parent's processEvents() is called" in new DirWatcherParams {
        
        configuration = configuration.copy(recursionDepth = 1)
        val dirWatcher = new DefaultDirWatcher(configuration, provider, callback)
    
        val subDir = Paths.get(watchDir + "/testDir/")

        val subDirWatcher = stub[DirWatcher]
        (provider.createDirWatcher _).expects(configuration.copy(dirPath=subDir, recursionDepth = configuration.recursionDepth - 1)).returning(subDirWatcher)

        subDir.toFile.mkdir
        TestUtil.waitForFileToExist(subDir.toFile)


        dirWatcher.processEvents()

        //call another time to verify that it's called on the sub-DirWatcher
        dirWatcher.processEvents()
        (subDirWatcher.processEvents _).verify()
      }

      "is deleted, should notify the responsible DirWatcher" in new DirWatcherParams {
        
        configuration = configuration.copy(recursionDepth = 1)
        val dirWatcher = new DefaultDirWatcher(configuration, provider, callback)
        
        //create and register a directory
        val subDir = Paths.get(watchDir + "/testDir/")
        
        val subDirWatcher = stub[DirWatcher]
        (provider.createDirWatcher _).expects(configuration.copy(dirPath=subDir, recursionDepth = configuration.recursionDepth - 1)).returning(subDirWatcher)
        
        subDir.toFile.mkdir()
        TestUtil.waitForFileToExist(subDir.toFile)
        
        dirWatcher.processEvents()

        subDir.toFile.delete()
        TestUtil.waitForFileToBeDeleted(subDir.toFile)

        dirWatcher.processEvents()
        (subDirWatcher.pathDeleted _).verify()
      }
    }

    "when a file" - {
      "is created, should create a FileWatcher, for the file pattern" - {
        
        "**.txt" in new DirWatcherParams { //should behave like "*.txt", as we only check the files in the current directory
          
          configuration = configuration.copy(filePattern = "**.txt")          
          val dirWatcher = new DefaultDirWatcher(configuration, provider, callback)
          
          val file = new File(watchDir + "/test.txt")
          
          (provider.createFileWatcher _).expects(file).returning(stub[FileWatcher])
  
          file.createNewFile()
  
          TestUtil.waitForFileToExist(file)
  
          dirWatcher.processEvents()
        }
        
        "*.txt" in new DirWatcherParams {
          
          configuration = configuration.copy(filePattern = "*.txt")          
          val dirWatcher = new DefaultDirWatcher(configuration, provider, callback)
          
          val file = new File(watchDir + "/test.txt")
          
          (provider.createFileWatcher _).expects(file).returning(stub[FileWatcher])
  
          file.createNewFile()
  
          TestUtil.waitForFileToExist(file)
  
          dirWatcher.processEvents()
        }
        
        "test.txt" in new DirWatcherParams {
          
          configuration = configuration.copy(filePattern = "test.txt")          
          val dirWatcher = new DefaultDirWatcher(configuration, provider, callback)
          
          val file = new File(watchDir + "/test.txt")
          
          (provider.createFileWatcher _).expects(file).returning(stub[FileWatcher])
  
          file.createNewFile()
  
          TestUtil.waitForFileToExist(file)
  
          dirWatcher.processEvents()
        }
      }
      
      //TEST that a file created in a subdirectory is matched when recursionDepth is set
      
      "is created, but doesn't match the file pattern, should NOT create a FileWatcher" in new DirWatcherParams {
        
        val dirWatcher = new DefaultDirWatcher(configuration, provider, callback)
        
        val file = new File(watchDir + "/test.foobar")
        
        (provider.createFileWatcher _).expects(file).never()

        file.createNewFile()

        TestUtil.waitForFileToExist(file)

        dirWatcher.processEvents()
      }

      "is modified, should notify the responsible FileWatchers that the file was modified" in new DirWatcherParams {
        
        val dirWatcher = new DefaultDirWatcher(configuration, provider, callback)

        val file = new File(watchDir + "/test.txt")

        val subFileWatcher = stub[FileWatcher]
        (provider.createFileWatcher _).expects(file).returning(subFileWatcher)

        file.createNewFile()
        TestUtil.waitForFileToExist(file)

        dirWatcher.processEvents()

        //write something to file
        TestUtil.writeStringToFile(file, "Hello World", StandardOpenOption.APPEND)

        TestUtil.waitForWatchService()

        dirWatcher.processEvents()

        (subFileWatcher.fileModified _).verify(callback).twice //twice, because DirWatcher calls this, too, when setting up the FileWatcher
      }

      "is deleted, should notify the responsible FileWatcher" in new DirWatcherParams {
        
        val dirWatcher = new DefaultDirWatcher(configuration, provider, callback)
        
        val file = new File(watchDir + "/test.txt")

        val subFileWatcher = stub[FileWatcher]
        (provider.createFileWatcher _).expects(file).returning(subFileWatcher)

        file.createNewFile
        TestUtil.waitForFileToExist(file)

        dirWatcher.processEvents()

        file.delete()
        TestUtil.waitForFileToBeDeleted(file)

        dirWatcher.processEvents()

        (subFileWatcher.pathDeleted _).verify()
      }
    }
    
    "when its configured directory doesn't exist, should throw an exception" in new DirWatcherParams {
      
      val watchDir = Paths.get("/abc/def/ghi/jkl/mno/pqr/stu/vwx")
      configuration = configuration.copy(dirPath = watchDir)
      
      
      assertThrows[InvalidPathException] {
        val dirWatcher = new DefaultDirWatcher(configuration, provider, callback)
      }
    }
  }
}
