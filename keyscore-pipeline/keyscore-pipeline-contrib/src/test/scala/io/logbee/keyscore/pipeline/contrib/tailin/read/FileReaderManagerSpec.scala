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
  
  
  trait FileReaderManagerSetup {
    val readSchedule = mock[ReadSchedule]
    val readPersistence = mock[ReadPersistence]
    val fileReaderProvider = mock[FileReaderProvider]
    val rotationPattern = ".[1-5]"
    
    val fileReaderManager = new FileReaderManager(fileReaderProvider, readSchedule, readPersistence, rotationPattern)
    
    val callback = mockFunction[FileReadData, Unit]
    
    
    val line1 = "abcde"
    val line2 = "fghij"
    val newline = "\n"
    val text = line1 + newline + line2
    val testFile = TestUtil.createFile(watchDir, ".fileReaderManagerTestFile", text)
    
    val readScheduleItem = ReadScheduleItem(testFile, startPos=0, endPos=testFile.length, testFile.lastModified, newerFilesWithSharedLastModified=0)
    
    (readSchedule.dequeue _)
      .expects()
      .returning(Option(readScheduleItem))
  }
  
  
  
  "A FileReaderManager should" - { //TODO more tests
    
    "read out a scheduled entry" in 
    new FileReaderManagerSetup {
      
      inSequence {
        (readPersistence.getCompletedRead _)
          .expects(testFile)
          .returning(FileReadRecord(previousReadPosition=0, previousReadTimestamp=0, newerFilesWithSharedLastModified=0))
        
        (fileReaderProvider.create _)
          .expects(testFile)
          .returning(new FileReader(testFile, rotationPattern="", byteBufferSize=1024, charset=StandardCharsets.UTF_8, readMode=ReadMode.LINE))
        
        
        callback.expects(FileReadData(string=line1,
                                      baseFile=testFile,
                                      physicalFile=testFile,
                                      readEndPos=charset.encode(line1 + newline).limit,
                                      writeTimestamp=testFile.lastModified,
                                      newerFilesWithSharedLastModified=0))
        callback.expects(FileReadData(string=line2,
                                      baseFile=testFile,
                                      physicalFile=testFile,
                                      readEndPos=charset.encode(text).limit,
                                      writeTimestamp=testFile.lastModified,
                                      newerFilesWithSharedLastModified=0))
      }
      
      fileReaderManager.getNextString(callback)
    }
    
    
    
    "read out multiple scheduled entries" in
    new FileReaderManagerSetup {
      inSequence {
        (readPersistence.getCompletedRead _)
          .expects(testFile)
          .returning(FileReadRecord(previousReadPosition=0, previousReadTimestamp=0, newerFilesWithSharedLastModified=0))
        
        
        (fileReaderProvider.create _)
          .expects(testFile)
          .returning(new FileReader(testFile, rotationPattern="", byteBufferSize=1024, charset=StandardCharsets.UTF_8, readMode=ReadMode.LINE))
        
        
        callback.expects(FileReadData(string=line1,
                                      baseFile=testFile,
                                      physicalFile=testFile,
                                      readEndPos=charset.encode(line1 + newline).limit,
                                      writeTimestamp=testFile.lastModified,
                                      newerFilesWithSharedLastModified=0))
        callback.expects(FileReadData(string=line2,
                                      baseFile=testFile,
                                      physicalFile=testFile,
                                      readEndPos=charset.encode(text).limit,
                                      writeTimestamp=testFile.lastModified,
                                      newerFilesWithSharedLastModified=0))
        
        
        
        val content2 = "22222" 
        val testFile2 = TestUtil.createFile(watchDir, ".fileReaderManagerTestFile2", content2)
        
        val readScheduleItem2 = ReadScheduleItem(testFile2, startPos=0, endPos=testFile2.length, testFile2.lastModified, newerFilesWithSharedLastModified=0)
        
        (readSchedule.dequeue _)
          .expects()
          .returning(Option(readScheduleItem2))
        
        (readPersistence.getCompletedRead _)
          .expects(testFile2)
          .returning(FileReadRecord(previousReadPosition=0, previousReadTimestamp=0, newerFilesWithSharedLastModified=0))
        
        (fileReaderProvider.create _)
          .expects(testFile2)
          .returning(new FileReader(testFile2, rotationPattern="", byteBufferSize=1024, charset=StandardCharsets.UTF_8, readMode=ReadMode.LINE))
        
        callback.expects(FileReadData(string=content2,
                                      baseFile=testFile2,
                                      physicalFile=testFile2,
                                      readEndPos=charset.encode(content2).limit,
                                      writeTimestamp=testFile2.lastModified,
                                      newerFilesWithSharedLastModified=0))
      }
      
      fileReaderManager.getNextString(callback)
      fileReaderManager.getNextString(callback)
    }
  }
}
