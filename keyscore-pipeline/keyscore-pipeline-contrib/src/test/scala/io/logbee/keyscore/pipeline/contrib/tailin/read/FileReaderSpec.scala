package io.logbee.keyscore.pipeline.contrib.tailin.read

import java.nio.charset.StandardCharsets
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

import scala.io.Source

import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfter
import org.scalatest.FreeSpec
import org.scalatest.Matchers
import org.scalatest.ParallelTestExecution

import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadScheduleItem
import io.logbee.keyscore.pipeline.contrib.tailin.util.TestUtil
import org.scalatest.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class FileReaderSpec extends FreeSpec with BeforeAndAfter with Matchers with MockFactory with ParallelTestExecution {

  var watchDir: Path = null

  before {
    watchDir = Files.createTempDirectory("watchTest")

    TestUtil.waitForFileToExist(watchDir.toFile)
  }

  after {
    TestUtil.recursivelyDelete(watchDir)
  }

  
  val defaultBufferSize = 1024
  val defaultCharset = StandardCharsets.UTF_8
  val defaultReadMode = ReadMode.LINE
  
  
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
  }

  
  
  "A FileReader" - {
    "should retrieve the list of files it has to read from," - {
      "which should contain" - {
        "only the FileReader's file itself," - {
          "if it got passed null as rotationPattern" in new LogFile {
            
            val rotationPattern = null
            
            val rotationFiles = FileReader.getFilesToRead(logFile, rotationPattern, 0L)
            
            rotationFiles.length shouldBe 1
            rotationFiles(0) shouldBe logFile
          }
          
          "if it got passed an empty rotationPattern" in new LogFile {
            
            val rotationPattern = ""
            
            val rotationFiles = FileReader.getFilesToRead(logFile, rotationPattern, 0L)
            
            rotationFiles.length shouldBe 1
            rotationFiles(0) shouldBe logFile
          }
        }
        
        "all files matching the rotationPattern," - {
          "if it got passed a non-empty rotationPattern" in new RotateFiles {
            
            val rotationPattern = logFile.getName + ".[1-2]"
            
            val rotationFiles = FileReader.getFilesToRead(logFile, rotationPattern, 0L)
            
            rotationFiles should contain allOf (logFile, logFile1, logFile2)
          }
        }
        
        "all files matching the rotationPattern that are newer than the previousReadTimestamp," - {
          "if it got passed a non-empty rotationPattern and a previousReadTimestamp" in new RotateFiles {
            
            val rotationPattern = logFile.getName + ".[1-5]"
            
            val rotationFiles = FileReader.getFilesToRead(logFile, rotationPattern, previousReadTimestamp)
            
            rotationFiles should contain allOf (logFile, logFile1, logFile2, logFile3_ModifiedAfterPreviousReadTimestamp)
            rotationFiles should not contain logFile4_ModifiedBeforePreviousReadTimestamp
          }
        }
      }
    }
    
    
    "should read the contents of" - {
      "its file" - {
        "line by line, if line-wise reading is active," - {
          
          val readMode = ReadMode.LINE
          
          "if the file contains only one line of text" in new LogFile {
            
            val fileReader = new FileReader(logFile, null, defaultBufferSize, defaultCharset, readMode)
            
            val line1 = "Line1"
            TestUtil.writeStringToFile(logFile, line1, StandardOpenOption.TRUNCATE_EXISTING)
  
            val mockCallback = mockFunction[String, Unit]
  
            mockCallback expects line1
            
            fileReader.fileModified(mockCallback, ReadScheduleItem(logFile, 0, logFile.length, logFile.lastModified))
          }
          
          "if the file contains only one line of text with a newline at the end" in new LogFile {
            
            val fileReader = new FileReader(logFile, null, defaultBufferSize, defaultCharset, readMode)
            
            val line1 = "Line1"
            val text = line1 + "\n"
            TestUtil.writeStringToFile(logFile, text, StandardOpenOption.TRUNCATE_EXISTING)
  
            val mockCallback = mockFunction[String, Unit]
  
            mockCallback expects line1
            
            fileReader.fileModified(mockCallback, ReadScheduleItem(logFile, 0, logFile.length, logFile.lastModified))
          }
          
          "if the file contains multiple lines of text" in new LogFile {
            
            val fileReader = new FileReader(logFile, null, defaultBufferSize, defaultCharset, readMode)
            
            val line1 = "Line1"
            val line2 = "Line2"
            val line3 = "Line3"
            val text = line1 + "\n" + line2 + "\n" + line3
            TestUtil.writeStringToFile(logFile, text, StandardOpenOption.TRUNCATE_EXISTING)
            
            
            val mockCallback = mockFunction[String, Unit]
  
            inSequence {
              mockCallback expects line1
              mockCallback expects line2
              mockCallback expects line3
            }
            fileReader.fileModified(mockCallback, ReadScheduleItem(logFile, 0, logFile.length, logFile.lastModified))
          }
          
          "if the file contains multiple newline-characters directly following each other"  in new LogFile {
            
            val fileReader = new FileReader(logFile, null, defaultBufferSize, defaultCharset, readMode)
            
            val line1 = "Line1"
            val line3 = "Line3"
            val text = line1 + "\n\n" + line3
            TestUtil.writeStringToFile(logFile, text, StandardOpenOption.TRUNCATE_EXISTING)
            
            
            val mockCallback = mockFunction[String, Unit]
  
            inSequence {
              mockCallback expects line1
              mockCallback expects line3
            }
            fileReader.fileModified(mockCallback, ReadScheduleItem(logFile, 0, logFile.length, logFile.lastModified))
          }
          
          "if the file contains a Windows newline \\r\\n" in new LogFile {
            
            val fileReader = new FileReader(logFile, null, defaultBufferSize, defaultCharset, readMode)
            
            val line1 = "Line1"
            val line2 = "Line2"
            val text = line1 + "\r\n" + line2
            TestUtil.writeStringToFile(logFile, text, StandardOpenOption.TRUNCATE_EXISTING)
            
            
            val mockCallback = mockFunction[String, Unit]
  
            inSequence {
              mockCallback expects line1
              mockCallback expects line2
            }
            fileReader.fileModified(mockCallback, ReadScheduleItem(logFile, 0, logFile.length, logFile.lastModified))
          }
          
          "if the contents of the file are longer than one buffer's length" in new LogFile {
            
            val fileReader = new FileReader(logFile, null, defaultBufferSize, defaultCharset, readMode)
            
            val line1 = new String(new Array[Byte](512))
            val line2 = new String(new Array[Byte](52))
            val line3 = new String(new Array[Byte](1023))
            val text = line1 + "\n" + line2 + "\n" + line3
            
            TestUtil.writeStringToFile(logFile, text, StandardOpenOption.TRUNCATE_EXISTING)
            
            
            val mockCallback = mockFunction[String, Unit]
  
            inSequence {
              mockCallback expects line1
              mockCallback expects line2
              mockCallback expects line3
            }
            
              
            fileReader.fileModified(mockCallback, ReadScheduleItem(logFile, 0, logFile.length, logFile.lastModified))
          }
          
          "if the file contains a line that is longer than the buffer" in new LogFile {
            
            val fileReader = new FileReader(logFile, null, defaultBufferSize, defaultCharset, readMode)
            
            val line1 = new String(new Array[Byte](12))
        		val line2 = new String(new Array[Byte](34567))
        		val line3 = new String(new Array[Byte](89))
            val text = line1 + "\n" + line2 + "\n" + line3
            
            TestUtil.writeStringToFile(logFile, text, StandardOpenOption.TRUNCATE_EXISTING)
            
            
            val mockCallback = mockFunction[String, Unit]
  
            inSequence {
              mockCallback expects line1
              mockCallback expects line2
              mockCallback expects line3
            }
            
            fileReader.fileModified(mockCallback, ReadScheduleItem(logFile, 0, logFile.length, logFile.lastModified))
          }
        }
        
        "in whole from the previous reading position, if file-wise reading is active" in new LogFile {
          
          val readMode = ReadMode.FILE
          val fileReader = new FileReader(logFile, null, defaultBufferSize, defaultCharset, readMode)
          
          
          val line1 = "Line1"
          val line2 = "Line2"
          val line3 = "Line3"
          
          val text = line1 + "\n" + line2 + "\n" + line3
          
          TestUtil.writeStringToFile(logFile, text, StandardOpenOption.TRUNCATE_EXISTING)
          
          
          val mockCallback = mockFunction[String, Unit]
          
          mockCallback expects text
          fileReader.fileModified(mockCallback, ReadScheduleItem(logFile, 0, logFile.length, logFile.lastModified))
        }
      }
      
      
      "its file as well as its rotated files, if rotation is active" - {
        "line by line, if line-wise reading is active" in new RotateFiles {
          
          val readMode = ReadMode.LINE
          val fileReader = new FileReader(logFile, defaultRotationPattern, defaultBufferSize, defaultCharset, readMode)
          
          
          //write something additional to the file to test line-wise reading
          val tmp_logFile2_lastModified = logFile2.lastModified
          val logFile2_AdditionalData = "test"
          TestUtil.writeStringToFile(logFile2, "\n" + logFile2_AdditionalData, StandardOpenOption.APPEND)
          logFile2.setLastModified(tmp_logFile2_lastModified) //reset the lastModified-time to what it was before writing, to keep the correct order
          
          
          val mockCallback = mockFunction[String, Unit]
  
          inSequence {
            mockCallback expects logFile3Data.substring(previousReadPosition)
            mockCallback expects logFile2Data
            mockCallback expects logFile2_AdditionalData
            mockCallback expects logFile1Data
            mockCallback expects logFileData
          }
          
          fileReader.fileModified(mockCallback, ReadScheduleItem(logFile, previousReadPosition, logFile3_ModifiedAfterPreviousReadTimestamp.length, logFile3_ModifiedAfterPreviousReadTimestamp.lastModified))
          fileReader.fileModified(mockCallback, ReadScheduleItem(logFile, 0, logFile2.length, logFile2.lastModified))
          fileReader.fileModified(mockCallback, ReadScheduleItem(logFile, 0, logFile1.length, logFile1.lastModified))
          fileReader.fileModified(mockCallback, ReadScheduleItem(logFile, 0, logFile.length, logFile.lastModified))
        }
        
        "file by file, if file-wise reading is active" in new RotateFiles {
          
          val readMode = ReadMode.FILE
          val fileReader = new FileReader(logFile, defaultRotationPattern, defaultBufferSize, defaultCharset, readMode)
          
          
          val mockCallback = mockFunction[String, Unit]
  
          inSequence {
            mockCallback expects logFile3Data.substring(previousReadPosition)
            mockCallback expects logFile2Data
            mockCallback expects logFile1Data
            mockCallback expects logFileData
          }
          
          fileReader.fileModified(mockCallback, ReadScheduleItem(logFile, previousReadPosition, logFile3_ModifiedAfterPreviousReadTimestamp.length, logFile3_ModifiedAfterPreviousReadTimestamp.lastModified))
          fileReader.fileModified(mockCallback, ReadScheduleItem(logFile, 0, logFile2.length, logFile2.lastModified))
          fileReader.fileModified(mockCallback, ReadScheduleItem(logFile, 0, logFile1.length, logFile1.lastModified))
          fileReader.fileModified(mockCallback, ReadScheduleItem(logFile, 0, logFile.length, logFile.lastModified))
        }
      }
      
      
      //TODO this doesn't work yet, because lastModified-time of rotatedFiles is identical
      "multiple rotated files after resuming reading" ignore {
        
        case class TestCase(bufferSize: Int, description: String)
        
        val bufferSizesToTest = Seq(
                                    TestCase(  10, "shorter than one line of text"),
                                    TestCase( 1024, "longer than one line of text"),
                                    TestCase(999999, "longer than the entire text"),
                                   )
        
        
        bufferSizesToTest.foreach { case TestCase(bufferSize, description) =>
          
          "with buffer size " + bufferSize + " bytes, which is " + description in new LogFile {
            
            TestUtil.writeLogToFileWithRotation(logFile, numberOfLines=1000, rotatePattern = logFile.getName + ".%i")

            
            val rotateMatcher = FileSystems.getDefault.getPathMatcher("glob:" + logFile.getParent + "/" + defaultRotationPattern)
            
            val files = (logFile.getParentFile.listFiles
              .filter(file => rotateMatcher.matches(file.toPath)) :+ logFile)
              .sortBy(file => file.lastModified)
            
            var contents = files.foldLeft("")((content, file) => content + Source.fromFile(file).mkString)
            
            if (defaultReadMode == ReadMode.LINE) { //we don't call back newlines in line-wise reading
              contents = contents.replace("\n", "")
            }
            
            
            var calledBackString = "" 
            val fileReader = new FileReader(watchedFile=logFile, defaultRotationPattern, bufferSize, defaultCharset, defaultReadMode)
            
            
            //schedule a read for every rotation file
            val filesToRead = FileReader.getFilesToRead(logFile, defaultRotationPattern, previousReadTimestamp=0)
            filesToRead.foreach { file =>
              println(file + " " + file.lastModified)
              fileReader.fileModified(string => calledBackString += string, ReadScheduleItem(logFile, 0, file.length, file.lastModified))
            }
            
            
            
            calledBackString shouldEqual contents
          }
        }
      }
    }
    
    //TODO this might need to be tested in FileReaderManager and probably also find some way to tell FileReaderManager the endPos 
//    "should persist a FileReadRecord" - {
//      "for one log file" - {
//        "without rotated files" in new PersistenceContextWithoutTimestamp {
//          
//          val fileReader = new FileReader(logFile, null, defaultBufferSize, defaultCharset, defaultReadMode)
//          
//          fileReader.fileModified((_: String) => (), ReadScheduleItem(logFile, 0, logFile.length, logFile.lastModified))
//        }
//
//        "with rotated files" in new PersistenceContextWithTimestamp {
//          //check that store() is called only once and with the correct parameters, even with multiple files (as we only want the read position persisted for the last file, not for the intermediate files)
//          
//          val fileReader = new FileReader(logFile, defaultRotationPattern, defaultBufferSize, defaultCharset, defaultReadMode)
//          
//          fileReader.fileModified((_: String) => (), ReadScheduleItem(logFile, 0, logFile.length, logFile.lastModified))
//        }
//      }
//    }
  }
}
