package io.logbee.keyscore.pipeline.contrib.tailin.file

import org.scalatest.FreeSpec
import org.scalatest.Matchers
import org.scalatest.BeforeAndAfter
import java.nio.file.Path
import io.logbee.keyscore.pipeline.contrib.tailin.util.TestUtil
import java.nio.file.Files
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.FilePersistenceContext
import scala.reflect.runtime.universe._
import org.scalamock.scalatest.MockFactory
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadSchedule
import java.io.File
import scala.io.Source
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadScheduleItem
import java.nio.file.StandardOpenOption

class ReadSchedulerSpec extends FreeSpec with Matchers with MockFactory with BeforeAndAfter {
  
  
  var watchDir: Path = null
  before {
    watchDir = Files.createTempDirectory("watchTest")
    TestUtil.waitForFileToExist(watchDir.toFile)
  }
  
  after {
    TestUtil.recursivelyDelete(watchDir)
  }
  
  
  
  
  trait LogFile {
    val logFileData = "Log_File_0_ "
    val logFile = TestUtil.createFile(watchDir, "log.txt", logFileData)
    
    val defaultRotationPattern = logFile.getName + ".[1-5]"
  }
  
  
  trait RotateFiles extends LogFile {
    
    val logFile1337Data = "Log_File_1337 "
    val logFile1337 = TestUtil.createFile(watchDir, "log.txt.1337", logFile1337Data)
    
    val logFileCsvData = "Log_File_Csv "
    val logFileCsv = TestUtil.createFile(watchDir, "log.csv", logFileCsvData)
    
    
    val otherLogFile1Data = "other_Log_File_1 "
    val otherLogFile1 = TestUtil.createFile(watchDir, "other_log.txt.1", otherLogFile1Data)
    
    val otherLogFileData = "other_Log_File "
    val otherLogFile = TestUtil.createFile(watchDir, "other_log.txt", otherLogFileData)
    
    
    val logFile4Data = "Log_File_4_4444 "
    val logFile4_ModifiedBeforePreviousReadTimestamp = TestUtil.createFile(watchDir, "log.txt.4", logFile4Data)
    
    val logFile3Data = "Log_File_3_333 "
    val logFile3_ModifiedAfterPreviousReadTimestamp = TestUtil.createFile(watchDir, "log.txt.3", logFile3Data)
    
    
    val logFile2Data = "Log_File_2_22 "
    val logFile2 = TestUtil.createFile(watchDir, "log.txt.2", logFile2Data)
    
    val logFile1Data = "Log_File_1_1 "
    val logFile1 = TestUtil.createFile(watchDir, "log.txt.1", logFile1Data)
    
    
    //set lastModified-times in the necessary order and to differences of bigger than 1 second to deal with common filesystem's lastModified-resolution of 1 second
    val logFileModified = System.currentTimeMillis
    logFile1337.setLastModified(logFileModified - 1000 * 3)
    logFileCsv.setLastModified(logFileModified - 1000 * 3)
    otherLogFile1.setLastModified(logFileModified - 1000 * 3)
    otherLogFile.setLastModified(logFileModified - 1000 * 2)
    logFile4_ModifiedBeforePreviousReadTimestamp.setLastModified(logFileModified - 1000 * 4)
    logFile3_ModifiedAfterPreviousReadTimestamp.setLastModified(logFileModified - 1000 * 3)
    logFile2.setLastModified(logFileModified - 1000 * 2)
    logFile1.setLastModified(logFileModified - 1000 * 1)
    logFile.setLastModified(logFileModified)
  }
  
  
  
  trait PersistenceContextWithoutTimestamp extends LogFile {
    
    val persistenceContextWithoutTimestamp = mock[FilePersistenceContext]
    (persistenceContextWithoutTimestamp.load[FileReadRecord] (_: String)(_: TypeTag[FileReadRecord]))
      .expects(logFile.getAbsolutePath, typeTag[FileReadRecord])
      .returning(Some(FileReadRecord(0, 0)))
  }
  

  trait PersistenceContextWithTimestamp extends RotateFiles {
    
    val previousReadPosition = logFile3Data.length / 2
    val previousReadTimestamp = logFile4_ModifiedBeforePreviousReadTimestamp.lastModified + 1
    
    val persistenceContextWithTimestamp: FilePersistenceContext = mock[FilePersistenceContext]
    
