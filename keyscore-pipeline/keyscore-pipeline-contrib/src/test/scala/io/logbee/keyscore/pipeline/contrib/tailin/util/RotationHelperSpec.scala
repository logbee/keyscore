package io.logbee.keyscore.pipeline.contrib.tailin.util

import org.scalatest.FreeSpec
import org.scalatest.Matchers

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
@RunWith(classOf[JUnitRunner])
class RotationHelperSpec extends RotateFilesSetup with Matchers {
  
  "RotationHelper should" - {
    "retrieve the list of files it has to read from,which should contain" - {
      "only the baseFile" - {
        "if it got passed null as rotationPattern" in new LogFile {
          
          val rotationPattern = null
          
          val rotationFiles = RotationHelper.getFilesToRead(logFile, rotationPattern, 0L)
          
          rotationFiles.length shouldBe 1
          rotationFiles(0) shouldBe logFile
        }
        
        "if it got passed an empty rotationPattern" in new LogFile {
          
          val rotationPattern = ""
          
          val rotationFiles = RotationHelper.getFilesToRead(logFile, rotationPattern, 0L)
          
          rotationFiles.length shouldBe 1
          rotationFiles(0) shouldBe logFile
        }
      }
      
      "all files matching the rotationPattern," - {
        "if it got passed a non-empty rotationPattern" in new RotateFiles {
          
          val rotationPattern = logFile.getName + ".[1-2]"
          
          val rotationFiles = RotationHelper.getFilesToRead(logFile, rotationPattern, 0L)
          
          rotationFiles should contain allOf (logFile, logFile1, logFile2)
        }
      }
      
      "all files matching the rotationPattern that are newer than the previousReadTimestamp," - {
        "if it got passed a non-empty rotationPattern and a previousReadTimestamp" in new RotateFiles {
          
          val rotationPattern = logFile.getName + ".[1-5]"
          
          val rotationFiles = RotationHelper.getFilesToRead(logFile, rotationPattern, previousReadTimestamp)
          
          rotationFiles should contain allOf (logFile, logFile1, logFile2, logFile3_ModifiedAfterPreviousReadTimestamp)
          rotationFiles should not contain logFile4_ModifiedBeforePreviousReadTimestamp
        }
      }
    }
  }
}
