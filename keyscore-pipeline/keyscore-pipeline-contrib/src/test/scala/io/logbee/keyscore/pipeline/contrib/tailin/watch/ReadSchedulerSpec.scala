package io.logbee.keyscore.pipeline.contrib.tailin.watch

import org.scalatest.Matchers
import io.logbee.keyscore.pipeline.contrib.tailin.util.TestUtil

import scala.reflect.runtime.universe._
import org.scalamock.scalatest.MockFactory
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadSchedule
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadScheduleItem
import java.nio.file.StandardOpenOption
import io.logbee.keyscore.pipeline.contrib.tailin.read.FileReadRecord
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.PersistenceContext
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadPersistence

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import io.logbee.keyscore.pipeline.contrib.tailin.util.RotateFilesSetup

@RunWith(classOf[JUnitRunner])
class ReadSchedulerSpec extends RotateFilesSetup with Matchers with MockFactory {

  trait PersistenceContextWithoutTimestamp extends LogFile {
    
    val persistenceContextWithoutTimestamp = mock[PersistenceContext]
    (persistenceContextWithoutTimestamp.load[FileReadRecord](_: String)(_: TypeTag[FileReadRecord]))
      .expects(logFile.getAbsolutePath, typeTag[FileReadRecord])
      .returning(Some(FileReadRecord(previousReadPosition = 0, previousReadTimestamp = 0, newerFilesWithSharedLastModified = 0)))
  }
  
  
  trait PersistenceContextWithTimestamp extends RotateFiles {
    
    val persistenceContextWithTimestamp = mock[PersistenceContext]
    
    (persistenceContextWithTimestamp.load[FileReadRecord](_: String)(_: TypeTag[FileReadRecord]))
      .expects(logFile.getAbsolutePath, typeTag[FileReadRecord])
      .returning(Some(FileReadRecord(previousReadPosition, previousReadTimestamp, newerFilesWithSharedLastModified = 0)))
  }
  
  
  trait ReadSchedulerSetup extends LogFile {
    val readSchedule = mock[ReadSchedule]
    val readPersistence = mock[ReadPersistence]
  }
  
  
  
