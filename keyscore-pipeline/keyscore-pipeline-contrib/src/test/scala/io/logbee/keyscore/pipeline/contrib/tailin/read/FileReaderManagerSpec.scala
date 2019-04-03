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
import io.logbee.keyscore.pipeline.contrib.tailin.util.RotateFilesSetup

@RunWith(classOf[JUnitRunner])
class FileReaderManagerSpec extends RotateFilesSetup with Matchers with MockFactory {
  
  val charset = Charset.forName("UTF-8")
  
  
  trait FileReaderManagerSetup extends RotateFiles {
    val readSchedule = mock[ReadSchedule]
    val readPersistence = mock[ReadPersistence]
    val fileReaderProvider = mock[FileReaderProvider]
    
    val fileReaderManager = new FileReaderManager(fileReaderProvider, readSchedule, readPersistence, defaultRotationPattern)
    
    val callback = mockFunction[FileReadData, Unit]
  }
  
  
  
  "A FileReaderManager should" - { //TODO more tests
    
    "read out a scheduled entry" in
    new FileReaderManagerSetup {
      
      inSequence {
        val readScheduleItem = ReadScheduleItem(logFile, startPos=previousReadPosition, endPos=logFile3_ModifiedAfterPreviousReadTimestamp.length, previousReadTimestamp, newerFilesWithSharedLastModified=0)
        
        (readSchedule.dequeue _)
          .expects()
          .returning(Option(readScheduleItem))
        
        (readPersistence.getCompletedRead _)
          .expects(logFile)
          .returning(FileReadRecord(previousReadPosition, previousReadTimestamp, newerFilesWithSharedLastModified=0))
        
        (fileReaderProvider.create _)
          .expects(logFile3_ModifiedAfterPreviousReadTimestamp)
          .returning(new FileReader(logFile3_ModifiedAfterPreviousReadTimestamp, rotationPattern="", byteBufferSize=1024, charset=charset, readMode=ReadMode.LINE))
        
        
        callback expects where {
          calledBackDataIsSimilarTo(
            FileReadData(
              string = logFile3Data.substring(previousReadPosition),
              baseFile = logFile,
              physicalFile = logFile3_ModifiedAfterPreviousReadTimestamp,
              readEndPos = charset.encode(logFile3Data).limit,
              writeTimestamp = previousReadTimestamp,
              readTimestamp = -1,
              newerFilesWithSharedLastModified = 0
            )
          )
        }
      }
      
      fileReaderManager.getNextString(callback)
    }
    
    
    
    "read out multiple scheduled entries" in
    new FileReaderManagerSetup {
      inSequence {
        val readScheduleItem = ReadScheduleItem(logFile, startPos=previousReadPosition, endPos=logFile3_ModifiedAfterPreviousReadTimestamp.length, previousReadTimestamp, newerFilesWithSharedLastModified=0)
        
        (readSchedule.dequeue _)
          .expects()
          .returning(Option(readScheduleItem))
        
        (readPersistence.getCompletedRead _)
          .expects(logFile)
          .returning(FileReadRecord(previousReadPosition, previousReadTimestamp, newerFilesWithSharedLastModified=0))
        
        
        (fileReaderProvider.create _)
          .expects(logFile3_ModifiedAfterPreviousReadTimestamp)
          .returning(new FileReader(fileToRead=logFile3_ModifiedAfterPreviousReadTimestamp,
                                    rotationPattern=defaultRotationPattern,
                                    byteBufferSize=1024,
                                    charset=StandardCharsets.UTF_8,
                                    readMode=ReadMode.LINE))
        
        
        callback expects where {
          calledBackDataIsSimilarTo(
            FileReadData(
              string = logFile3Data.substring(previousReadPosition),
              baseFile = logFile,
              physicalFile = logFile3_ModifiedAfterPreviousReadTimestamp,
              readEndPos = charset.encode(logFile3Data).limit,
              writeTimestamp = previousReadTimestamp,
              readTimestamp = -1,
              newerFilesWithSharedLastModified = 0
            )
          )
        }
        
        val readScheduleItem2 = ReadScheduleItem(logFile, startPos=0, endPos=logFile2.length, logFile2.lastModified, newerFilesWithSharedLastModified=0)
        
        (readSchedule.dequeue _)
          .expects()
          .returning(Option(readScheduleItem2))
        
        (readPersistence.getCompletedRead _)
          .expects(logFile)
          .returning(FileReadRecord(previousReadPosition=charset.encode(logFile3Data).limit,
                                    previousReadTimestamp=readScheduleItem2.lastModified,
                                    newerFilesWithSharedLastModified=0))
        
        (fileReaderProvider.create _)
          .expects(logFile2)
          .returning(new FileReader(logFile2, rotationPattern=defaultRotationPattern, byteBufferSize=1024, charset=charset, readMode=ReadMode.LINE))
        
        
        callback expects where {
          calledBackDataIsSimilarTo(
            FileReadData(
              string = logFile2Data,
              baseFile = logFile,
              physicalFile = logFile2,
              readEndPos = charset.encode(logFile2Data).limit,
              writeTimestamp = logFile2.lastModified,
              readTimestamp = -1,
              newerFilesWithSharedLastModified = 0
            )
          )
        }
      }
      
      fileReaderManager.getNextString(callback)
      fileReaderManager.getNextString(callback)
    }
    
    
    "read out a scheduled entry from the correct file when rotation has occurred after scheduling and before reading out" in
    new FileReaderManagerSetup {
      
      rotate()
      
      inSequence {
        val readScheduleItem = ReadScheduleItem(logFile, startPos=previousReadPosition, endPos=charset.encode(logFile3Data).limit, previousReadTimestamp, newerFilesWithSharedLastModified=0)
        
        (readSchedule.dequeue _)
          .expects()
          .returning(Option(readScheduleItem))
        
        (readPersistence.getCompletedRead _)
          .expects(logFile)
          .returning(FileReadRecord(previousReadPosition, previousReadTimestamp, newerFilesWithSharedLastModified=0))
        
        
        
        //TODO is maybe this FileReader created before the rotate happens and therefore its fileReadChannel points to fileContent4 ?
          //Test works, if the rotate happens beforehand, which would confirm it,
          //but changing fileReaderManager to create a new fileReader every time doesn't seem to affect it.
          //
          //maybe because we always return the same in the following, it shadows the behaviour:
          //(real test does not currently work either, though)
        (fileReaderProvider.create _)
          .expects(logFile4_ModifiedBeforePreviousReadTimestamp) 
          .returning(new FileReader(fileToRead=logFile4_ModifiedBeforePreviousReadTimestamp,
                                    rotationPattern=defaultRotationPattern,
                                    byteBufferSize=1024,
                                    charset=charset,
                                    readMode=ReadMode.LINE))
        
        
        callback expects where {
          calledBackDataIsSimilarTo(
            FileReadData(
              string=logFile3Data.substring(previousReadPosition),
              baseFile=logFile,
              physicalFile=logFile4_ModifiedBeforePreviousReadTimestamp,
              readEndPos=charset.encode(logFile3Data).limit,
              writeTimestamp=previousReadTimestamp,
              readTimestamp = -1,
              newerFilesWithSharedLastModified=0
            )
          )
        }
        
      }
      
//      rotate()
      fileReaderManager.getNextString(callback)
    }
    
    //TODO TEST where rotation happens between readings, so that the newly rotated file is directly on the file-path we just read out
  }
}
