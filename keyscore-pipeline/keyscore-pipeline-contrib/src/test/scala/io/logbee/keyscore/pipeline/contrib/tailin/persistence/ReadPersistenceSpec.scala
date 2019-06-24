package io.logbee.keyscore.pipeline.contrib.tailin.persistence

import org.scalatest.FreeSpec
import org.scalatest.Matchers
import org.scalamock.scalatest.MockFactory
import io.logbee.keyscore.pipeline.contrib.tailin.read.FileReadRecord
import scala.reflect.runtime.universe._
import java.io.File


import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import io.logbee.keyscore.pipeline.contrib.tailin.file.local.LocalFile

@RunWith(classOf[JUnitRunner])
class ReadPersistenceSpec extends FreeSpec with Matchers with MockFactory {
  
  
  trait ReadPersistenceSetup {
    val completedPersistence = mock[PersistenceContext]
    val committedPersistence = mock[PersistenceContext]
    
    val readPersistence = new ReadPersistence(completedPersistence, committedPersistence)
    
    val file = new LocalFile(new File(".testFile"))
    val fileReadRecord = FileReadRecord(previousReadPosition=1, previousReadTimestamp=2, newerFilesWithSharedLastModified=0)
  }
  
  
  "A ReadPersistence should" - {
    
    "return the last read entry" - {
      
      "when no read has been completed or committed" in
      new ReadPersistenceSetup {
        
        inSequence {
          (completedPersistence.load[FileReadRecord] (_: String)(_: TypeTag[FileReadRecord]))
            .expects(file.absolutePath, typeTag[FileReadRecord])
            .returning(None)
          
          (committedPersistence.load[FileReadRecord] (_: String)(_: TypeTag[FileReadRecord]))
            .expects(file.absolutePath, typeTag[FileReadRecord])
            .returning(None)
        }
        
        readPersistence.getCompletedRead(file) shouldBe FileReadRecord(previousReadPosition=0, previousReadTimestamp=0, newerFilesWithSharedLastModified=0)
      }
      
      
      "when no read has been completed, but one has been committed" in
      new ReadPersistenceSetup {
        
        inSequence {
          (completedPersistence.load[FileReadRecord] (_: String)(_: TypeTag[FileReadRecord]))
            .expects(file.absolutePath, typeTag[FileReadRecord])
            .returning(None)
          
          (committedPersistence.load[FileReadRecord] (_: String)(_: TypeTag[FileReadRecord]))
            .expects(file.absolutePath, typeTag[FileReadRecord])
            .returning(Some(fileReadRecord))
        }
        
        readPersistence.getCompletedRead(file) shouldBe fileReadRecord
      }
      
      
      "when a read has been completed" in
      new ReadPersistenceSetup {
        
        (completedPersistence.load[FileReadRecord] (_: String)(_: TypeTag[FileReadRecord]))
          .expects(file.absolutePath, typeTag[FileReadRecord])
          .returning(Some(fileReadRecord))
        
        readPersistence.getCompletedRead(file) shouldBe fileReadRecord
      }
    }
    
    
    "store a completed read" in
    new ReadPersistenceSetup {
      
      (completedPersistence.load[FileReadRecord] (_: String)(_: TypeTag[FileReadRecord]))
          .expects(file.absolutePath, typeTag[FileReadRecord])
          .returning(None)
      
      (completedPersistence.store _)
        .expects(file.absolutePath, fileReadRecord)
      
      readPersistence.completeRead(file, fileReadRecord)
    }
    
    
    "store a committed read" in
    new ReadPersistenceSetup {
      
      inSequence {
        (completedPersistence.load[FileReadRecord] (_: String)(_: TypeTag[FileReadRecord]))
          .expects(file.absolutePath, typeTag[FileReadRecord])
          .returning(Some(fileReadRecord))
        
        (committedPersistence.load[FileReadRecord] (_: String)(_: TypeTag[FileReadRecord]))
          .expects(file.absolutePath, typeTag[FileReadRecord])
          .returning(None)
        
        (committedPersistence.store _)
          .expects(file.absolutePath, fileReadRecord)
      }
      
      readPersistence.commitRead(file, fileReadRecord)
    }
    
    
    "store a committed read and update the list of completed reads, if that for some reason has a less up-to-date entry" in
    new ReadPersistenceSetup {
      
      inSequence {
        (completedPersistence.load[FileReadRecord] (_: String)(_: TypeTag[FileReadRecord]))
          .expects(file.absolutePath, typeTag[FileReadRecord])
          .returning(None)
        
        (completedPersistence.store _)
          .expects(file.absolutePath, fileReadRecord)
        
        (committedPersistence.load[FileReadRecord] (_: String)(_: TypeTag[FileReadRecord]))
          .expects(file.absolutePath, typeTag[FileReadRecord])
          .returning(None)
        
        (committedPersistence.store _)
          .expects(file.absolutePath, fileReadRecord)
      }
      
      readPersistence.commitRead(file, fileReadRecord)
    }
    
    
    "discard a completed read which is older than the current entry" in
    new ReadPersistenceSetup {
      
      inSequence {
        (completedPersistence.load[FileReadRecord] (_: String)(_: TypeTag[FileReadRecord]))
          .expects(file.absolutePath, typeTag[FileReadRecord])
          .returning(Some(fileReadRecord.copy(previousReadTimestamp=fileReadRecord.previousReadTimestamp+1)))
        
        (completedPersistence.store _)
          .expects(*, *)
          .never
      }
      
      readPersistence.completeRead(file, fileReadRecord)
    }
    
    
    "discard a committed read which is older than the current entry" in
    new ReadPersistenceSetup {
      
      inSequence {
        (completedPersistence.load[FileReadRecord] (_: String)(_: TypeTag[FileReadRecord]))
          .expects(file.absolutePath, typeTag[FileReadRecord])
          .returning(Some(fileReadRecord.copy(previousReadTimestamp=fileReadRecord.previousReadTimestamp+1))) //this does not need to be updated
        
        (completedPersistence.store _)
          .expects(*, *)
          .never
        
        (committedPersistence.load[FileReadRecord] (_: String)(_: TypeTag[FileReadRecord]))
          .expects(file.absolutePath, typeTag[FileReadRecord])
          .returning(Some(fileReadRecord.copy(previousReadTimestamp=fileReadRecord.previousReadTimestamp+1))) //this does not need to be updated
        
        (committedPersistence.store _)
          .expects(*, *)
          .never
      }
      
      readPersistence.commitRead(file, fileReadRecord)
    }
    
    
    "discard a committed read which is older than the current entry, while updating the out-of-date completed read persistence" in
    new ReadPersistenceSetup {
      
      inSequence {
        (completedPersistence.load[FileReadRecord] (_: String)(_: TypeTag[FileReadRecord]))
          .expects(file.absolutePath, typeTag[FileReadRecord])
          .returning(Some(fileReadRecord.copy(previousReadTimestamp=fileReadRecord.previousReadTimestamp-1))) //this needs to be updated
        
        (completedPersistence.store _)
          .expects(file.absolutePath, fileReadRecord)
        
        (committedPersistence.load[FileReadRecord] (_: String)(_: TypeTag[FileReadRecord]))
          .expects(file.absolutePath, typeTag[FileReadRecord])
          .returning(Some(fileReadRecord.copy(previousReadTimestamp=fileReadRecord.previousReadTimestamp+1))) //this does not need to be updated
        
        (committedPersistence.store _)
          .expects(*, *)
          .never
      }
        
      readPersistence.commitRead(file, fileReadRecord)
    }
  }
}
