package io.logbee.keyscore.pipeline.contrib.tailin.util

import org.scalatest.FreeSpec
import org.scalatest.Matchers

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import io.logbee.keyscore.pipeline.contrib.tailin.read.FileReadRecord

@RunWith(classOf[JUnitRunner])
class RotationHelperSpec extends RotateFilesSetup with Matchers {
  
  
  //TODO test what happens if null is passed as previousReadRecord
  val noPreviousReadRecord = new FileReadRecord(previousReadPosition=0, previousReadTimestamp=0, newerFilesWithSharedLastModified=0)
  
  "RotationHelper should" - {
    "retrieve the list of files it has to read from, which should contain" - {
      "only the baseFile" - {
        "if it got passed null as rotationPattern" in new LogFile {
          
          val rotationPattern = null
          
          val rotationFiles = RotationHelper.getRotationFilesToRead(logFile, rotationPattern, noPreviousReadRecord)
          
          rotationFiles.length shouldBe 1
          rotationFiles(0) shouldBe logFile
        }
        
        "if it got passed an empty rotationPattern" in new LogFile {
          
          val rotationPattern = ""
          
          val rotationFiles = RotationHelper.getRotationFilesToRead(logFile, rotationPattern, noPreviousReadRecord)
          
          rotationFiles.length shouldBe 1
          rotationFiles(0) shouldBe logFile
        }
      }
      
      "all files matching the rotationPattern," - {
        "if it got passed a non-empty rotationPattern" in new RotateFiles {
          
          val rotationPattern = logFile.getName + ".[1-2]"
          
          val rotationFiles = RotationHelper.getRotationFilesToRead(logFile, rotationPattern, noPreviousReadRecord)
          
          rotationFiles should contain allOf (logFile, logFile1, logFile2)
        }
      }
      
      
      "all files matching the rotationPattern that are newer than the previousReadTimestamp," - {
        "if it got passed a non-empty rotationPattern and a previousReadTimestamp" in new RotateFiles {
          
          val rotationPattern = logFile.getName + ".[1-5]"
          
          val rotationFiles = RotationHelper.getRotationFilesToRead(logFile, rotationPattern, new FileReadRecord(previousReadPosition=0, previousReadTimestamp, newerFilesWithSharedLastModified=0))
          
          rotationFiles should contain allOf (logFile, logFile1, logFile2, logFile3_ModifiedAfterPreviousReadTimestamp)
          rotationFiles should not contain logFile4_ModifiedBeforePreviousReadTimestamp
        }
      }
    }
  }
  
  "we have files with shared lastModified-timestamp and" - { //TODO adjust these test descriptions and put them in a proper place in the test tree
    
    "no files were deleted (simplest case)" in
    new RotateFiles {
      
      val sharedLastModified = 123456789
      
      logFile4_ModifiedBeforePreviousReadTimestamp.setLastModified(sharedLastModified)
      logFile3_ModifiedAfterPreviousReadTimestamp.setLastModified(sharedLastModified)
      logFile2.setLastModified(sharedLastModified)
      
      
      val previousReadRecord = new FileReadRecord(previousReadPosition=0,
                                                  previousReadTimestamp=logFile3_ModifiedAfterPreviousReadTimestamp.lastModified, //==sharedLastModified (except that it may be rounded down, because the filesystem has a lower resolution for lastModified-timestamps)
                                                  newerFilesWithSharedLastModified=1)
      
      val rotationFiles = RotationHelper.getRotationFilesToRead(logFile, defaultRotationPattern, previousReadRecord)
      
      
      rotationFiles should contain allOf (logFile3_ModifiedAfterPreviousReadTimestamp, logFile2, logFile1, logFile)
      rotationFiles should not contain logFile4_ModifiedBeforePreviousReadTimestamp
    }
    
    
    "1 file was deleted" in
    new RotateFiles {
      //TODO
    }
    
    
    "less files than the number of newerFilesWithSharedLastModified would suggest" in
    new RotateFiles {
      //TODO
    }
    
    
    "no files with the shared lastModified-timestamp left" in //also do such a test without shared-lastModified-timestamp
    new RotateFiles {
      //TODO
    }
  }
}
