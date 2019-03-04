package io.logbee.keyscore.pipeline.contrib.tailin.watch

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
import io.logbee.keyscore.pipeline.contrib.tailin.read.FileReadRecord
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.RAMPersistenceContext
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.PersistenceContext
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadPersistence

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import io.logbee.keyscore.pipeline.contrib.tailin.util.RotateFilesSetup

@RunWith(classOf[JUnitRunner])
class ReadSchedulerSpec extends RotateFilesSetup with Matchers with MockFactory {
  
  
  trait PersistenceContextWithoutTimestamp extends LogFile {
    
    val persistenceContextWithoutTimestamp = mock[PersistenceContext]
    (persistenceContextWithoutTimestamp.load[FileReadRecord] (_: String)(_: TypeTag[FileReadRecord]))
      .expects(logFile.getAbsolutePath, typeTag[FileReadRecord])
      .returning(Some(FileReadRecord(0, 0)))
  }
  

  trait PersistenceContextWithTimestamp extends RotateFiles {
    
    val persistenceContextWithTimestamp = mock[PersistenceContext]
    
    (persistenceContextWithTimestamp.load[FileReadRecord] (_: String)(_: TypeTag[FileReadRecord]))
      .expects(logFile.getAbsolutePath, typeTag[FileReadRecord])
      .returning(Some(FileReadRecord(previousReadPosition, previousReadTimestamp)))
  }
  
  
  trait ReadSchedulerSetup extends LogFile {
    val readSchedule = mock[ReadSchedule]
  }
  
  
  "A ReadScheduler should" - {
    
    "queue a read for a change in a file" in
    new ReadSchedulerSetup {
      
      val readPersistence = mock[ReadPersistence]
      
      (readPersistence.getCompletedRead _)
        .expects(logFile)
        .returning(FileReadRecord(0, 0))
      
      val readScheduler = new ReadScheduler(logFile, defaultRotationPattern, readPersistence, readSchedule)
      
      (readSchedule.push _).expects(ReadScheduleItem(logFile, startPos=0, endPos=logFile.length, logFile.lastModified))
      
      readScheduler.fileModified()
    }
    
    
    "queue multiple reads for a change in a file, to also catch up on changes in its rotated files" in
    new ReadSchedulerSetup with RotateFiles {
      
      val readPersistence = mock[ReadPersistence]
      
      inSequence {
        (readPersistence.getCompletedRead _)
          .expects(logFile)
          .returning(FileReadRecord(0, 0))
        
        (readSchedule.push _).expects(ReadScheduleItem(logFile, startPos=0, endPos=logFile.length, logFile.lastModified))
        
        
        (readPersistence.getCompletedRead _)
          .expects(logFile1)
          .returning(FileReadRecord(0, 0))
        
        (readSchedule.push _).expects(ReadScheduleItem(logFile1, startPos=0, endPos=logFile1.length, logFile1.lastModified))
        
        
        (readPersistence.getCompletedRead _)
          .expects(logFile2)
          .returning(FileReadRecord(0, 0))
        
        (readSchedule.push _).expects(ReadScheduleItem(logFile2, startPos=0, endPos=logFile2.length, logFile2.lastModified))
        
        
        (readPersistence.getCompletedRead _)
          .expects(logFile3_ModifiedAfterPreviousReadTimestamp)
          .returning(FileReadRecord(previousReadPosition, previousReadTimestamp))
        
        (readSchedule.push _).expects(ReadScheduleItem(logFile3_ModifiedAfterPreviousReadTimestamp, startPos=previousReadPosition, endPos=logFile3_ModifiedAfterPreviousReadTimestamp.length, logFile3_ModifiedAfterPreviousReadTimestamp.lastModified))
        
        
        (readPersistence.getCompletedRead _)
          .expects(logFile4_ModifiedBeforePreviousReadTimestamp)
          .returning(FileReadRecord(logFile4_ModifiedBeforePreviousReadTimestamp.length, logFile4_ModifiedBeforePreviousReadTimestamp.lastModified))
      }
      
      val readScheduler = new ReadScheduler(logFile, defaultRotationPattern, readPersistence, readSchedule)
      readScheduler.fileModified()
    }
    
    
    "schedule only reads for files, which haven't already been completely read" in
    new ReadSchedulerSetup with RotateFiles {
      
		  val readPersistence = mock[ReadPersistence]
      
      inSequence {
		    (readPersistence.getCompletedRead _)
		      .expects(logFile)
		      .returning(FileReadRecord(previousReadPosition=0, previousReadTimestamp=0))
		    
        (readSchedule.push _).expects(ReadScheduleItem(logFile, startPos=0, endPos=logFile.length,  logFile.lastModified))
        
        
        (readPersistence.getCompletedRead _)
		      .expects(logFile1)
		      .returning(FileReadRecord(previousReadPosition=0, previousReadTimestamp=0))
		    
        (readSchedule.push _).expects(ReadScheduleItem(logFile1, startPos=0, endPos=logFile1.length, logFile1.lastModified))
        
        
        
        val remainingCompletedFiles = Seq(logFile2, logFile3_ModifiedAfterPreviousReadTimestamp, logFile4_ModifiedBeforePreviousReadTimestamp)
        
        remainingCompletedFiles.foreach{file =>
          (readPersistence.getCompletedRead _)
		      .expects(file)
		      .returning(FileReadRecord(previousReadPosition=file.length, previousReadTimestamp=file.lastModified))
		    }
      }
      
      val readScheduler = new ReadScheduler(logFile, defaultRotationPattern, readPersistence, readSchedule)
      readScheduler.fileModified()
    }
    
    
    "schedule reads from the correct starting position, when a read up to that position has already been completed" in
    new ReadSchedulerSetup {
      
      val previousReadPosition = 3
		  val readPersistence = mock[ReadPersistence]
      
      inSequence {
        (readPersistence.getCompletedRead _)
          .expects(logFile)
          .returning(FileReadRecord(previousReadPosition, previousReadTimestamp=0))
        
        (readSchedule.push _).expects(ReadScheduleItem(logFile, startPos=previousReadPosition, endPos=logFile.length, logFile.lastModified))
      }
      
      val readScheduler = new ReadScheduler(logFile, defaultRotationPattern, readPersistence, readSchedule)
      readScheduler.fileModified()
    }
    
    
    "schedule reads from the last completed read position, even if a read to a further position has already been scheduled (but not completed)" in //our ReadSchedule works like a stack, so the previously scheduled entry will be encountered later and then get ignored
    new ReadSchedulerSetup {
      
      val readPersistence = mock[ReadPersistence]
      
      val previousReadTimestamp = 2
      
      
      (readPersistence.getCompletedRead _)
        .expects(logFile)
        .returning(FileReadRecord(previousReadTimestamp, previousReadTimestamp=0))
        .twice
      
      val readScheduler = new ReadScheduler(logFile, defaultRotationPattern, readPersistence, readSchedule)
      
      inSequence {
        //schedule a read up to the current file length
        (readSchedule.push _).expects(ReadScheduleItem(logFile, startPos=previousReadTimestamp, endPos=logFile.length, logFile.lastModified))
        readScheduler.fileModified()
        
        //append something more to the file
        TestUtil.writeStringToFile(logFile, "222\nö222", StandardOpenOption.APPEND)
        
        
        //expect it to read again from the start to the new file length
        (readSchedule.push _).expects(ReadScheduleItem(logFile, startPos=previousReadTimestamp, endPos=logFile.length, logFile.lastModified))
        readScheduler.fileModified()
      }
    }
    
    
    "schedule reads from the start (including rotated files), when a read for this file has not yet been scheduled or completed" in
    new ReadSchedulerSetup with RotateFiles {
      
      val readPersistence = mock[ReadPersistence]
      
      inSequence {
        
        val files = Seq(logFile, logFile1, logFile2, logFile3_ModifiedAfterPreviousReadTimestamp, logFile4_ModifiedBeforePreviousReadTimestamp)
        
        files.foreach { file =>
          (readPersistence.getCompletedRead _)
            .expects(file)
            .returning(FileReadRecord(0, 0))
          
          (readSchedule.push _).expects(ReadScheduleItem(file, startPos=0, endPos=file.length, file.lastModified))
        }
      }
      
      val readScheduler = new ReadScheduler(logFile, defaultRotationPattern, readPersistence, readSchedule)
      readScheduler.fileModified()
    }
  }
}
