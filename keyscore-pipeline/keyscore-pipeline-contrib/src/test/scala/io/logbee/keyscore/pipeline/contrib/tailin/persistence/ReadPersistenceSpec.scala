package io.logbee.keyscore.pipeline.contrib.tailin.persistence

import java.nio.charset.StandardCharsets

import io.logbee.keyscore.pipeline.contrib.tailin.file.OpenFileHandle
import io.logbee.keyscore.pipeline.contrib.tailin.read.FileReader.FileReadRecord
import io.logbee.keyscore.pipeline.contrib.tailin.util.TestUtil.OpenableMockFileHandle
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FreeSpec, Matchers}
import org.scalatestplus.junit.JUnitRunner

import scala.reflect.runtime.universe.{TypeTag, typeTag}

@RunWith(classOf[JUnitRunner])
class ReadPersistenceSpec extends FreeSpec with Matchers with MockFactory {

  trait ReadPersistenceSetup {
    val completedPersistence = mock[PersistenceContext[String, FileReadRecord]]
    val committedPersistence = mock[PersistenceContext[String, FileReadRecord]]
    
    val readPersistence = new ReadPersistence(completedPersistence, committedPersistence)
    
    implicit val charset = StandardCharsets.UTF_8

    val fileReadRecord = FileReadRecord(previousReadPosition=1, previousReadTimestamp=2, newerFilesWithSharedLastModified=0)

    val openFileHandle = mock[OpenFileHandle]
    val absolutePath = "/tmp/.testFile"
    val fileHandle = new OpenableMockFileHandle(absolutePath, openFileHandle)
  }
  
  
  "A ReadPersistence should" - {
    
    "return the last read entry" - {
      
      "when no read has been completed or committed" in
      new ReadPersistenceSetup {
        inSequence {
          (completedPersistence.load (_: String))
            .expects(absolutePath)
            .returning(None)
          
          (committedPersistence.load (_: String))
            .expects(absolutePath)
            .returning(None)
        }
        
        readPersistence.getCompletedRead(fileHandle) shouldBe FileReadRecord(previousReadPosition=0, previousReadTimestamp=0, newerFilesWithSharedLastModified=0)
      }
      
      
      "when no read has been completed, but one has been committed" in
      new ReadPersistenceSetup {
        inSequence {
          (completedPersistence.load (_: String))
            .expects(absolutePath)
            .returning(None)
          
          (committedPersistence.load (_: String))
            .expects(absolutePath)
            .returning(Some(fileReadRecord))
        }
        
        readPersistence.getCompletedRead(fileHandle) shouldBe fileReadRecord
      }
      
      
      "when a read has been completed" in
      new ReadPersistenceSetup {
        (completedPersistence.load (_: String))
          .expects(absolutePath)
          .returning(Some(fileReadRecord))
        
        readPersistence.getCompletedRead(fileHandle) shouldBe fileReadRecord
      }
    }
    
    
    "store a completed read" in
    new ReadPersistenceSetup {
      (completedPersistence.load (_: String))
          .expects(absolutePath)
          .returning(None)
      
      (completedPersistence.store _)
        .expects(absolutePath, fileReadRecord)
      
      readPersistence.completeRead(fileHandle, fileReadRecord)
    }
    
    
    "store a committed read" in
    new ReadPersistenceSetup {
      inSequence {
        (completedPersistence.load (_: String))
          .expects(absolutePath)
          .returning(Some(fileReadRecord))
        
        (completedPersistence.store _)
          .expects(absolutePath, fileReadRecord)

        (committedPersistence.load (_: String))
          .expects(absolutePath)
          .returning(None)
        
        (committedPersistence.store _)
          .expects(absolutePath, fileReadRecord)
      }
      
      readPersistence.commitRead(fileHandle, fileReadRecord)
    }
    
    
    "store a committed read and update the list of completed reads, if that for some reason has a less up-to-date entry" in
    new ReadPersistenceSetup {
      inSequence {
        (completedPersistence.load (_: String))
          .expects(absolutePath)
          .returning(None)
        
        (completedPersistence.store _)
          .expects(absolutePath, fileReadRecord)
        
        (committedPersistence.load (_: String))
          .expects(absolutePath)
          .returning(None)
        
        (committedPersistence.store _)
          .expects(absolutePath, fileReadRecord)
      }
      
      readPersistence.commitRead(fileHandle, fileReadRecord)
    }
    
    
    "discard a completed read which is older than the current entry" in
    new ReadPersistenceSetup {
      inSequence {
        (completedPersistence.load (_: String))
          .expects(absolutePath)
          .returning(Some(fileReadRecord.copy(previousReadTimestamp=fileReadRecord.previousReadTimestamp+1)))
        
        (completedPersistence.store _)
          .expects(*, *)
          .never
      }
      
      readPersistence.completeRead(fileHandle, fileReadRecord)
    }
    
    
    "discard a committed read which is older than the current entry" in
    new ReadPersistenceSetup {
      inSequence {
        (completedPersistence.load (_: String))
          .expects(absolutePath)
          .returning(Some(fileReadRecord.copy(previousReadTimestamp=fileReadRecord.previousReadTimestamp+1))) //this does not need to be updated
        
        (completedPersistence.store _)
          .expects(*, *)
          .never
        
        (committedPersistence.load (_: String))
          .expects(absolutePath)
          .returning(Some(fileReadRecord.copy(previousReadTimestamp=fileReadRecord.previousReadTimestamp+1))) //this does not need to be updated
        
        (committedPersistence.store _)
          .expects(*, *)
          .never
      }
      
      readPersistence.commitRead(fileHandle, fileReadRecord)
    }
    
    
    "discard a committed read which is older than the current entry, while updating the out-of-date completed read persistence" in
    new ReadPersistenceSetup {
      inSequence {
        (completedPersistence.load (_: String))
          .expects(absolutePath)
          .returning(Some(fileReadRecord.copy(previousReadTimestamp=fileReadRecord.previousReadTimestamp-1))) //this needs to be updated
        
        (completedPersistence.store _)
          .expects(absolutePath, fileReadRecord)
        
        (committedPersistence.load (_: String))
          .expects(absolutePath)
          .returning(Some(fileReadRecord.copy(previousReadTimestamp=fileReadRecord.previousReadTimestamp+1))) //this does not need to be updated
        
        (committedPersistence.store _)
          .expects(*, *)
          .never
      }
        
      readPersistence.commitRead(fileHandle, fileReadRecord)
    }
  }
}
