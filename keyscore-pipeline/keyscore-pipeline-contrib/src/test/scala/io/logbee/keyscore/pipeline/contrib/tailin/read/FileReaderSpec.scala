package io.logbee.keyscore.pipeline.contrib.tailin.read

import java.nio.charset.StandardCharsets
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

import scala.io.Source

import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfter
import org.scalatest.FreeSpec
import org.scalatest.Matchers
import org.scalatest.ParallelTestExecution

import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadScheduleItem
import io.logbee.keyscore.pipeline.contrib.tailin.util.TestUtil

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import io.logbee.keyscore.pipeline.contrib.tailin.util.RotateFilesSetup

@RunWith(classOf[JUnitRunner])
class FileReaderSpec extends RotateFilesSetup with Matchers with MockFactory with ParallelTestExecution {
  
  
  val defaultBufferSize = 1024
  val defaultCharset = StandardCharsets.UTF_8
  val defaultReadMode = ReadMode.LINE

  
  
  "A FileReader" - {
    
    "should read the contents of" - {
      "its file" - {
        "line by line, if line-wise reading is active," - {
          
          val readMode = ReadMode.LINE
          
          "if the file contains only one line of text" in new LogFile {
            
            val fileReader = new FileReader(logFile, null, defaultBufferSize, defaultCharset, readMode)
            
            val line1 = "Line1"
            TestUtil.writeStringToFile(logFile, line1, StandardOpenOption.TRUNCATE_EXISTING)
  
            val mockCallback = mockFunction[FileReadData, Unit]
  
            mockCallback expects FileReadData(line1, logFile, logFile.length, logFile.lastModified)
            
            fileReader.read(mockCallback, ReadScheduleItem(logFile, 0, logFile.length, logFile.lastModified))
          }
          
          "if the file contains only one line of text with a newline at the end" in new LogFile {
            
            val fileReader = new FileReader(logFile, null, defaultBufferSize, defaultCharset, readMode)
            
            val line1 = "Line1"
            val text = line1 + "\n"
            TestUtil.writeStringToFile(logFile, text, StandardOpenOption.TRUNCATE_EXISTING)
  
            val mockCallback = mockFunction[FileReadData, Unit]
  
            mockCallback expects FileReadData(line1, logFile, logFile.length, logFile.lastModified)
            
            fileReader.read(mockCallback, ReadScheduleItem(logFile, 0, logFile.length, logFile.lastModified))
          }
          
          "if the file contains multiple lines of text" in new LogFile {
            
            val fileReader = new FileReader(logFile, null, defaultBufferSize, defaultCharset, readMode)
            
            val line1 = "Line1"
            val line2 = "Line2"
            val line3 = "Line3"
            val text = line1 + "\n" + line2 + "\n" + line3
            TestUtil.writeStringToFile(logFile, text, StandardOpenOption.TRUNCATE_EXISTING)
            
            
            val mockCallback = mockFunction[FileReadData, Unit]
  
            inSequence {
              mockCallback expects FileReadData(line1, logFile, line1.length + "\n".length, logFile.lastModified)
              mockCallback expects FileReadData(line2, logFile, line1.length + "\n".length + line2.length + "\n".length, logFile.lastModified)
              mockCallback expects FileReadData(line3, logFile, line1.length + "\n".length + line2.length + "\n".length + line3.length, logFile.lastModified)
            }
            fileReader.read(mockCallback, ReadScheduleItem(logFile, 0, logFile.length, logFile.lastModified))
          }
          
          "if the file contains multiple newline-characters directly following each other"  in new LogFile {
            
            val fileReader = new FileReader(logFile, null, defaultBufferSize, defaultCharset, readMode)
            
            val line1 = "Line1"
            val line3 = "Line3"
            val text = line1 + "\n\n" + line3
            TestUtil.writeStringToFile(logFile, text, StandardOpenOption.TRUNCATE_EXISTING)
            
            
            val mockCallback = mockFunction[FileReadData, Unit]
  
            inSequence {
              mockCallback expects FileReadData(line1, logFile, line1.length + "\n\n".length, logFile.lastModified)
              mockCallback expects FileReadData(line3, logFile, line1.length + "\n\n".length + line3.length, logFile.lastModified)
            }
            fileReader.read(mockCallback, ReadScheduleItem(logFile, 0, logFile.length, logFile.lastModified))
          }
          
          "if the file contains a Windows newline \\r\\n" in new LogFile {
            
            val fileReader = new FileReader(logFile, null, defaultBufferSize, defaultCharset, readMode)
            
            val line1 = "Line1"
            val line2 = "Line2"
            val text = line1 + "\r\n" + line2
            TestUtil.writeStringToFile(logFile, text, StandardOpenOption.TRUNCATE_EXISTING)
            
            
            val mockCallback = mockFunction[FileReadData, Unit]
  
            inSequence {
              mockCallback expects FileReadData(line1, logFile, line1.length + "\r\n".length, logFile.lastModified)
              mockCallback expects FileReadData(line2, logFile, line1.length + "\r\n".length + line2.length, logFile.lastModified)
            }
            fileReader.read(mockCallback, ReadScheduleItem(logFile, 0, logFile.length, logFile.lastModified))
          }
          
          "if the contents of the file are longer than one buffer's length" in new LogFile {
            
            val fileReader = new FileReader(logFile, null, defaultBufferSize, defaultCharset, readMode)
            
            val line1 = new String(new Array[Byte](512))
            val line2 = new String(new Array[Byte](52))
            val line3 = new String(new Array[Byte](1023))
            val text = line1 + "\n" + line2 + "\n" + line3
            
            TestUtil.writeStringToFile(logFile, text, StandardOpenOption.TRUNCATE_EXISTING)
            
            
            val mockCallback = mockFunction[FileReadData, Unit]
  
            inSequence {
              mockCallback expects FileReadData(line1, logFile, line1.length + "\n".length, logFile.lastModified)
              mockCallback expects FileReadData(line2, logFile, line1.length + "\n".length + line2.length + "\n".length, logFile.lastModified)
              mockCallback expects FileReadData(line3, logFile, line1.length + "\n".length + line2.length + "\n".length + line3.length, logFile.lastModified)
            }
            
              
            fileReader.read(mockCallback, ReadScheduleItem(logFile, 0, logFile.length, logFile.lastModified))
          }
          
          "if the file contains a line that is longer than the buffer" in new LogFile {
            
            val fileReader = new FileReader(logFile, null, defaultBufferSize, defaultCharset, readMode)
            
            val line1 = new String(new Array[Byte](12))
        		val line2 = new String(new Array[Byte](34567))
        		val line3 = new String(new Array[Byte](89))
            val text = line1 + "\n" + line2 + "\n" + line3
            
            TestUtil.writeStringToFile(logFile, text, StandardOpenOption.TRUNCATE_EXISTING)
            
            
            val mockCallback = mockFunction[FileReadData, Unit]
  
            inSequence {
              mockCallback expects FileReadData(line1, logFile, line1.length + "\n".length, logFile.lastModified)
              mockCallback expects FileReadData(line2, logFile, line1.length + "\n".length + line2.length + "\n".length, logFile.lastModified)
              mockCallback expects FileReadData(line3, logFile, line1.length + "\n".length + line2.length + "\n".length + line3.length, logFile.lastModified)
            }
            
            fileReader.read(mockCallback, ReadScheduleItem(logFile, 0, logFile.length, logFile.lastModified))
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
          
          
          val mockCallback = mockFunction[FileReadData, Unit]
          
          mockCallback expects FileReadData(text, logFile, text.length, logFile.lastModified)
          fileReader.read(mockCallback, ReadScheduleItem(logFile, 0, logFile.length, logFile.lastModified))
        }
      }
      
      
      //TODO this test is obsolete here, as a single FileReader doesn't handle rotation anymore. Possibly reuse code in ReadSchedulerSpec or integration test
//      //TODO this doesn't work yet, because lastModified-time of rotatedFiles is identical
//      "multiple rotated files after resuming reading" ignore {
//        
//        case class TestCase(bufferSize: Int, description: String)
//        
//        val bufferSizesToTest = Seq(
//                                    TestCase(  10, "shorter than one line of text"),
//                                    TestCase( 1024, "longer than one line of text"),
//                                    TestCase(999999, "longer than the entire text"),
//                                   )
//        
//        
//        bufferSizesToTest.foreach { case TestCase(bufferSize, description) =>
//          
//          "with buffer size " + bufferSize + " bytes, which is " + description in new LogFile {
//            
//            TestUtil.writeLogToFileWithRotation(logFile, numberOfLines=1000, rotatePattern = logFile.getName + ".%i")
//
//            
//            val rotateMatcher = FileSystems.getDefault.getPathMatcher("glob:" + logFile.getParent + "/" + defaultRotationPattern)
//            
//            val files = (logFile.getParentFile.listFiles
//              .filter(file => rotateMatcher.matches(file.toPath)) :+ logFile)
//              .sortBy(file => file.lastModified)
//            
//            var contents = files.foldLeft("")((content, file) => content + Source.fromFile(file).mkString)
//            
//            if (defaultReadMode == ReadMode.LINE) { //we don't call back newlines in line-wise reading
//              contents = contents.replace("\n", "")
//            }
//            
//            
//            var calledBackString = "" 
//            val fileReader = new FileReader(baseFile=logFile, defaultRotationPattern, bufferSize, defaultCharset, defaultReadMode)
//            
//            
//            //schedule a read for every rotation file
//            val filesToRead = FileReader.getFilesToRead(logFile, defaultRotationPattern, previousReadTimestamp=0)
//            filesToRead.foreach { file =>
//              println(file + " " + file.lastModified)
//              fileReader.read(string => calledBackString += string, ReadScheduleItem(logFile, 0, file.length, file.lastModified))
//            }
//            
//            
//            
//            calledBackString shouldEqual contents
//          }
//        }
//      }
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
