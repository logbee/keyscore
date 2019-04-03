package io.logbee.keyscore.pipeline.contrib.tailin.util

import java.nio.file.Path

import org.scalatest.BeforeAndAfter
import org.scalatest.FreeSpec
import java.nio.file.Files

import io.logbee.keyscore.pipeline.contrib.tailin.read.FileReadData
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.nio.file.StandardOpenOption

@RunWith(classOf[JUnitRunner])
class RotateFilesSetup extends FreeSpec with BeforeAndAfter {
  
  
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
    
    val previousReadPosition = logFile3Data.length / 2
    val previousReadTimestamp = logFile4_ModifiedBeforePreviousReadTimestamp.lastModified + 1
    
    
    def rotate() {
        logFile3_ModifiedAfterPreviousReadTimestamp.renameTo(logFile4_ModifiedBeforePreviousReadTimestamp)
        logFile2.renameTo(logFile3_ModifiedAfterPreviousReadTimestamp)
        logFile1.renameTo(logFile2)
        logFile.renameTo(logFile1)
        
        Thread.sleep(1000) //TODO this is currently necessary to offset the lastModified-timestamps -> make this nicer by manually setting the lastModified-timestamps
        
        logFile.createNewFile()
        TestUtil.waitForFileToExist(logFile)
        TestUtil.writeStringToFile(logFile, "Rotated", StandardOpenOption.APPEND)
    }
  }
  
  
  
  
  def calledBackDataIsSimilarTo(expected: FileReadData): FileReadData => Boolean = {
    actual: FileReadData => {
      expected.string == actual.string &&
      Option(expected.baseFile).equals(Option(actual.baseFile)) &&
      Option(expected.physicalFile).equals(Option(actual.physicalFile)) &&
      expected.readEndPos == actual.readEndPos &&
      expected.writeTimestamp == actual.writeTimestamp &&
      actual.readTimestamp >= actual.writeTimestamp &&
      actual.readTimestamp <= System.currentTimeMillis &&
      expected.newerFilesWithSharedLastModified == actual.newerFilesWithSharedLastModified
    }
  }
}
