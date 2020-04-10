package io.logbee.keyscore.pipeline.contrib.tailin.read

import java.nio.charset.Charset

import io.logbee.keyscore.pipeline.contrib.tailin.file.FileHandle
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadScheduleItem
import io.logbee.keyscore.pipeline.contrib.tailin.util.SpecWithRotateFiles
import org.junit.runner.RunWith
import org.scalamock.matchers.ArgCapture.CaptureAll
import org.scalamock.scalatest.MockFactory
import org.scalatest.ParallelTestExecution
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class FileReaderSpec extends SpecWithRotateFiles with Matchers with MockFactory with ParallelTestExecution {
  
  val defaultBufferSize = 1024
  
  "A FileReader" - {
    val newerFilesWithSharedLastModified = 0
    def postReadFileActionFunc_noop(file: FileHandle): Unit = {}
    
    val charsetNames = Seq("UTF-8", "UTF-16LE", "UTF-32", "ISO-8859-1", "Windows-1252") //test with either "UTF-16LE" or "UTF-16BE", not "UTF-16". Otherwise our test setup writes a BOM with every string written to file.
    charsetNames.foreach {
      charsetName => {
        charset = Charset.forName(charsetName)
        
        def byteLen(string: String): Int = {
          charset.encode(string).limit()
        }
        
        "with charset " + charsetName - {
          "should read the contents of its file" - {
            "line by line, if line-wise reading is active," - {

              val lineReadMode = ReadMode.Line

              "if the file contains only one line of text" in
              new LogFile {
                val line1 = "Line1"
                logFile = writeStringToFile(logFile, line1)

                val fileReader = new FileReader(logFile.file, defaultBufferSize, charset, lineReadMode, postReadFileActionFunc = postReadFileActionFunc_noop)

                val mockCallback = mockFunction[FileReadData, Unit]
                val capturedFileReadData = CaptureAll[FileReadData]()
                mockCallback expects capture(capturedFileReadData) once
                
                fileReader.read(mockCallback, ReadScheduleItem(logFile.file, 0, logFile.length(), logFile.lastModified, newerFilesWithSharedLastModified))

                calledBackDataIsSimilarTo(
                  capturedFileReadData,
                  FileReadData(
                    readData=line1,
                    baseFile=null,
                    physicalFile=logFile.absolutePath,
                    readEndPos=logFile.length(),
                    writeTimestamp=logFile.lastModified,
                    readTimestamp = -1,
                    newerFilesWithSharedLastModified
                  )
                )
              }
              
              
              "if the file contains only one line of text with a newline at the end" in
              new LogFile {
                val line1 = "Line1"
                val text = line1 + "\n"
                logFile = writeStringToFile(logFile, text)

                val fileReader = new FileReader(logFile.file, defaultBufferSize, charset, lineReadMode, postReadFileActionFunc = postReadFileActionFunc_noop)

                val mockCallback = mockFunction[FileReadData, Unit]
                val capturedFileReadData = CaptureAll[FileReadData]()
                mockCallback expects capture(capturedFileReadData) once
                
                fileReader.read(mockCallback, ReadScheduleItem(logFile.file, 0, byteLen(text), logFile.lastModified, newerFilesWithSharedLastModified))

                calledBackDataIsSimilarTo(
                  capturedFileReadData,
                  FileReadData(
                    readData=line1,
                    baseFile=null,
                    physicalFile=logFile.absolutePath,
                    readEndPos=byteLen(text),
                    writeTimestamp=logFile.lastModified,
                    readTimestamp = -1,
                    newerFilesWithSharedLastModified
                  )
                )
              }


              "if the file contains multiple lines of text" in
              new LogFile {
                val line1 = "Line1"
                val line2 = "Line2"
                val line3 = "Line3"
                val newline = "\n"
                val text = line1 + newline + line2 + newline + line3
                logFile = writeStringToFile(logFile, text)

                val fileReader = new FileReader(logFile.file, defaultBufferSize, charset, lineReadMode, postReadFileActionFunc = postReadFileActionFunc_noop)

                val mockCallback = mockFunction[FileReadData, Unit]
                val capturedFileReadData = CaptureAll[FileReadData]()
                mockCallback expects capture(capturedFileReadData) repeated 3
                
                fileReader.read(mockCallback, ReadScheduleItem(logFile.file, 0, byteLen(text), logFile.lastModified, newerFilesWithSharedLastModified))

                calledBackDataIsSimilarTo(
                  capturedFileReadData,
                  FileReadData(
                    readData=line1,
                    baseFile=null,
                    physicalFile=logFile.absolutePath,
                    readEndPos=byteLen(line1 + newline),
                    writeTimestamp=logFile.lastModified,
                    readTimestamp = -1,
                    newerFilesWithSharedLastModified
                  ),
                  FileReadData(
                    readData=line2,
                    baseFile=null,
                    physicalFile=logFile.absolutePath,
                    readEndPos=byteLen(line1 + newline + line2 + newline),
                    writeTimestamp=logFile.lastModified,
                    readTimestamp = -1,
                    newerFilesWithSharedLastModified
                  ),
                  FileReadData(
                    readData=line3,
                    baseFile=null,
                    physicalFile=logFile.absolutePath,
                    readEndPos=byteLen(line1 + newline + line2 + newline + line3),
                    writeTimestamp=logFile.lastModified,
                    readTimestamp = -1,
                    newerFilesWithSharedLastModified
                  )
                )
              }


              "if the file contains multiple newline-characters directly following each other" in
              new LogFile {
                val line1 = "Line1"
                val line3 = "Line3"
                val newline = "\n\n"
                val text = line1 + newline + line3
                logFile = writeStringToFile(logFile, text)

                val fileReader = new FileReader(logFile.file, defaultBufferSize, charset, lineReadMode, postReadFileActionFunc = postReadFileActionFunc_noop)

                val mockCallback = mockFunction[FileReadData, Unit]
                val capturedFileReadData = CaptureAll[FileReadData]()
                mockCallback expects capture(capturedFileReadData) twice
                
                fileReader.read(mockCallback, ReadScheduleItem(logFile.file, 0, byteLen(text), logFile.lastModified, newerFilesWithSharedLastModified))
                
                calledBackDataIsSimilarTo(
                  capturedFileReadData,
                  FileReadData(
                    readData=line1,
                    baseFile=null,
                    physicalFile=logFile.absolutePath,
                    readEndPos=byteLen(line1 + newline),
                    writeTimestamp=logFile.lastModified,
                    readTimestamp = -1,
                    newerFilesWithSharedLastModified
                  ),
                  FileReadData(
                    readData=line3,
                    baseFile=null,
                    physicalFile=logFile.absolutePath,
                    readEndPos=byteLen(line1 + newline + line3),
                    writeTimestamp=logFile.lastModified,
                    readTimestamp = -1,
                    newerFilesWithSharedLastModified
                  )
                )
              }


              "if the file contains a Windows newline \\r\\n" in
              new LogFile {
                val line1 = "Line1"
                val line2 = "Line2"
                val newline = "\r\n"
                val text = line1 + newline + line2
                logFile = writeStringToFile(logFile, text)

                val fileReader = new FileReader(logFile.file, defaultBufferSize, charset, lineReadMode, postReadFileActionFunc = postReadFileActionFunc_noop)

                val mockCallback = mockFunction[FileReadData, Unit]
                val capturedFileReadData = CaptureAll[FileReadData]()
                mockCallback expects capture(capturedFileReadData) twice
                
                fileReader.read(mockCallback, ReadScheduleItem(logFile.file, 0, byteLen(text), logFile.lastModified, newerFilesWithSharedLastModified))

                calledBackDataIsSimilarTo(
                  capturedFileReadData,
                  FileReadData(
                    readData=line1,
                    baseFile=null,
                    physicalFile=logFile.absolutePath,
                    readEndPos=byteLen(line1 + newline),
                    writeTimestamp=logFile.lastModified,
                    readTimestamp = -1,
                    newerFilesWithSharedLastModified
                  ),
                  FileReadData(
                    readData=line2,
                    baseFile=null,
                    physicalFile=logFile.absolutePath,
                    readEndPos=byteLen(line1 + newline + line2),
                    writeTimestamp=logFile.lastModified,
                    readTimestamp = -1,
                    newerFilesWithSharedLastModified
                  )
                )
              }


              "if the contents of the file are longer than one buffer's length" in
              new LogFile {
                val line1 = "äbcdefg"
                val line2 = "hij"
                val line3 = "klmnö"
                val newline = "\n"
                val text = line1 + newline + line2 + newline + line3

                logFile = writeStringToFile(logFile, text)

                val _bufferSize = byteLen(text) / 2

                val fileReader = new FileReader(logFile.file, _bufferSize, charset, lineReadMode, postReadFileActionFunc = postReadFileActionFunc_noop)

                val mockCallback = mockFunction[FileReadData, Unit]
                val capturedFileReadData = CaptureAll[FileReadData]()
                mockCallback expects capture(capturedFileReadData) repeated 3
                
                fileReader.read(mockCallback, ReadScheduleItem(logFile.file, 0, byteLen(text), logFile.lastModified, newerFilesWithSharedLastModified))

                calledBackDataIsSimilarTo(
                  capturedFileReadData,
                  FileReadData(
                    readData=line1,
                    baseFile=null,
                    physicalFile=logFile.absolutePath,
                    readEndPos=byteLen(line1 + newline),
                    writeTimestamp=logFile.lastModified,
                    readTimestamp = -1,
                    newerFilesWithSharedLastModified
                  ),
                  FileReadData(
                    readData=line2,
                    baseFile=null,
                    physicalFile=logFile.absolutePath,
                    readEndPos=byteLen(line1 + newline + line2 + newline),
                    writeTimestamp=logFile.lastModified,
                    readTimestamp = -1,
                    newerFilesWithSharedLastModified
                  ),
                  FileReadData(
                    readData=line3,
                    baseFile=null,
                    physicalFile=logFile.absolutePath,
                    readEndPos=byteLen(line1 + newline + line2 + newline + line3),
                    writeTimestamp=logFile.lastModified,
                    readTimestamp = -1,
                    newerFilesWithSharedLastModified
                  )
                )
              }

              "if the file contains a line that is longer than the buffer" in
              new LogFile {
                val line1 = "äbcdefg"
                val line2 = "hij"
                val line3 = "klmnö"
                val newline = "\n"
                val text = line1 + newline + line2 + newline + line3

                logFile = writeStringToFile(logFile, text)

                val _bufferSize = byteLen(line1) / 2

                val fileReader = new FileReader(logFile.file, _bufferSize, charset, lineReadMode, postReadFileActionFunc = postReadFileActionFunc_noop)

                val mockCallback = mockFunction[FileReadData, Unit]
                val capturedFileReadData = CaptureAll[FileReadData]()
                mockCallback expects capture(capturedFileReadData) repeated 3
                
                fileReader.read(mockCallback, ReadScheduleItem(logFile.file, 0, byteLen(text), logFile.lastModified, newerFilesWithSharedLastModified))

                calledBackDataIsSimilarTo(
                  capturedFileReadData,
                  FileReadData(
                    readData=line1,
                    baseFile=null,
                    physicalFile=logFile.absolutePath,
                    readEndPos=byteLen(line1 + newline),
                    writeTimestamp=logFile.lastModified,
                    readTimestamp = -1,
                    newerFilesWithSharedLastModified
                  ),
                  FileReadData(
                    readData=line2,
                    baseFile=null,
                    physicalFile=logFile.absolutePath,
                    readEndPos=byteLen(line1 + newline + line2 + newline),
                    writeTimestamp=logFile.lastModified,
                    readTimestamp = -1,
                    newerFilesWithSharedLastModified
                  ),
                  FileReadData(
                    readData=line3,
                    baseFile=null,
                    physicalFile=logFile.absolutePath,
                    readEndPos=byteLen(line1 + newline + line2 + newline + line3),
                    writeTimestamp=logFile.lastModified,
                    readTimestamp = -1,
                    newerFilesWithSharedLastModified
                  )
                )
              }
            }
            
            "in blocks of multiple lines, if multi-line reading is active" in
            new LogFile {
              val line1 = "Line1"
              val line2 = "Line2"
              val line3 = "Line3"
              val newline = "\n"
              val multiLineText1 = line1 + newline + line2 + newline + line3 + newline
              val multiLineText2 = line1 + newline + line2 + newline + line3 + newline
              val text = multiLineText1 + multiLineText2
              logFile = writeStringToFile(logFile, text)
              
              val firstLinePattern = ".*" + line1 + ".*"
              val readMode = ReadMode.MultiLine(firstLinePattern.r)
              val fileReader = new FileReader(logFile.file, defaultBufferSize, charset, readMode, postReadFileActionFunc = postReadFileActionFunc_noop)
              
              val mockCallback = mockFunction[FileReadData, Unit]
              val capturedFileReadData = CaptureAll[FileReadData]()
              mockCallback expects capture(capturedFileReadData) twice
              
              fileReader.read(mockCallback, ReadScheduleItem(logFile.file, 0, byteLen(text), logFile.lastModified, newerFilesWithSharedLastModified))
              
              calledBackDataIsSimilarTo(
                capturedFileReadData,
                FileReadData(
                  readData=multiLineText1,
                  baseFile=null,
                  physicalFile=logFile.absolutePath,
                  readEndPos=byteLen(multiLineText1),
                  writeTimestamp=logFile.lastModified,
                  readTimestamp = -1,
                  newerFilesWithSharedLastModified
                ),
                FileReadData(
                  readData=multiLineText2,
                  baseFile=null,
                  physicalFile=logFile.absolutePath,
                  readEndPos=byteLen(text),
                  writeTimestamp=logFile.lastModified,
                  readTimestamp = -1,
                  newerFilesWithSharedLastModified
                )
              )
            }
            
            "in blocks of multiple lines from first line to last line, if multi-line-with-end reading is active" in
            new LogFile {
              val line1 = "Line1"
              val line2 = "Line2"
              val line3 = "Line3"
              val line4 = "Line4"
              val newline = "\n"
              val multiLineText = line1 + newline + line2 + newline + line3 + newline + line4 + newline
              val expected      = line1 + newline + line2 + newline + line3
              logFile = writeStringToFile(logFile, multiLineText)
              
              val firstLinePattern = ".*" + line1 + ".*"
              val lastLinePattern  = ".*" + line3 + ".*"
              val readMode = ReadMode.MultiLineWithEnd(firstLinePattern.r, lastLinePattern.r)
              val fileReader = new FileReader(logFile.file, defaultBufferSize, charset, readMode, postReadFileActionFunc = postReadFileActionFunc_noop)
              
              val mockCallback = mockFunction[FileReadData, Unit]
              val capturedFileReadData = CaptureAll[FileReadData]()
              mockCallback expects capture(capturedFileReadData) once
              
              
              fileReader.read(mockCallback, ReadScheduleItem(logFile.file, 0, byteLen(multiLineText), logFile.lastModified, newerFilesWithSharedLastModified))
              
              
              
              calledBackDataIsSimilarTo(
                capturedFileReadData,
                FileReadData(
                  readData=expected,
                  baseFile=null,
                  physicalFile=logFile.absolutePath,
                  readEndPos=byteLen(multiLineText),
                  writeTimestamp=logFile.lastModified,
                  readTimestamp = -1,
                  newerFilesWithSharedLastModified
                )
              )
            }
            
            "in whole, if file-wise reading is active" in
            new LogFile {
              val _readMode = ReadMode.File

              val line1 = "Line1"
              val line2 = "Line2"
              val line3 = "Line3"

              val text = line1 + "\n" + line2 + "\n" + line3

              logFile = writeStringToFile(logFile, text)

              val fileReader = new FileReader(logFile.file, defaultBufferSize, charset, _readMode, postReadFileActionFunc = postReadFileActionFunc_noop)

              val mockCallback = mockFunction[FileReadData, Unit]
              val capturedFileReadData = CaptureAll[FileReadData]()
              mockCallback expects capture(capturedFileReadData) once
              
              fileReader.read(mockCallback, ReadScheduleItem(logFile.file, 0, byteLen(text), logFile.lastModified, newerFilesWithSharedLastModified))

              calledBackDataIsSimilarTo(
                capturedFileReadData,
                FileReadData(
                  readData=text,
                  baseFile=null,
                  physicalFile=logFile.absolutePath,
                  readEndPos=byteLen(text),
                  writeTimestamp=logFile.lastModified,
                  readTimestamp = -1,
                  newerFilesWithSharedLastModified
                )
              )
            }
          }

          "should execute given actions after completing to read a file" in
          new LogFile {
            val _readMode = ReadMode.File
            val _fileCompleteAction = mockFunction[FileHandle, Unit]
            val fileReader = new FileReader(logFile.file, defaultBufferSize, charset, _readMode, postReadFileActionFunc = _fileCompleteAction)

            val mockCallback = mockFunction[FileReadData, Unit]
            mockCallback expects *
            
            _fileCompleteAction expects logFile.file
            fileReader.read(mockCallback, ReadScheduleItem(logFile.file, 0, logFile.length(), logFile.lastModified, newerFilesWithSharedLastModified))
          }
        }
      }
    }
  }
}
