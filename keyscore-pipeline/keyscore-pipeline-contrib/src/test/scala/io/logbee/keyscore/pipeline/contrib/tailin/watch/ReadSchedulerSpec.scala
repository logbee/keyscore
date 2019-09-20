package io.logbee.keyscore.pipeline.contrib.tailin.watch

import io.logbee.keyscore.pipeline.contrib.tailin.persistence.{PersistenceContext, ReadPersistence, ReadSchedule, ReadScheduleItem}
import io.logbee.keyscore.pipeline.contrib.tailin.read.FileReader.FileReadRecord
import io.logbee.keyscore.pipeline.contrib.tailin.util.SpecWithRotateFiles
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

import scala.reflect.runtime.universe._

@RunWith(classOf[JUnitRunner])
class ReadSchedulerSpec extends SpecWithRotateFiles with Matchers with MockFactory {

  trait PersistenceContextWithoutTimestamp extends LogFile {
    
    val persistenceContextWithoutTimestamp = mock[PersistenceContext]
    (persistenceContextWithoutTimestamp.load[FileReadRecord](_: String)(_: TypeTag[FileReadRecord]))
      .expects(logFile.absolutePath, typeTag[FileReadRecord])
      .returning(Some(FileReadRecord(previousReadPosition = 0, previousReadTimestamp = 0, newerFilesWithSharedLastModified = 0)))
  }
  
  
  trait PersistenceContextWithTimestamp extends RotateFiles {
    
    val persistenceContextWithTimestamp = mock[PersistenceContext]
    
    (persistenceContextWithTimestamp.load[FileReadRecord](_: String)(_: TypeTag[FileReadRecord]))
      .expects(logFile.absolutePath, typeTag[FileReadRecord])
      .returning(Some(FileReadRecord(previousReadPosition, previousReadTimestamp, newerFilesWithSharedLastModified = 0)))
  }
  
  
  trait ReadSchedulerSetup {
    val readSchedule = mock[ReadSchedule]
    val readPersistence = mock[ReadPersistence]
  }
  

