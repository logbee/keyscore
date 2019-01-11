package io.logbee.keyscore.pipeline.contrib.tailin.file

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, StandardOpenOption}

import io.logbee.keyscore.pipeline.contrib.tailin.persistence.FilePersistenceContext
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfter, FreeSpec, Matchers, ParallelTestExecution}
import org.scalatest.junit.JUnitRunner

import scala.reflect.runtime.universe._
import io.logbee.keyscore.pipeline.contrib.tailin.util.TestUtil
import java.nio.file.Paths
import java.nio.file.FileSystems
import scala.io.Source


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
    logFile2.setLastModified(logFileModified - 1000 * 2)
    logFile1.setLastModified(logFileModified - 1000 * 1)
    logFile.setLastModified(logFileModified)
  }

  
  trait PersistenceContextWithoutTimestamp extends LogFile {
    
    val persistenceContextWithoutTimestamp = mock[FilePersistenceContext]
    (persistenceContextWithoutTimestamp.load[RotationRecord] (_: String)(_: TypeTag[RotationRecord]))
      .expects(logFile.getAbsolutePath, typeTag[RotationRecord])
      .returning(Some(RotationRecord(0, 0)))
  }
  

  trait PersistenceContextWithTimestamp extends RotateFiles {
    
    val logFile4Data = "Log_File_4_4444 "
    val logFile4_ModifiedBeforePreviousReadTimestamp = TestUtil.createFile(watchDir, "log.txt.4", logFile4Data)
    logFile4_ModifiedBeforePreviousReadTimestamp.setLastModified(logFileModified - 1000 * 4)
    
    val logFile3Data = "Log_File_3_333 "
    val logFile3_ModifiedAfterPreviousReadTimestamp = TestUtil.createFile(watchDir, "log.txt.3", logFile3Data)
    logFile3_ModifiedAfterPreviousReadTimestamp.setLastModified(logFileModified - 1000 * 3)
    
    val previousReadPosition = logFile3Data.length / 2
    val previousReadTimestamp = logFile4_ModifiedBeforePreviousReadTimestamp.lastModified + 1
    val persistenceContextWithTimestamp: FilePersistenceContext = mock[FilePersistenceContext]
    (persistenceContextWithTimestamp.load[RotationRecord] (_: String)(_: TypeTag[RotationRecord]))
      .expects(logFile.getAbsolutePath, typeTag[RotationRecord])
      .returning(Some(RotationRecord(previousReadPosition, previousReadTimestamp)))
  }
  
  
  
  "A FileReader" - {
    "should retrieve the list of files it has to read from," - {
      "which should contain" - {
        "only the FileReader's file itself," - {
          "if it got passed null as rotationPattern" in new PersistenceContextWithoutTimestamp {
            
            val rotationPattern = null
            
            val fileReader = new FileReader(logFile, rotationPattern, persistenceContextWithoutTimestamp, defaultBufferSize, defaultCharset, defaultReadMode)
            
            val rotationFiles = fileReader.getFilesToRead
            
            rotationFiles.length shouldBe 1
            rotationFiles(0) shouldBe logFile
          }
          
          "if it got passed an empty rotationPattern" in new PersistenceContextWithoutTimestamp {
            
            val rotationPattern = ""
            
            val fileReader = new FileReader(logFile, rotationPattern, persistenceContextWithoutTimestamp, defaultBufferSize, defaultCharset, defaultReadMode)
            
            val rotationFiles = fileReader.getFilesToRead
            
            rotationFiles.length shouldBe 1
            rotationFiles(0) shouldBe logFile
          }
        }
        
        "all files matching the rotationPattern," - {
          "if it got passed a non-empty rotationPattern" in new PersistenceContextWithTimestamp {
            
            val rotationPattern = logFile.getName + ".[1-2]"
            val fileReader = new FileReader(logFile, rotationPattern, persistenceContextWithTimestamp, defaultBufferSize, defaultCharset, defaultReadMode)
            
            
            val rotationFiles = fileReader.getFilesToRead
            
            rotationFiles should contain allOf (logFile, logFile1, logFile2)
          }
        }
        
        "all files matching the rotationPattern that are newer than the previousReadTimestamp," - {
          "if it got passed a non-empty rotationPattern and a previousReadTimestamp" in new PersistenceContextWithTimestamp {
            
            val rotationPattern = logFile.getName + ".[1-5]"
            
            val fileReader = new FileReader(logFile, rotationPattern, persistenceContextWithTimestamp, defaultBufferSize, defaultCharset, defaultReadMode)
            
            val rotationFiles = fileReader.getFilesToRead
            
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
          
          "if the file contains only one line of text" in new PersistenceContextWithoutTimestamp {
            
            val fileReader = new FileReader(logFile, null, persistenceContextWithoutTimestamp, defaultBufferSize, defaultCharset, readMode)
            
            val line1 = "Line1"
            TestUtil.writeStringToFile(logFile, line1, StandardOpenOption.TRUNCATE_EXISTING)
  
            (persistenceContextWithoutTimestamp.store (_: String, _: RotationRecord))
              .expects(logFile.getAbsolutePath, RotationRecord(logFile.length, logFile.lastModified))
  
            val mockCallback = mockFunction[String, Unit]
  
            mockCallback expects line1
            
            fileReader.fileModified(mockCallback)
          }
          
          "if the file contains only one line of text with a newline at the end" in new PersistenceContextWithoutTimestamp {
            
            val fileReader = new FileReader(logFile, null, persistenceContextWithoutTimestamp, defaultBufferSize, defaultCharset, readMode)
            
            val line1 = "Line1"
            val text = line1 + "\n"
            TestUtil.writeStringToFile(logFile, text, StandardOpenOption.TRUNCATE_EXISTING)
  
            (persistenceContextWithoutTimestamp.store (_: String, _: RotationRecord))
              .expects(logFile.getAbsolutePath, RotationRecord(logFile.length, logFile.lastModified))
  
            val mockCallback = mockFunction[String, Unit]
  
            mockCallback expects line1
            
            fileReader.fileModified(mockCallback)
          }
          
          "if the file contains multiple lines of text" in new PersistenceContextWithoutTimestamp {
            
            val fileReader = new FileReader(logFile, null, persistenceContextWithoutTimestamp, defaultBufferSize, defaultCharset, readMode)
            
            val line1 = "Line1"
            val line2 = "Line2"
            val line3 = "Line3"
            val text = line1 + "\n" + line2 + "\n" + line3
            TestUtil.writeStringToFile(logFile, text, StandardOpenOption.TRUNCATE_EXISTING)
            
            
            (persistenceContextWithoutTimestamp.store (_: String, _: RotationRecord))
              .expects(logFile.getAbsolutePath, RotationRecord(logFile.length, logFile.lastModified))
            
            val mockCallback = mockFunction[String, Unit]
  
            inSequence {
              mockCallback expects line1
              mockCallback expects line2
              mockCallback expects line3
            }
            fileReader.fileModified(mockCallback)
          }
          
          "if the file contains multiple newline-characters directly following each other"  in new PersistenceContextWithoutTimestamp {
            
            val fileReader = new FileReader(logFile, null, persistenceContextWithoutTimestamp, defaultBufferSize, defaultCharset, readMode)
            
            val line1 = "Line1"
            val line3 = "Line3"
            val text = line1 + "\n\n" + line3
            TestUtil.writeStringToFile(logFile, text, StandardOpenOption.TRUNCATE_EXISTING)
            
            
            (persistenceContextWithoutTimestamp.store (_: String, _: RotationRecord))
              .expects(logFile.getAbsolutePath, RotationRecord(logFile.length, logFile.lastModified))
            
            val mockCallback = mockFunction[String, Unit]
  
            inSequence {
              mockCallback expects line1
              mockCallback expects line3
            }
            fileReader.fileModified(mockCallback)
          }
          
          "if the file contains a Windows newline \\r\\n" in new PersistenceContextWithoutTimestamp {
            
            val fileReader = new FileReader(logFile, null, persistenceContextWithoutTimestamp, defaultBufferSize, defaultCharset, readMode)
            
            val line1 = "Line1"
            val line2 = "Line2"
            val text = line1 + "\r\n" + line2
            TestUtil.writeStringToFile(logFile, text, StandardOpenOption.TRUNCATE_EXISTING)
            
            
            (persistenceContextWithoutTimestamp.store (_: String, _: RotationRecord))
              .expects(logFile.getAbsolutePath, RotationRecord(logFile.length, logFile.lastModified))
            
            val mockCallback = mockFunction[String, Unit]
  
            inSequence {
              mockCallback expects line1
              mockCallback expects line2
            }
            fileReader.fileModified(mockCallback)
          }
          
          "if the contents of the file are longer than one buffer's length" in new PersistenceContextWithoutTimestamp {
            
            val fileReader = new FileReader(logFile, null, persistenceContextWithoutTimestamp, defaultBufferSize, defaultCharset, readMode)
            
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
            
            (persistenceContextWithoutTimestamp.store (_: String, _: RotationRecord))
              .expects(logFile.getAbsolutePath, RotationRecord(logFile.length, logFile.lastModified))
            
              
            fileReader.fileModified(mockCallback)
          }
          
          "if the file contains a line that is longer than the buffer" in new PersistenceContextWithoutTimestamp {
            
            val fileReader = new FileReader(logFile, null, persistenceContextWithoutTimestamp, defaultBufferSize, defaultCharset, readMode)
            
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
            
            (persistenceContextWithoutTimestamp.store (_: String, _: RotationRecord))
              .expects(logFile.getAbsolutePath, RotationRecord(logFile.length, logFile.lastModified))
            
            fileReader.fileModified(mockCallback)
          }
        }
        
        "in whole from the previous reading position, if file-wise reading is active" in new PersistenceContextWithoutTimestamp {
          
          val readMode = ReadMode.FILE
          val fileReader = new FileReader(logFile, null, persistenceContextWithoutTimestamp, defaultBufferSize, defaultCharset, readMode)
          
          
          val line1 = "Line1"
          val line2 = "Line2"
          val line3 = "Line3"
          
          val text = line1 + "\n" + line2 + "\n" + line3
          
          TestUtil.writeStringToFile(logFile, text, StandardOpenOption.TRUNCATE_EXISTING)
          
          
          (persistenceContextWithoutTimestamp.store (_: String, _: RotationRecord))
            .expects(logFile.getAbsolutePath, RotationRecord(logFile.length, logFile.lastModified))
          
          val mockCallback = mockFunction[String, Unit]
          
          mockCallback expects text
          fileReader.fileModified(mockCallback)
        }
      }
      
      
      "its file as well as its rotated files, if rotation is active" - {
        "line by line, if line-wise reading is active" in new PersistenceContextWithTimestamp {
          
          val readMode = ReadMode.LINE
          val fileReader = new FileReader(logFile, defaultRotationPattern, persistenceContextWithTimestamp, defaultBufferSize, defaultCharset, readMode)
          
          
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
          
          (persistenceContextWithTimestamp.store (_: String, _: RotationRecord))
            .expects(logFile.getAbsolutePath, RotationRecord(logFile.length, logFile.lastModified))
          fileReader.fileModified(mockCallback)
        }
        
        "file by file, if file-wise reading is active" in new PersistenceContextWithTimestamp {
          
          val readMode = ReadMode.FILE
          val fileReader = new FileReader(logFile, defaultRotationPattern, persistenceContextWithTimestamp, defaultBufferSize, defaultCharset, readMode)
          
          
          val mockCallback = mockFunction[String, Unit]
  
          inSequence {
            mockCallback expects logFile3Data.substring(previousReadPosition)
            mockCallback expects logFile2Data
            mockCallback expects logFile1Data
            mockCallback expects logFileData
          }
          
          (persistenceContextWithTimestamp.store (_: String, _: RotationRecord))
            .expects(logFile.getAbsolutePath, RotationRecord(logFile.length, logFile.lastModified))
          fileReader.fileModified(mockCallback)
        }
      }
      
      
      
      "multiple rotated files after resuming reading" - {
        
        case class TestCase(bufferSize: Int, description: String)
        
        val bufferSizesToTest = Seq(
                                    TestCase(  10, "shorter than one line of text"), 
                                    TestCase( 1024, "longer than one line of text"), 
                                    TestCase(999999, "longer than the entire text"),
                                   )
        
        
        bufferSizesToTest.foreach { case TestCase(bufferSize, description) =>
          
          "with buffer size " + bufferSize + " bytes, which is " + description in new PersistenceContextWithoutTimestamp {
            
            TestUtil.writeLogToFileWithRotation(logFile, numberOfLines=1000, rotatePattern = logFile.getName + ".%i")

            
            val rotateMatcher = FileSystems.getDefault.getPathMatcher("glob:" + logFile.getParent + "/" + defaultRotationPattern)
            
            val files = (logFile.getParentFile.listFiles
              .filter(file => rotateMatcher.matches(file.toPath)) :+ logFile)
              .sortBy(file => file.lastModified)
            
            var contents = files.foldLeft("")((content, file) => content + Source.fromFile(file).mkString)
            
            if (defaultReadMode == ReadMode.LINE) { //we don't call back newlines in line-wise reading
              contents = contents.replace("\n", "")
            }
            
            
            (persistenceContextWithoutTimestamp.store (_: String, _: Any))
              .expects(logFile.toString, RotationRecord(logFile.length, logFile.lastModified))
            
            var calledBackString = "" 
            val fileReader = new FileReader(watchedFile=logFile, defaultRotationPattern, persistenceContextWithoutTimestamp, bufferSize, defaultCharset, defaultReadMode)
            fileReader.fileModified(string => calledBackString += string)
            
            
            calledBackString shouldEqual contents
          }
        }
      }
    }
    
    
    "should persist a RotationRecord" - {
      "for one log file" - {
        "without rotated files" in new PersistenceContextWithoutTimestamp {
          
          val fileReader = new FileReader(logFile, null, persistenceContextWithoutTimestamp, defaultBufferSize, defaultCharset, defaultReadMode)
          
          (persistenceContextWithoutTimestamp.store (_: String, _: Any))
            .expects(logFile.toString, RotationRecord(logFile.length, logFile.lastModified))

          fileReader.fileModified((_: String) => ())
        }

        "with rotated files" in new PersistenceContextWithTimestamp {
          //check that store() is called only once and with the correct parameters, even with multiple files (as we only want the read position persisted for the last file, not for the intermediate files)
          
          val fileReader = new FileReader(logFile, defaultRotationPattern, persistenceContextWithTimestamp, defaultBufferSize, defaultCharset, defaultReadMode)
          
          (persistenceContextWithTimestamp.store (_: String, _: Any))
            .expects(logFile.toString, RotationRecord(logFileData.length, logFile.lastModified))
          
          fileReader.fileModified((_: String) => ())
        }
      }
    }
  }
}
