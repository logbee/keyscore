package io.logbee.keyscore.pipeline.contrib.tailin.file

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, StandardOpenOption}

import io.logbee.keyscore.pipeline.contrib.tailin.persistence.FilePersistenceContext
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfter, FreeSpec, Matchers, ParallelTestExecution}
import org.scalatest.junit.JUnitRunner

import scala.reflect.runtime.universe._
import io.logbee.keyscore.pipeline.contrib.tailin.util.TestUtility


@RunWith(classOf[JUnitRunner])
class FileReaderSpec extends FreeSpec with BeforeAndAfter with Matchers with MockFactory with ParallelTestExecution {

  var watchDir: Path = null

  before {
    watchDir = Files.createTempDirectory("watchTest")

    TestUtility.waitForFileToExist(watchDir.toFile)
  }

  after {
    TestUtility.recursivelyDelete(watchDir)
  }

  val defaultBufferSize = 1024
  val defaultCharset = StandardCharsets.UTF_8
  val defaultReadMode = ReadMode.LINE
  
  trait LogFile {
    
    val logFile = watchDir.resolve("log.txt").toFile
    
    logFile.createNewFile()
    TestUtility.waitForFileToExist(logFile)
    
    
    val logFileData = "Log_File_0_ "
    TestUtility.writeStringToFile(logFile, logFileData, StandardOpenOption.APPEND)
  }
  
  trait RotateFiles extends LogFile {
    
    val logFile1337 = watchDir.resolve("log.txt.1337").toFile
    val logFileCsv = watchDir.resolve("log.csv").toFile
    
    val otherLogFile1 = watchDir.resolve("other_log.txt.1").toFile
    val otherLogFile = watchDir.resolve("other_log.txt").toFile
    
    val logFile2 = watchDir.resolve("log.txt.2").toFile
    val logFile1 = watchDir.resolve("log.txt.1").toFile

    val files = Array(logFile1337, logFileCsv, otherLogFile1, otherLogFile, logFile2, logFile1)

    files.foreach { file => 
      file.createNewFile()
      TestUtility.waitForFileToExist(file)
    }
    
    val logFile1337Data = "Log_File_1337 "
    TestUtility.writeStringToFile(logFile1337, logFile1337Data, StandardOpenOption.APPEND)
    
    val logFileCsvData = "Log_File_Csv "
    TestUtility.writeStringToFile(logFileCsv, logFileCsvData, StandardOpenOption.APPEND)
    
    val otherLogFile1Data = "other_Log_File_1 "
    TestUtility.writeStringToFile(otherLogFile1, otherLogFile1Data, StandardOpenOption.APPEND)
    
    Thread.sleep(1000)
    
    val otherLogFileData = "other_Log_File "
    TestUtility.writeStringToFile(otherLogFile, otherLogFileData, StandardOpenOption.APPEND)

    val logFile2Data = "Log_File_2_22 "
    TestUtility.writeStringToFile(logFile2, logFile2Data, StandardOpenOption.APPEND)
    
    Thread.sleep(1000)
    
    val logFile1Data = "Log_File_1_1 "
    TestUtility.writeStringToFile(logFile1, logFile1Data, StandardOpenOption.APPEND)
    
    Thread.sleep(1000)
    
    //rewrite logFile to set its lastModified-timestamp after logFile2 and logFile1
    TestUtility.writeStringToFile(logFile, logFileData, StandardOpenOption.TRUNCATE_EXISTING)
  }

  trait PersistenceContextWithoutTimestamp extends LogFile {
    
    val persistenceContextWithoutTimestamp = mock[FilePersistenceContext]
    (persistenceContextWithoutTimestamp.load[RotationRecord] (_: String)(_: TypeTag[RotationRecord]))
      .expects(logFile.getAbsolutePath, typeTag[RotationRecord])
      .returning(Some(RotationRecord(0, 0)))
    
      
    val fileReader = new FileReader(logFile, null, persistenceContextWithoutTimestamp, defaultBufferSize, defaultCharset, defaultReadMode)
  }

  trait PersistenceContextWithTimestamp extends RotateFiles {
    
    val logFile4_ModifiedBeforePreviousReadTimestamp = watchDir.resolve("log.txt.4").toFile
    logFile4_ModifiedBeforePreviousReadTimestamp.createNewFile()
    TestUtility.waitForFileToExist(logFile4_ModifiedBeforePreviousReadTimestamp)
    
    val logFile4Data = "Log_File_4_4444 "
    TestUtility.writeStringToFile(logFile4_ModifiedBeforePreviousReadTimestamp, logFile4Data, StandardOpenOption.APPEND)
    
    Thread.sleep(1000)
    
