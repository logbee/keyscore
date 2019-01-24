package io.logbee.keyscore.pipeline.contrib.tailin.watch

import java.io.File
import java.nio.file._

import io.logbee.keyscore.pipeline.contrib.tailin._
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest._
import org.scalatest.junit.JUnitRunner
import io.logbee.keyscore.pipeline.contrib.tailin.util.TestUtil

@RunWith(classOf[JUnitRunner])
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
    var configuration = DirWatcherConfiguration(dirPath = watchDir, matchPattern = DirWatcherPattern(watchDir + "/*.txt"))
  }

  
  "A DirWatcher," - {
    "when a sub-directory" - {
      "is created, should create a DirWatcher for that sub-directory, which's processEvents() is called when the parent's processEvents() is called" in new DirWatcherParams {
        
        configuration = configuration.copy(matchPattern = DirWatcherPattern(fullFilePattern = watchDir + "/*/test.txt", depth = 2))
        val dirWatcher = new DefaultDirWatcher(configuration, provider)
    
        val subDir = Paths.get(watchDir + "/testDir/")

        val subDirWatcher = stub[DirWatcher]
        (provider.createDirWatcher _)
          .expects(configuration.copy(dirPath=subDir,
                                      matchPattern = configuration.matchPattern.copy(subDirPattern = "*", depth=3)))
          .returning(subDirWatcher)

        subDir.toFile.mkdir
        TestUtil.waitForFileToExist(subDir.toFile)


        dirWatcher.processEvents()

        //call another time to verify that it's called on the sub-DirWatcher
        dirWatcher.processEvents()
        (subDirWatcher.processEvents _).verify()
      }

      "is deleted, should notify the responsible DirWatcher" in new DirWatcherParams {
        
        configuration = configuration.copy(matchPattern = DirWatcherPattern(fullFilePattern = watchDir + "/*/test.txt", depth=2))
        val dirWatcher = new DefaultDirWatcher(configuration, provider)
        
        //create and register a directory
        val subDir = Paths.get(watchDir + "/testDir/")
        
        val subDirWatcher = stub[DirWatcher]
        (provider.createDirWatcher _)
          .expects(configuration.copy(dirPath=subDir,
                                      matchPattern = configuration.matchPattern.copy(subDirPattern = "*", depth=3)))
          .returning(subDirWatcher)
        
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
            
            configuration = configuration.copy(matchPattern = DirWatcherPattern(watchDir + "/" + setup.pattern))
            val dirWatcher = new DefaultDirWatcher(configuration, provider)
            
            val file = new File(watchDir + "/test.txt")
            
            (provider.createFileWatcher _).expects(file).returning(stub[FileWatcher])
    
            file.createNewFile()
    
            TestUtil.waitForFileToExist(file)
    
            dirWatcher.processEvents()
          }
        }
      }
      
      "is created in a sub-directory, should create a FileWatcher, for the pattern" - {

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

            provider = stub[WatcherProvider]

            configuration = configuration.copy(matchPattern = DirWatcherPattern(fullFilePattern = watchDir + "/" + setup.startingPattern, depth=2))
            val dirWatcher = new DefaultDirWatcher(configuration, provider)


            val subDir = Paths.get(watchDir + "/subDir/")

            Files.createDirectory(subDir)
            TestUtil.waitForFileToExist(subDir.toFile)

            val file = TestUtil.createFile(subDir, "test.txt", "testInhalt")

            dirWatcher.processEvents()

            val fileWatcher = stub[FileWatcher]
            (provider.createFileWatcher _).when(file).returns(fileWatcher)
            
            
            val subDirWatcherConfig = DirWatcherConfiguration(subDir, DirWatcherPattern(configuration.matchPattern.fullFilePattern, setup.subDirPattern, depth=3))
            val subDirWatcher = new DefaultDirWatcher(subDirWatcherConfig, provider)
            (provider.createDirWatcher _).verify(subDirWatcherConfig).returns(subDirWatcher)
            
            (fileWatcher.fileModified _).verify()
          }
        }
      }
      
      
      "is created, but doesn't match the file pattern, should NOT create a FileWatcher" in new DirWatcherParams {
        
        val dirWatcher = new DefaultDirWatcher(configuration, provider)
        
        val file = new File(watchDir + "/test.foobar")
        
        (provider.createFileWatcher _).expects(file).never()

        file.createNewFile()

        TestUtil.waitForFileToExist(file)

        dirWatcher.processEvents()
      }

      "is modified, should notify the responsible FileWatchers that the file was modified" in new DirWatcherParams {
        
        val dirWatcher = new DefaultDirWatcher(configuration, provider)

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

        (subFileWatcher.fileModified _).verify().twice //twice, because DirWatcher calls this, too, when setting up the FileWatcher
      }

      "is deleted, should notify the responsible FileWatcher" in new DirWatcherParams {
        
        val dirWatcher = new DefaultDirWatcher(configuration, provider)
        
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
        val dirWatcher = new DefaultDirWatcher(configuration, provider)
      }
    }
  }
}
