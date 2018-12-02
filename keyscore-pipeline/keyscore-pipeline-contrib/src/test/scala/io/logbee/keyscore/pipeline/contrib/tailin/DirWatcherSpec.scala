package io.logbee.keyscore.pipeline.contrib.tailin.logic

import java.io.File
import java.nio.file._

import io.logbee.keyscore.pipeline.contrib.tailin._
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DirWatcherSpec extends FreeSpec with BeforeAndAfter with Matchers with MockFactory with Inside with OptionValues with ParallelTestExecution {

  var watchDir: Path = null

  before {
    watchDir = Files.createTempDirectory("watchTest")

    TestUtility.waitForFileToExist(watchDir.toFile)
  }

  after {
    TestUtility.recursivelyDelete(watchDir)
  }

  trait TestDirWatcher {
    val provider = mock[WatcherProvider]
    val configuration = DirWatcherConfiguration(watchDir, "**.txt")
    val callback = (_: String) => ()
    val dirWatcher = new DefaultDirWatcher(configuration, provider, callback)
  }

  "A DirWatcher," - {
    "when a sub-directory" - {
      "is created, should create a DirWatcher for that sub-directory" - {
        "and this sub-DirWatcher should have its processEvents() called when the parent's processEvents() is called" in new TestDirWatcher {
      
          val subDir = Paths.get(watchDir + "/testDir/")
  
          val subDirWatcher = stub[DirWatcher]
          (provider.createDirWatcher _).expects(configuration.copy(dirPath=subDir)).returning(subDirWatcher)
  
          subDir.toFile.mkdir
          TestUtility.waitForFileToExist(subDir.toFile)
  
          dirWatcher.processEvents

          //call another time to verify that it's called on the sub-DirWatcher
          dirWatcher.processEvents
          (subDirWatcher.processEvents _).verify
        }
      }

      "is deleted, should notify the responsible DirWatcher" in new TestDirWatcher {
        //create and register a directory
        val subDir = Paths.get(watchDir + "/testDir/")
        val subDirWatcher = stub[DirWatcher]
        (provider.createDirWatcher _).expects(configuration.copy(dirPath=subDir)).returning(subDirWatcher)
        
        subDir.toFile.mkdir
        TestUtility.waitForFileToExist(subDir.toFile)
        
        dirWatcher.processEvents()

        subDir.toFile.delete
        TestUtility.waitForFileToBeDeleted(subDir.toFile)

        dirWatcher.processEvents()
        (subDirWatcher.pathDeleted _).verify()
      }
    }

    "when a file" - {
      "is created, and matches the file pattern, should create a FileWatcher" in new TestDirWatcher {
        
        val file = new File(watchDir + "/test.txt")
        
        (provider.createFileWatcher _).expects(file).returning(stub[FileWatcher])

        file.createNewFile

        TestUtility.waitForFileToExist(file)

        dirWatcher.processEvents
      }
      
      "is created, but doesn't match the file pattern, should NOT create a FileWatcher" in new TestDirWatcher {
        
        val file = new File(watchDir + "/test.foobar")
        
        (provider.createFileWatcher _).expects(file).never()

        file.createNewFile

        TestUtility.waitForFileToExist(file)

        dirWatcher.processEvents
      }

      "is modified, should notify the responsible FileWatchers that the file was modified" in new TestDirWatcher {

        val file = new File(watchDir + "/test.txt")

        val subFileWatcher = stub[FileWatcher]
        (provider.createFileWatcher _).expects(file).returning(subFileWatcher)

        file.createNewFile()
        TestUtility.waitForFileToExist(file)

        dirWatcher.processEvents()

        //write something to file
        TestUtility.writeStringToFile(file, "Hello World", StandardOpenOption.APPEND)

        TestUtility.waitForWatchService()

        dirWatcher.processEvents()

        (subFileWatcher.fileModified _).verify(callback).twice //twice, because DirWatcher calls this, too, when setting up the FileWatcher
      }

      "is deleted, should notify the responsible FileWatcher" in new TestDirWatcher {
        val file = new File(watchDir + "/test.txt")

        val subFileWatcher = stub[FileWatcher]
        (provider.createFileWatcher _).expects(file).returning(subFileWatcher)

        file.createNewFile
        TestUtility.waitForFileToExist(file)

        dirWatcher.processEvents()

        file.delete
        TestUtility.waitForFileToBeDeleted(file)

        dirWatcher.processEvents

        (subFileWatcher.pathDeleted _).verify()
      }
    }
  }
}
