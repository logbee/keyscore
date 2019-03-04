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
import java.nio.charset.Charset
import java.nio.ByteBuffer

@RunWith(classOf[JUnitRunner])
class FileReaderSpec extends RotateFilesSetup with Matchers with MockFactory with ParallelTestExecution {
  
  val defaultBufferSize = 1024
  
  
  "A FileReader" - {
    val charsetNames = Seq("UTF-8", "UTF-16LE", "UTF-32", "ISO-8859-1", "Windows-1252") //test with either "UTF-16LE" or "UTF-16BE", not "UTF-16". Otherwise our test setup writes a BOM with every string written to file. 
    charsetNames.foreach {
      charsetName => {
        val charset = Charset.forName(charsetName)
        
        def byteLen(string: String): Int = {
          charset.encode(string).limit
        }
        
        "with charset " + charsetName - {
          "should read the contents of" - {
            "its file" - {
              "line by line, if line-wise reading is active," - {
                
                val lineReadMode = ReadMode.LINE
                
                
                "if the file contains only one line of text" in
                new LogFile {
                  
                  val fileReader = new FileReader(logFile, null, defaultBufferSize, charset, lineReadMode)
                  
                  val line1 = "Line1"
                  TestUtil.writeStringToFile(logFile, line1, StandardOpenOption.TRUNCATE_EXISTING, charset)
                  
                  val mockCallback = mockFunction[FileReadData, Unit]
                  
                  mockCallback expects FileReadData(line1, logFile, logFile.length, logFile.lastModified)
                  
                  fileReader.read(mockCallback, ReadScheduleItem(logFile, 0, logFile.length, logFile.lastModified))
                }
                
                
                "if the file contains only one line of text with a newline at the end" in
                new LogFile {
                  
                  val fileReader = new FileReader(logFile, null, defaultBufferSize, charset, lineReadMode)
                  
                  val line1 = "Line1"
                  val text = line1 + "\n"
                  TestUtil.writeStringToFile(logFile, text, StandardOpenOption.TRUNCATE_EXISTING, charset)
                  
                  val mockCallback = mockFunction[FileReadData, Unit]
                  
                  mockCallback expects FileReadData(line1, logFile, logFile.length, logFile.lastModified)
                  
                  fileReader.read(mockCallback, ReadScheduleItem(logFile, 0, logFile.length, logFile.lastModified))
                }
                
                
                "if the file contains multiple lines of text" in
                new LogFile {
                  
                  val fileReader = new FileReader(logFile, null, defaultBufferSize, charset, lineReadMode)
                  
                  val line1 = "Line1"
                  val line2 = "Line2"
                  val line3 = "Line3"
                  val newline = "\n"
                  val text = line1 + newline + line2 + newline + line3
                  TestUtil.writeStringToFile(logFile, text, StandardOpenOption.TRUNCATE_EXISTING, charset)
                  
                  
                  val mockCallback = mockFunction[FileReadData, Unit]
                  
                  inSequence {
                    mockCallback expects FileReadData(line1, logFile, byteLen(line1 + newline), logFile.lastModified)
                    mockCallback expects FileReadData(line2, logFile, byteLen(line1 + newline + line2 + newline), logFile.lastModified)
                    mockCallback expects FileReadData(line3, logFile, byteLen(line1 + newline + line2 + newline + line3), logFile.lastModified)
                  }
                  fileReader.read(mockCallback, ReadScheduleItem(logFile, 0, logFile.length, logFile.lastModified))
                }
                
                
                "if the file contains multiple newline-characters directly following each other" in
                new LogFile {
                  
                  val fileReader = new FileReader(logFile, null, defaultBufferSize, charset, lineReadMode)
                  
                  val line1 = "Line1"
                  val line3 = "Line3"
                  val newline = "\n\n"
                  val text = line1 + newline + line3
                  TestUtil.writeStringToFile(logFile, text, StandardOpenOption.TRUNCATE_EXISTING, charset)
                  
                  
                  val mockCallback = mockFunction[FileReadData, Unit]
                  
                  inSequence {
                    mockCallback expects FileReadData(line1, logFile, byteLen(line1 + newline), logFile.lastModified)
                    mockCallback expects FileReadData(line3, logFile, byteLen(line1 + newline + line3), logFile.lastModified)
                  }
                  fileReader.read(mockCallback, ReadScheduleItem(logFile, 0, logFile.length, logFile.lastModified))
                }
                
                
                "if the file contains a Windows newline \\r\\n" in
                new LogFile {
                  
                  val fileReader = new FileReader(logFile, null, defaultBufferSize, charset, lineReadMode)
                  
                  val line1 = "Line1"
                  val line2 = "Line2"
                  val newline = "\r\n"
                  val text = line1 + newline + line2
                  TestUtil.writeStringToFile(logFile, text, StandardOpenOption.TRUNCATE_EXISTING, charset)
                  
                  
                  val mockCallback = mockFunction[FileReadData, Unit]
                  
                  inSequence {
                    mockCallback expects FileReadData(line1, logFile, byteLen(line1 + newline), logFile.lastModified)
                    mockCallback expects FileReadData(line2, logFile, byteLen(line1 + newline + line2), logFile.lastModified)
                  }
                  fileReader.read(mockCallback, ReadScheduleItem(logFile, 0, logFile.length, logFile.lastModified))
                }
                
                
                "if the contents of the file are longer than one buffer's length" in
                new LogFile {
                  
                  val line1 = "äbcdefg"
                  val line2 = "hij"
                  val line3 = "klmnö"
                  val newline = "\n"
                  val text = line1 + newline + line2 + newline + line3
                  
                  TestUtil.writeStringToFile(logFile, text, StandardOpenOption.TRUNCATE_EXISTING, charset)
                  
                  
                  
                  val _bufferSize = byteLen(text) / 2
                  
                  val fileReader = new FileReader(logFile, null, _bufferSize, charset, lineReadMode)
                  
                  
                  val mockCallback = mockFunction[FileReadData, Unit]
                  
                  inSequence {
                    mockCallback expects FileReadData(line1, logFile, byteLen(line1 + newline), logFile.lastModified)
                    mockCallback expects FileReadData(line2, logFile, byteLen(line1 + newline + line2 + newline), logFile.lastModified)
                    mockCallback expects FileReadData(line3, logFile, byteLen(line1 + newline + line2 + newline + line3), logFile.lastModified)
                  }
                  
                  fileReader.read(mockCallback, ReadScheduleItem(logFile, 0, logFile.length, logFile.lastModified))
                }
                
                
                
                "if the file contains a line that is longer than the buffer" in
                new LogFile {
                  
                  val line1 = "äbcdefg"
                  val line2 = "hij"
                  val line3 = "klmnö"
                  val newline = "\n"
                  val text = line1 + newline + line2 + newline + line3
                  
                  TestUtil.writeStringToFile(logFile, text, StandardOpenOption.TRUNCATE_EXISTING, charset)
                  
                  
                  
                  val _bufferSize = byteLen(line1) / 2
                  
                  val fileReader = new FileReader(logFile, null, _bufferSize, charset, lineReadMode)
                  
                  
                  val mockCallback = mockFunction[FileReadData, Unit]
                  
                  inSequence {
                    mockCallback expects FileReadData(line1, logFile, byteLen(line1 + newline), logFile.lastModified)
                    mockCallback expects FileReadData(line2, logFile, byteLen(line1 + newline + line2 + newline), logFile.lastModified)
                    mockCallback expects FileReadData(line3, logFile, byteLen(line1 + newline + line2 + newline + line3), logFile.lastModified)
                  }
                  
                  fileReader.read(mockCallback, ReadScheduleItem(logFile, 0, logFile.length, logFile.lastModified))
                }
              }
              
              
              "in whole, if file-wise reading is active" in
              new LogFile {
                
                val _readMode = ReadMode.FILE
                val fileReader = new FileReader(logFile, null, defaultBufferSize, charset, _readMode)
                
                
                val line1 = "Line1"
                val line2 = "Line2"
                val line3 = "Line3"
                
                val text = line1 + "\n" + line2 + "\n" + line3
                
                TestUtil.writeStringToFile(logFile, text, StandardOpenOption.TRUNCATE_EXISTING, charset)
                
                
                val mockCallback = mockFunction[FileReadData, Unit]
                
                mockCallback expects FileReadData(text, logFile, byteLen(text), logFile.lastModified)
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
        }
      }
    }
  }
}