    val logFile3_ModifiedAfterPreviousReadTimestamp = watchDir.resolve("log.txt.3").toFile
    logFile3_ModifiedAfterPreviousReadTimestamp.createNewFile()
    TestUtility.waitForFileToExist(logFile3_ModifiedAfterPreviousReadTimestamp)
    
    val logFile3Data = "Log_File_3_333 "
    TestUtility.writeStringToFile(logFile3_ModifiedAfterPreviousReadTimestamp, logFile3Data, StandardOpenOption.APPEND)
    
    
    val previousReadPosition = 0
    val persistenceContextWithTimestamp: FilePersistenceContext = mock[FilePersistenceContext]
    (persistenceContextWithTimestamp.load[RotationRecord] (_: String)(_: TypeTag[RotationRecord]))
      .expects(logFile.getAbsolutePath, typeTag[RotationRecord])
      .returning(Some(RotationRecord(previousReadPosition, logFile4_ModifiedBeforePreviousReadTimestamp.lastModified)))
      
    
    //Rewrite contents of other rotated files to set their lastModified-timestamp after the others
    Thread.sleep(1000)
    TestUtility.writeStringToFile(logFile2, logFile2Data, StandardOpenOption.TRUNCATE_EXISTING)
    
    Thread.sleep(1000)
    TestUtility.writeStringToFile(logFile1, logFile1Data, StandardOpenOption.TRUNCATE_EXISTING)
    
    Thread.sleep(1000)
    TestUtility.writeStringToFile(logFile, logFileData, StandardOpenOption.TRUNCATE_EXISTING)
    
    
    
    val fileReader = new FileReader(logFile, ".[1-5]", persistenceContextWithTimestamp, defaultBufferSize, defaultCharset, defaultReadMode)
  }