    (persistenceContextWithTimestamp.load[FileReadRecord] (_: String)(_: TypeTag[FileReadRecord]))
      .expects(logFile.getAbsolutePath, typeTag[FileReadRecord])
      .returning(Some(FileReadRecord(previousReadPosition, previousReadTimestamp)))
  }
  
  
  
  
  trait ReadSchedulerSetup extends LogFile {
    val readSchedule = mock[ReadSchedule]
  }
  
  
  "A ReadScheduler should" - {
    
    "queue a read for a change in a file" in
    new ReadSchedulerSetup with PersistenceContextWithoutTimestamp {
      
      val readScheduler = new ReadScheduler(logFile, defaultRotationPattern, persistenceContextWithoutTimestamp, readSchedule)
      
      (readSchedule.queue _).expects(ReadScheduleItem(logFile, startPos=0, endPos=logFileData.getBytes.length, logFile.lastModified))
      
      readScheduler.fileModified(_ => ())
    }
    
    
    "queue multiple reads for a change in a file, to also catch up on changes in its rotated files" in
    new ReadSchedulerSetup with RotateFiles {
      
      val previousReadPosition = 2
      val persistenceContext = mock[FilePersistenceContext]
      
      inSequence {
        (persistenceContext.load[FileReadRecord] (_: String)(_: TypeTag[FileReadRecord]))
          .expects(logFile.getAbsolutePath, typeTag[FileReadRecord])
          .returning(Some(FileReadRecord(previousReadPosition, previousReadTimestamp=0)))
      
      
        (readSchedule.queue _).expects(ReadScheduleItem(logFile, startPos=previousReadPosition, endPos=logFile4Data.getBytes.length, logFile4_ModifiedBeforePreviousReadTimestamp.lastModified))
        (readSchedule.queue _).expects(ReadScheduleItem(logFile, startPos=0, endPos=logFile3Data.getBytes.length, logFile3_ModifiedAfterPreviousReadTimestamp.lastModified))
        (readSchedule.queue _).expects(ReadScheduleItem(logFile, startPos=0, endPos=logFile2Data.getBytes.length, logFile2.lastModified))
        (readSchedule.queue _).expects(ReadScheduleItem(logFile, startPos=0, endPos=logFile1Data.getBytes.length, logFile1.lastModified))
        (readSchedule.queue _).expects(ReadScheduleItem(logFile, startPos=0, endPos=logFileData.getBytes.length, logFile.lastModified))
      }
      
      val readScheduler = new ReadScheduler(logFile, defaultRotationPattern, persistenceContext, readSchedule)
      readScheduler.fileModified(_ => ())
    }
    
    
    "queue only one read for a change in a file, when rotated files exist that have already been read" in
    new ReadSchedulerSetup with RotateFiles {
      
      val persistenceContext = mock[FilePersistenceContext]
      
      inSequence {
        (persistenceContext.load[FileReadRecord] (_: String)(_: TypeTag[FileReadRecord]))
          .expects(logFile.getAbsolutePath, typeTag[FileReadRecord])
          .returning(Some(FileReadRecord(previousReadPosition=logFile2.length, previousReadTimestamp=logFile2.lastModified)))
        
        (readSchedule.queue _).expects(ReadScheduleItem(logFile, startPos=0, endPos=logFile1Data.getBytes.length, logFile1.lastModified))
        (readSchedule.queue _).expects(ReadScheduleItem(logFile, startPos=0, endPos=logFileData.getBytes.length,  logFile.lastModified))
      }
      
      val readScheduler = new ReadScheduler(logFile, defaultRotationPattern, persistenceContext, readSchedule)
      readScheduler.fileModified(_ => ())
    }
    
    
    "queue a read from the correct starting position, when a read up to that position has already been *scheduled*" in
    new ReadSchedulerSetup with PersistenceContextWithoutTimestamp {
      
      val readScheduler = new ReadScheduler(logFile, defaultRotationPattern, persistenceContextWithoutTimestamp /*nothing has been read yet*/, readSchedule)
      val previousReadPosition = logFile.length
      
      inSequence {
        //schedule a read up to the current file length
        (readSchedule.queue _).expects(ReadScheduleItem(logFile, startPos=0, endPos=previousReadPosition, logFile.lastModified))
        readScheduler.fileModified(_ => ())
        
        //append something more to the file
        TestUtil.writeStringToFile(logFile, "222\nö222", StandardOpenOption.APPEND)
        
        //expect it to read on from that position
        (readSchedule.queue _).expects(ReadScheduleItem(logFile, startPos=previousReadPosition, endPos=logFile.length, logFile.lastModified))
        readScheduler.fileModified(_ => ())
      }
    }
    
    
    "queue a read from the correct starting position, when a read up to that position has already been *completed*" in
    new ReadSchedulerSetup {
      
      val previousReadPosition = 3
      val persistenceContext = mock[FilePersistenceContext]
      
      inSequence {
        (persistenceContext.load[FileReadRecord] (_: String)(_: TypeTag[FileReadRecord]))
          .expects(logFile.getAbsolutePath, typeTag[FileReadRecord])
          .returning(Some(FileReadRecord(previousReadPosition, previousReadTimestamp=0)))
         
        (readSchedule.queue _).expects(ReadScheduleItem(logFile, startPos=previousReadPosition, endPos=logFileData.getBytes.length, logFile.lastModified))
      }
      
      val readScheduler = new ReadScheduler(logFile, defaultRotationPattern, persistenceContext, readSchedule)
      readScheduler.fileModified(_ => ())
    }
    
    
    "queue a read from the start (including rotated files), when a read for this file has not yet been scheduled or completed" in
    new ReadSchedulerSetup with RotateFiles {
      
      val persistenceContext = mock[FilePersistenceContext]
      
      inSequence {
        (persistenceContext.load[FileReadRecord] (_: String)(_: TypeTag[FileReadRecord]))
          .expects(logFile.getAbsolutePath, typeTag[FileReadRecord])
          .returning(None)
        
        
        (readSchedule.queue _).expects(ReadScheduleItem(logFile, startPos=0, endPos=logFile4_ModifiedBeforePreviousReadTimestamp.length, logFile4_ModifiedBeforePreviousReadTimestamp.lastModified))
        (readSchedule.queue _).expects(ReadScheduleItem(logFile, startPos=0, endPos=logFile3_ModifiedAfterPreviousReadTimestamp.length, logFile3_ModifiedAfterPreviousReadTimestamp.lastModified))
        (readSchedule.queue _).expects(ReadScheduleItem(logFile, startPos=0, endPos=logFile2.length, logFile2.lastModified))
        (readSchedule.queue _).expects(ReadScheduleItem(logFile, startPos=0, endPos=logFile1.length, logFile1.lastModified))
        (readSchedule.queue _).expects(ReadScheduleItem(logFile, startPos=0, endPos=logFile.length, logFile.lastModified))
      }
      
      val readScheduler = new ReadScheduler(logFile, defaultRotationPattern, persistenceContext, readSchedule)
      readScheduler.fileModified(_ => ())
    }
  }
}
