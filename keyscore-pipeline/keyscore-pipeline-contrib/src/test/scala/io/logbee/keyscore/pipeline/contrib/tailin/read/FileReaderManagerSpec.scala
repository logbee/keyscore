package io.logbee.keyscore.pipeline.contrib.tailin.read

import org.scalatest.FreeSpec
import org.scalatest.Matchers
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadSchedule
import org.scalamock.scalatest.MockFactory
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.PersistenceContext
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadScheduleItem
import java.io.File
import io.logbee.keyscore.pipeline.contrib.tailin.util.TestUtil
import org.scalatest.BeforeAndAfter
import java.nio.file.Path
import java.nio.file.Files
import java.nio.charset.StandardCharsets
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadPersistence


import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.nio.charset.Charset

@RunWith(classOf[JUnitRunner])
class FileReaderManagerSpec extends FreeSpec with Matchers with MockFactory with BeforeAndAfter {
  
  
  var watchDir: Path = null

  before {
    watchDir = Files.createTempDirectory("watchTest")

    TestUtil.waitForFileToExist(watchDir.toFile)
  }

  after {
    TestUtil.recursivelyDelete(watchDir)
  }
  
  val charset = Charset.forName("UTF-8")
  
  "A FileReaderManager should" - {
    
    "do things" in { //TODO more tests
      val readSchedule = mock[ReadSchedule]
      val readPersistence = mock[ReadPersistence]
      val fileReaderProvider = mock[FileReaderProvider]
      
      val fileReaderManager = new FileReaderManager(fileReaderProvider, readSchedule, readPersistence)
      
      
      val callback = mockFunction[FileReadData, Unit]
      
      val line1 = "Hello"
      val line2 = "Wörld"
      val string = line1 + "\n" + line2
      val testFile = TestUtil.createFile(watchDir, ".fileReaderManagerTestFile", string)
      
      inSequence {
        (readSchedule.pop _)
          .expects()
          .returning(Option(ReadScheduleItem(testFile, startPos=0, endPos=testFile.length, writeTimestamp=testFile.lastModified)))
        
        (readPersistence.getCompletedRead _)
          .expects(testFile)
          .returning(FileReadRecord(0, 0))
        
        (fileReaderProvider.create _)
          .expects(testFile)
          .returning(new FileReader(testFile, rotationPattern="", byteBufferSize=1024, charset=StandardCharsets.UTF_8, readMode=ReadMode.LINE))
        
        
        callback.expects(FileReadData(line1, testFile, charset.encode(line1).limit + "\n".length, testFile.lastModified))
        callback.expects(FileReadData(line2, testFile, charset.encode(string).limit, testFile.lastModified))
      }
      
      fileReaderManager.getNextString(callback)
    }
    
    //upon being pulled, should just return the next string to be pushed
  }
}