  "A FileReader" - {
    "should retrieve the list of files it has to read from," - {
      "which should contain" - {
        "only the FileReader's file itself," - {
          "if it got passed null as rotationSuffix" in new RotateFiles {
            
            val persistenceContextWithoutTimestamp = mock[FilePersistenceContext]
            (persistenceContextWithoutTimestamp.load[RotationRecord] (_: String)(_: TypeTag[RotationRecord]))
              .expects(logFile.getAbsolutePath, typeTag[RotationRecord])
              .returning(Some(RotationRecord(0, 0)))
            
            val rotationSuffix: String = null
            
            val _fileReader = new FileReader(logFile, rotationSuffix, persistenceContextWithoutTimestamp, defaultBufferSize, defaultCharset, defaultReadMode)
            
            val rotationFiles = _fileReader.getFilesToRead
            
            rotationFiles.length shouldBe 1
            rotationFiles(0) shouldBe logFile
          }
          
          "if it got passed an empty rotationSuffix" in new RotateFiles {
            
            val persistenceContextWithoutTimestamp = mock[FilePersistenceContext]
            (persistenceContextWithoutTimestamp.load[RotationRecord] (_: String)(_: TypeTag[RotationRecord]))
              .expects(logFile.getAbsolutePath, typeTag[RotationRecord])
              .returning(Some(RotationRecord(0, 0)))            
            
            val rotationSuffix: String = ""
            
            val _fileReader = new FileReader(logFile, rotationSuffix, persistenceContextWithoutTimestamp, defaultBufferSize, defaultCharset, defaultReadMode)
            
            val rotationFiles = _fileReader.getFilesToRead
            
            rotationFiles.length shouldBe 1
            rotationFiles(0) shouldBe logFile
          }
        }
        
        "all files matching the rotationSuffix," - {
          "if it got passed a non-empty rotationSuffix" in new RotateFiles {
            
            val mockPersistenceContext = mock[FilePersistenceContext]
            (mockPersistenceContext.load[RotationRecord] (_: String)(_: TypeTag[RotationRecord]))
              .expects(logFile.getAbsolutePath, typeTag[RotationRecord])
              .returning(Some(RotationRecord(0, 0)))
            
            
            val rotationSuffix = ".[1-2]"
            val fileReader = new FileReader(logFile, rotationSuffix, mockPersistenceContext, defaultBufferSize, defaultCharset, defaultReadMode)
            
            
            val rotationFiles = fileReader.getFilesToRead
            
            rotationFiles should contain allOf (logFile, logFile1, logFile2)
          }
        }
        
        "all files matching the rotationSuffix that are newer than the previousReadTimestamp," - {
          "if it got passed a non-empty rotationSuffix and a previousReadTimestamp" in new PersistenceContextWithTimestamp {
            
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
            
            val line1 = "Line1"
            TestUtility.writeStringToFile(logFile, line1, StandardOpenOption.TRUNCATE_EXISTING)
  
            (persistenceContextWithoutTimestamp.store (_: String, _: RotationRecord))
              .expects(logFile.getAbsolutePath, RotationRecord(logFile.length, logFile.lastModified))
  
            val mockCallback = mockFunction[String, Unit]
  
            mockCallback expects line1
            
            fileReader.fileModified(mockCallback)
          }
          
          "if the file contains only one line of text with a newline at the end" in new PersistenceContextWithoutTimestamp {
            
            val line1 = "Line1"
            val text = line1 + "\n"
            TestUtility.writeStringToFile(logFile, text, StandardOpenOption.TRUNCATE_EXISTING)
  
            (persistenceContextWithoutTimestamp.store (_: String, _: RotationRecord))
              .expects(logFile.getAbsolutePath, RotationRecord(logFile.length, logFile.lastModified))
  
            val mockCallback = mockFunction[String, Unit]
  
            mockCallback expects line1
            
            fileReader.fileModified(mockCallback)
          }
          
          "if the file contains multiple lines of text" in new PersistenceContextWithoutTimestamp {
            
            val line1 = "Line1"
            val line2 = "Line2"
            val line3 = "Line3"
            val text = line1 + "\n" + line2 + "\n" + line3
            TestUtility.writeStringToFile(logFile, text, StandardOpenOption.TRUNCATE_EXISTING)
            
            
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
          
          //TEST? multiline with newline at the end
          
          "if the file contains multiple newline-characters directly following each other"  in new PersistenceContextWithoutTimestamp {
            
            val line1 = "Line1"
            val line3 = "Line3"
            val text = line1 + "\n\n" + line3
            TestUtility.writeStringToFile(logFile, text, StandardOpenOption.TRUNCATE_EXISTING)
            
            
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
            
            val line1 = "Line1"
            val line2 = "Line2"
            val text = line1 + "\r\n" + line2
            TestUtility.writeStringToFile(logFile, text, StandardOpenOption.TRUNCATE_EXISTING)
            
            
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
            
            val line1 = new String(new Array[Byte](512))
            val line2 = new String(new Array[Byte](52))
            val line3 = new String(new Array[Byte](1023))
            val text = line1 + "\n" + line2 + "\n" + line3
            
            TestUtility.writeStringToFile(logFile, text, StandardOpenOption.TRUNCATE_EXISTING)
            
            
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
            
            val line1 = new String(new Array[Byte](12))
        		val line2 = new String(new Array[Byte](34567))
        		val line3 = new String(new Array[Byte](89))
            val text = line1 + "\n" + line2 + "\n" + line3
            
            TestUtility.writeStringToFile(logFile, text, StandardOpenOption.TRUNCATE_EXISTING)
            
            
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
        
        "in whole from the previous reading position, if file-wise reading is active" in new LogFile {
          
          val readMode = ReadMode.FILE
          
          val line1 = "Line1"
          val line2 = "Line2"
          val line3 = "Line3"
          
          val text = line1 + "\n" + line2 + "\n" + line3
          
          TestUtility.writeStringToFile(logFile, text, StandardOpenOption.TRUNCATE_EXISTING)
          
          
           val persistenceContextWithoutTimestamp = mock[FilePersistenceContext]
          (persistenceContextWithoutTimestamp.load[RotationRecord] (_: String)(_: TypeTag[RotationRecord]))
            .expects(logFile.getAbsolutePath, typeTag[RotationRecord])
            .returning(Some(RotationRecord(0, 0)))
          
          val fileReader = new FileReader(logFile, null, persistenceContextWithoutTimestamp, defaultBufferSize, defaultCharset, readMode)
          
          
          
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
        
        "file by file, if file-wise reading is active" in {
          val readMode = ReadMode.FILE
        }
      }
    }

    "should persist a RotationRecord" - {
      "for one log file" - {
        "without rotated files" in new PersistenceContextWithoutTimestamp {
          
          (persistenceContextWithoutTimestamp.store (_: String, _: Any))
            .expects(logFile.toString, RotationRecord(logFile.length, logFile.lastModified))

          fileReader.fileModified((_: String) => ())
        }

        "with rotated files" in new PersistenceContextWithTimestamp {
          //check that store() is called only once and with the correct parameters, even with multiple files (as we only want the read position persisted for the last file, not for the intermediate files)
          
          (persistenceContextWithTimestamp.store (_: String, _: Any))
            .expects(logFile.toString, RotationRecord(logFileData.length, logFile.lastModified))
          
            
          fileReader.fileModified((_: String) => ())
        }
      }
    }

    "should react to a file modification event" - {
      //TODO integration test
    }
  }
}