  "A ReadScheduler should" - {
    
    "queue a read for a change in a file" in
    new ReadSchedulerSetup with LogFile {
      
      (readPersistence.getCompletedRead _)
        .expects(*)
        .returning(FileReadRecord(previousReadPosition=0,
                                  previousReadTimestamp=0,
                                  newerFilesWithSharedLastModified = 0))
      
      val readScheduler = new ReadScheduler(logFile.file, rotationPattern, readPersistence, readSchedule)
      
      (readSchedule.enqueue _)
        .expects(ReadScheduleItem(
          logFile.file,
          startPos = 0,
          endPos = logFile.length(),
          logFile.lastModified,
          newerFilesWithSharedLastModified = 0))
          
      readScheduler.processChanges()
    }
    
    
    "queue multiple reads for a change in a file, to also catch up on changes in its rotated files" in
    new ReadSchedulerSetup with RotateFiles {
    
      inSequence {
        (readPersistence.getCompletedRead _)
          .expects(*)
          .returning(FileReadRecord(previousReadPosition,
                                    previousReadTimestamp,
                                    newerFilesWithSharedLastModified = 0))
          
        (readSchedule.enqueue _)
          .expects(ReadScheduleItem(
            logFile.file,
            startPos = previousReadPosition,
            endPos = logFile3.length(),
            logFile3.lastModified,
            newerFilesWithSharedLastModified = 0))
            
        (readSchedule.enqueue _)
          .expects(ReadScheduleItem(
            logFile.file,
            startPos = 0,
            endPos = logFile2.length(),
            logFile2.lastModified,
            newerFilesWithSharedLastModified = 0))
            
        (readSchedule.enqueue _)
          .expects(ReadScheduleItem(
            logFile.file,
            startPos = 0,
            endPos = logFile1.length(),
            logFile1.lastModified,
            newerFilesWithSharedLastModified = 0))
            
        (readSchedule.enqueue _)
          .expects(ReadScheduleItem(
            logFile.file,
            startPos = 0,
            endPos = logFile.length(),
            logFile.lastModified,
            newerFilesWithSharedLastModified = 0))
      }
      
      val readScheduler = new ReadScheduler(logFile.file, rotationPattern, readPersistence, readSchedule)
      readScheduler.processChanges()
    }
    
    
    "schedule only reads for files, which haven't already been completely read" in
    new ReadSchedulerSetup with RotateFiles {
    
      inSequence {
        (readPersistence.getCompletedRead _)
          .expects(*)
          .returning(FileReadRecord(previousReadPosition,
                                    previousReadTimestamp,
                                    newerFilesWithSharedLastModified = 0))
        
        (readSchedule.enqueue _)
          .expects(ReadScheduleItem(
            logFile.file,
            startPos = previousReadPosition,
            endPos = logFile3.length(),
            logFile3.lastModified,
            newerFilesWithSharedLastModified = 0)
          )
        
        val files = Seq(logFile2, logFile1, logFile)
        
        files.foreach { file =>
          (readSchedule.enqueue _)
            .expects(ReadScheduleItem(
              logFile.file,
              startPos = 0,
              endPos = file.length(),
              file.lastModified,
              newerFilesWithSharedLastModified = 0)
            )
        }
      }
      
      val readScheduler = new ReadScheduler(logFile.file, rotationPattern, readPersistence, readSchedule)
      readScheduler.processChanges()
    }
    
    
    "schedule reads from the correct starting position, when a read up to that position has already been completed" in
    new ReadSchedulerSetup with LogFile {
    
      val previousReadPosition = 3
      
      inSequence {
        (readPersistence.getCompletedRead _)
          .expects(*)
          .returning(FileReadRecord(previousReadPosition,
                                    previousReadTimestamp = 0,
                                    newerFilesWithSharedLastModified = 0))
        
        (readSchedule.enqueue _)
          .expects(ReadScheduleItem(
            logFile.file,
            startPos = previousReadPosition,
            endPos = logFile.length(),
            logFile.lastModified,
            newerFilesWithSharedLastModified = 0))
      }

      val readScheduler = new ReadScheduler(logFile.file, rotationPattern, readPersistence, readSchedule)
      readScheduler.processChanges()
    }
    
    
    "schedule reads from the start (including rotated files), when a read for this file has not yet been scheduled or completed" in
    new ReadSchedulerSetup with RotateFiles {

      inSequence {
        (readPersistence.getCompletedRead _)
          .expects(*)
          .returning(FileReadRecord(0, 0, 0))
        
        val files = Seq(logFile4, logFile3, logFile2, logFile1, logFile)
        
        files.foreach { file =>
          (readSchedule.enqueue _)
            .expects(ReadScheduleItem(
              logFile.file,
              startPos = 0,
              endPos = file.length(),
              file.lastModified,
              newerFilesWithSharedLastModified = 0))
        }
      }
      
      val readScheduler = new ReadScheduler(logFile.file, rotationPattern, readPersistence, readSchedule)
      readScheduler.processChanges()
    }
    
    
    
    "schedule a read only for the newest file, if the files have been rotated" in
    new ReadSchedulerSetup with RotateFiles {
      
      (readPersistence.getCompletedRead _)
        .expects(*)
        .returning(FileReadRecord(previousReadPosition=logFile.length(),
                                  previousReadTimestamp=logFile.lastModified,
                                  newerFilesWithSharedLastModified=0)
                  )


      rotate()

      (readSchedule.enqueue _)
        .expects(ReadScheduleItem(
          logFile.file,
          startPos = 0,
          endPos = logFile.length(),
          logFile.lastModified,
          newerFilesWithSharedLastModified = 0))
      
      val readScheduler = new ReadScheduler(logFile.file, rotationPattern, readPersistence, readSchedule)
      readScheduler.processChanges()
    }
    
    
    
    trait SharedLastModifiedFilesSetup extends RotateFiles {
      val sharedLastModified = 1234567890
      
      logFile2 = TestFileInfo(logFile2.absolutePath, logFile2.content, sharedLastModified)
      logFile1 = TestFileInfo(logFile1.absolutePath, logFile1.content, sharedLastModified)
      logFile  = TestFileInfo(logFile.absolutePath,  logFile.content,  sharedLastModified + 12345678, Seq(logFile2, logFile1).map(_.file)) //specifically different from the others (in the happy flow)
    }
    
    
    
    
    "schedule reads for files with the same lastModified-timestamp with correct information about the number of newer files with same lastModified-timestamp" in
    new ReadSchedulerSetup with SharedLastModifiedFilesSetup {

      previousReadPosition = logFile2.length() / 2
      
      inSequence {
        (readPersistence.getCompletedRead _)
          .expects(*)
          .returning(FileReadRecord(previousReadPosition,
                                    previousReadTimestamp=0,
                                    newerFilesWithSharedLastModified=0))
        
        
        (readSchedule.enqueue _)
          .expects(ReadScheduleItem(
            logFile.file,
            startPos = previousReadPosition,
            endPos = logFile2.length(),
            logFile2.lastModified,
            newerFilesWithSharedLastModified = 1)
          )
        
        (readSchedule.enqueue _)
          .expects(ReadScheduleItem(
            logFile.file,
            startPos = 0,
            endPos = logFile1.length(),
            logFile1.lastModified,
            newerFilesWithSharedLastModified = 0)
          )
          
          
        (readSchedule.enqueue _)
          .expects(ReadScheduleItem(
            logFile.file,
            startPos = 0,
            endPos = logFile.length(),
            logFile.lastModified,
            newerFilesWithSharedLastModified = 0)
          )
      }
      
      val readScheduler = new ReadScheduler(logFile.file, rotationPattern, readPersistence, readSchedule)
      readScheduler.processChanges()
    }
    
    
    
    "schedule reads correctly when it needs to resume in files with shared lastModified-timestamps" in
    new ReadSchedulerSetup with LogFile with SharedLastModifiedFilesSetup {
      
      previousReadPosition = logFile1.length() / 2
      val newerFilesWithSharedLastModified = 0
      
      inSequence {
        (readPersistence.getCompletedRead _)
          .expects(*)
          .returning(FileReadRecord(previousReadPosition,
                                    previousReadTimestamp=logFile1.lastModified, //==sharedLastModified (except that it may be rounded down, because the filesystem does not have the same resolution for lastModified-timestamps)
                                    newerFilesWithSharedLastModified))
        
        (readSchedule.enqueue _)
          .expects(ReadScheduleItem(
            logFile.file,
            startPos = previousReadPosition,
            endPos = logFile1.length(),
            logFile1.lastModified,
            newerFilesWithSharedLastModified = 0)
          )
          
          
        (readSchedule.enqueue _)
          .expects(ReadScheduleItem(
            logFile.file,
            startPos = 0,
            endPos = logFile.length(),
            logFile.lastModified,
            newerFilesWithSharedLastModified = 0)
          )
      }
      val readScheduler = new ReadScheduler(logFile.file, rotationPattern, readPersistence, readSchedule)
      readScheduler.processChanges()
    }
    
    
    
    
    "not schedule reads for files which share their lastModified-timestamp with the non-rotated log file (to which data can still be appended to)" in
    new ReadSchedulerSetup with LogFile with RotateFiles {
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
      
      
      val sharedLastModified = 1234567890

      logFile2 = TestFileInfo(logFile2.absolutePath, logFile2.content, sharedLastModified - 12345) //this file has an older lastModified-timestamp, so should still get scheduled
      logFile1 = TestFileInfo(logFile1.absolutePath, logFile1.content, sharedLastModified)
      logFile  = TestFileInfo(logFile.absolutePath,  logFile.content,  sharedLastModified, Seq(logFile2, logFile1).map(_.file)) //this file specifically shares its lastModified-timestamp in this example
      

      previousReadPosition = logFile2.length() / 2
      
      inSequence {
        (readPersistence.getCompletedRead _)
          .expects(*)
          .returning(FileReadRecord(previousReadPosition,
                                    previousReadTimestamp=0,
                                    newerFilesWithSharedLastModified=0))
        
        
        (readSchedule.enqueue _)
          .expects(ReadScheduleItem(
            logFile.file,
            startPos = previousReadPosition,
            endPos = logFile2.length(),
            logFile2.lastModified,
            newerFilesWithSharedLastModified = 0))
          
          
        (readSchedule.enqueue _)
          .expects(*)
          .never
      }
      
      val readScheduler = new ReadScheduler(logFile.file, rotationPattern, readPersistence, readSchedule)
      readScheduler.processChanges()
    }
  }
}