  "A ReadScheduler should" - {
    
    "queue a read for a change in a file" in
    new ReadSchedulerSetup {
      
      (readPersistence.getCompletedRead _)
        .expects(logFile)
        .returning(FileReadRecord(previousReadPosition=0,
                                  previousReadTimestamp=0,
                                  newerFilesWithSharedLastModified = 0))
      
      val readScheduler = new ReadScheduler(logFile, defaultRotationPattern, readPersistence, readSchedule)
      
      (readSchedule.enqueue _)
        .expects(ReadScheduleItem(
          logFile,
          startPos = 0,
          endPos = logFile.length,
          logFile.lastModified,
          newerFilesWithSharedLastModified = 0))
          
      readScheduler.fileModified()
    }
    
    
    "queue multiple reads for a change in a file, to also catch up on changes in its rotated files" in
    new ReadSchedulerSetup with RotateFiles {
    
      inSequence {
        (readPersistence.getCompletedRead _)
          .expects(logFile)
          .returning(FileReadRecord(previousReadPosition,
                                    previousReadTimestamp,
                                    newerFilesWithSharedLastModified = 0))
          
        (readSchedule.enqueue _)
          .expects(ReadScheduleItem(
            logFile,
            startPos = previousReadPosition,
            endPos = logFile3_ModifiedAfterPreviousReadTimestamp.length,
            logFile3_ModifiedAfterPreviousReadTimestamp.lastModified,
            newerFilesWithSharedLastModified = 0))
            
        (readSchedule.enqueue _)
          .expects(ReadScheduleItem(
            logFile,
            startPos = 0,
            endPos = logFile2.length,
            logFile2.lastModified,
            newerFilesWithSharedLastModified = 0))
            
        (readSchedule.enqueue _)
          .expects(ReadScheduleItem(
            logFile,
            startPos = 0,
            endPos = logFile1.length,
            logFile1.lastModified,
            newerFilesWithSharedLastModified = 0))
            
        (readSchedule.enqueue _)
          .expects(ReadScheduleItem(
            logFile,
            startPos = 0,
            endPos = logFile.length,
            logFile.lastModified,
            newerFilesWithSharedLastModified = 0))
      }
      
      val readScheduler = new ReadScheduler(logFile, defaultRotationPattern, readPersistence, readSchedule)
      readScheduler.fileModified()
    }
    
    
    "schedule only reads for files, which haven't already been completely read" in
    new ReadSchedulerSetup with RotateFiles {
    
      inSequence {
        (readPersistence.getCompletedRead _)
          .expects(logFile)
          .returning(FileReadRecord(previousReadPosition,
                                    previousReadTimestamp,
                                    newerFilesWithSharedLastModified = 0))
        
        (readSchedule.enqueue _)
          .expects(ReadScheduleItem(
            logFile,
            startPos = previousReadPosition,
            endPos = logFile3_ModifiedAfterPreviousReadTimestamp.length,
            logFile3_ModifiedAfterPreviousReadTimestamp.lastModified,
            newerFilesWithSharedLastModified = 0)
          )
        
        val files = Seq(logFile2, logFile1, logFile)
        
        files.foreach { file =>
          (readSchedule.enqueue _)
            .expects(ReadScheduleItem(
              logFile,
              startPos = 0,
              endPos = file.length,
              file.lastModified,
              newerFilesWithSharedLastModified = 0)
            )
        }
      }
      
      val readScheduler = new ReadScheduler(logFile, defaultRotationPattern, readPersistence, readSchedule)
      readScheduler.fileModified()
    }
    
    
    "schedule reads from the correct starting position, when a read up to that position has already been completed" in
    new ReadSchedulerSetup {
    
      val previousReadPosition = 3
      
      inSequence {
        (readPersistence.getCompletedRead _)
          .expects(logFile)
          .returning(FileReadRecord(previousReadPosition,
                                    previousReadTimestamp = 0,
                                    newerFilesWithSharedLastModified = 0))
        
        (readSchedule.enqueue _)
          .expects(ReadScheduleItem(
            logFile,
            startPos = previousReadPosition,
            endPos = logFile.length,
            logFile.lastModified,
            newerFilesWithSharedLastModified = 0))
      }
      
      val readScheduler = new ReadScheduler(logFile, defaultRotationPattern, readPersistence, readSchedule)
      readScheduler.fileModified()
    }
    //TODO adjust test(-descriptions) to accommodate for us not caring anymore whether a read has completed or just scheduled
    
    
    "schedule reads from the last completed read position, even if a read to a further position has already been scheduled (but not completed)" in //TODO does this test make sense?
    new ReadSchedulerSetup {
      
      val previousReadPosition = 2
      
      (readPersistence.getCompletedRead _)
        .expects(logFile)
        .returning(FileReadRecord(previousReadPosition, previousReadTimestamp = 0, newerFilesWithSharedLastModified = 0))
      
      val readScheduler = new ReadScheduler(logFile, defaultRotationPattern, readPersistence, readSchedule)
      
      inSequence {
        //schedule a read up to the current file length
        (readSchedule.enqueue _)
          .expects(ReadScheduleItem(
            logFile,
            startPos = previousReadPosition,
            endPos = logFile.length,
            logFile.lastModified,
            newerFilesWithSharedLastModified = 0))
        readScheduler.fileModified()
        
        val previousEndPos = logFile.length
        
        //append something more to the file
        TestUtil.writeStringToFile(logFile, "222\nö222", StandardOpenOption.APPEND)
        
        //expect it to read again from the start to the new file length
        (readSchedule.enqueue _)
          .expects(ReadScheduleItem(
            logFile,
            startPos = previousEndPos,
            endPos = logFile.length,
            logFile.lastModified,
            newerFilesWithSharedLastModified = 0))
        readScheduler.fileModified()
      }
    }
    
    
    "schedule reads from the start (including rotated files), when a read for this file has not yet been scheduled or completed" in
    new ReadSchedulerSetup with RotateFiles {
    
      inSequence {
        (readPersistence.getCompletedRead _)
          .expects(logFile)
          .returning(FileReadRecord(0, 0, 0))
        
        val files = Seq(logFile4_ModifiedBeforePreviousReadTimestamp, logFile3_ModifiedAfterPreviousReadTimestamp, logFile2, logFile1, logFile)
        
        files.foreach { file =>
          (readSchedule.enqueue _)
            .expects(ReadScheduleItem(
              logFile,
              startPos = 0,
              endPos = file.length,
              file.lastModified,
              newerFilesWithSharedLastModified = 0))
        }
      }
      
      val readScheduler = new ReadScheduler(logFile, defaultRotationPattern, readPersistence, readSchedule)
      readScheduler.fileModified()
    }
    
    
    
    "schedule a read only for the newest file, if the files have been rotated" in
    new ReadSchedulerSetup with RotateFiles {
      
      inSequence {
        (readPersistence.getCompletedRead _)
          .expects(logFile)
          .returning(FileReadRecord(previousReadPosition=logFile.length,
                                    previousReadTimestamp=logFile.lastModified,
                                    newerFilesWithSharedLastModified=0)
                    )
        
        
        logFile3_ModifiedAfterPreviousReadTimestamp.renameTo(logFile4_ModifiedBeforePreviousReadTimestamp)
        logFile2.renameTo(logFile3_ModifiedAfterPreviousReadTimestamp)
        logFile1.renameTo(logFile2)
        logFile.renameTo(logFile1)
        
        Thread.sleep(1000) //TODO this is currently necessary to offset the lastModified-timestamps -> make this nicer by manually setting the lastModified-timestamps
        
        logFile.createNewFile()
        TestUtil.waitForFileToExist(logFile)
        TestUtil.writeStringToFile(logFile, "Test", StandardOpenOption.APPEND)
        println("logFile: " + logFile.lastModified)
        
        (readSchedule.enqueue _)
          .expects(ReadScheduleItem(
            logFile,
            startPos = 0,
            endPos = logFile.length,
            logFile.lastModified,
            newerFilesWithSharedLastModified = 0))
      }
      
      val readScheduler = new ReadScheduler(logFile, defaultRotationPattern, readPersistence, readSchedule)
      readScheduler.fileModified()
    }
    
    
    
    trait SharedLastModifiedFilesSetup {
      val baseFile = TestUtil.createFile(watchDir, "file", "0")
      val rotatePattern = baseFile.getName + ".[1-5]"
      
      //if the lastModified-timestamp is equal, how new the file is, is decided by what rotation-index is included in the file-name (usually something like '.1', '.2' etc.)
      val file1Name = baseFile.getName + ".1"
      val file2Name = baseFile.getName + ".2"
      
      val file1 = TestUtil.createFile(watchDir, file1Name, "11")
      val file2 = TestUtil.createFile(watchDir, file2Name, "222")
      
      val sharedLastModified = 123456789
      
      baseFile.setLastModified(sharedLastModified + 12345678) //specifically different from the others (in the happy flow)
      file1.setLastModified(sharedLastModified)
      file2.setLastModified(sharedLastModified)
    }
    
    
    
    
    "schedule reads for files with the same lastModified-timestamp with correct information about the number of newer files with same lastModified-timestamp" in
    new ReadSchedulerSetup with SharedLastModifiedFilesSetup {
    
      val previousReadPosition = file2.length / 2
      
      inSequence {
        (readPersistence.getCompletedRead _)
          .expects(baseFile)
          .returning(FileReadRecord(previousReadPosition,
                                    previousReadTimestamp=0,
                                    newerFilesWithSharedLastModified=0))
        
        
        (readSchedule.enqueue _)
          .expects(ReadScheduleItem(
            baseFile,
            startPos = previousReadPosition,
            endPos = file2.length,
            file2.lastModified,
            newerFilesWithSharedLastModified = 1)
          )
        
        (readSchedule.enqueue _)
          .expects(ReadScheduleItem(
            baseFile,
            startPos = 0,
            endPos = file1.length,
            file1.lastModified,
            newerFilesWithSharedLastModified = 0)
          )
          
          
        (readSchedule.enqueue _)
          .expects(ReadScheduleItem(
            baseFile,
            startPos = 0,
            endPos = baseFile.length,
            baseFile.lastModified,
            newerFilesWithSharedLastModified = 0)
          )
      }
      
      val readScheduler = new ReadScheduler(baseFile, rotatePattern, readPersistence, readSchedule)
      readScheduler.fileModified()
    }
    
    
    
    "schedule reads correctly when it needs to resume in files with shared lastModified-timestamps" in
    new ReadSchedulerSetup with SharedLastModifiedFilesSetup {
      
      val previousReadPosition = file1.length / 2
      val newerFilesWithSharedLastModified = 0
      
      inSequence {
        (readPersistence.getCompletedRead _)
          .expects(baseFile)
          .returning(FileReadRecord(previousReadPosition,
                                    previousReadTimestamp=file1.lastModified, //==sharedLastModified (except that it may be rounded down, because the filesystem does not have the same resolution for lastModified-timestamps)
                                    newerFilesWithSharedLastModified))
        
        (readSchedule.enqueue _)
          .expects(ReadScheduleItem(
            baseFile,
            startPos = previousReadPosition,
            endPos = file1.length,
            file1.lastModified,
            newerFilesWithSharedLastModified = 0)
          )
          
          
        (readSchedule.enqueue _)
          .expects(ReadScheduleItem(
            baseFile,
            startPos = 0,
            endPos = baseFile.length,
            baseFile.lastModified,
            newerFilesWithSharedLastModified = 0)
          )
      }
      val readScheduler = new ReadScheduler(baseFile, rotatePattern, readPersistence, readSchedule)
      readScheduler.fileModified()
    }
    
    
    
    
    "should not schedule reads for files which share their lastModified-timestamp with the non-rotated log file (to which data can still be appended to)" in
    new ReadSchedulerSetup with SharedLastModifiedFilesSetup {
      //This tested behaviour is important, as otherwise the non-rotated log file's lastModified-timestamp could change at some point in the future (as data can still be appended to this file).
      //We need it to not change, as we differentiate between files with identical lastModified-timestamp by basically counting from the newest of these shared-lastModified files towards the oldest.
      
      //If for example the rotated files with rotation-indexes ".3", ".2" and ".1" in the file-name, share the same lastModified-timestamp,
      //then we can say that for .3 there's still 2 newerFilesWithSharedLastModified (which is .2 and .1),
      //for .2 there's newerFilesWithSharedLastModified=1 and for .1 there's newerFilesWithSharedLastModified=0.
      //(This can be assumed, because when rotating, the indexes should always get bigger. Going towards .0 makes no sense.)
      
      //When reading out from the schedule, the ".3", ".2", ".1" in the file-name may have changed through rotation (each become bigger), so we can't rely on these.
      //Instead what we do, is to look at files with this lastModified-timestamp (which will stay the same for these files, as it only changes when new data is appended, which shouldn't happen after they've been rotated away).
      //Then from these shared-lastModified files, we find the file with the lowest index. This must have been .1 when we were scheduling it.
      //For .2, we know that there was newerFilesWithSharedLastModified=1, so we take the next-bigger index (with the same lastModified-timestamp).
      //For .3, we know that there was newerFilesWithSharedLastModified=2, so we go two indexes further from the lowest index with this same lastModified-timestamp.
      
      
      
      baseFile.setLastModified(sharedLastModified) //this file specifically shares its lastModified-timestamp in this example
      file2.setLastModified(sharedLastModified - 12345) //this file has an older lastModified-timestamp, so should still get scheduled
      
      val previousReadPosition = file2.length / 2
      
      inSequence {
        (readPersistence.getCompletedRead _)
          .expects(baseFile)
          .returning(FileReadRecord(previousReadPosition,
                                    previousReadTimestamp=0,
                                    newerFilesWithSharedLastModified=0))
        
        
        (readSchedule.enqueue _)
          .expects(ReadScheduleItem(
            baseFile,
            startPos = previousReadPosition,
            endPos = file2.length,
            file2.lastModified,
            newerFilesWithSharedLastModified = 0))
          
          
        (readSchedule.enqueue _)
          .expects(*)
          .never
      }
      
      val readScheduler = new ReadScheduler(baseFile, rotatePattern, readPersistence, readSchedule)
      readScheduler.fileModified()
    }
  }
}
