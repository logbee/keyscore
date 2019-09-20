package io.logbee.keyscore.pipeline.contrib.tailin.read

import java.nio.charset.Charset

import io.logbee.keyscore.pipeline.contrib.tailin.file.FileHandle
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadScheduleItem
import io.logbee.keyscore.pipeline.contrib.tailin.util.SpecWithRotateFiles
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, ParallelTestExecution}
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class FileReaderSpec extends SpecWithRotateFiles with Matchers with MockFactory with ParallelTestExecution {
  
  val defaultBufferSize = 1024

  "A FileReader" - {
    val newerFilesWithSharedLastModified = 0
    val fileCompleteActions = Seq()

    val charsetNames = Seq("UTF-8", "UTF-16LE", "UTF-32", "ISO-8859-1", "Windows-1252") //test with either "UTF-16LE" or "UTF-16BE", not "UTF-16". Otherwise our test setup writes a BOM with every string written to file.
    charsetNames.foreach {
      charsetName => {
        charset = Charset.forName(charsetName)
        
        def byteLen(string: String): Int = {
          charset.encode(string).limit
        }
        
        "with charset " + charsetName - {
          "should read the contents of its file" - {
            "line by line, if line-wise reading is active," - {

              val lineReadMode = ReadMode.Line

              "if the file contains only one line of text" in
              new LogFile {
                val line1 = "Line1"
                logFile = writeStringToFile(logFile, line1)

                val fileReader = new FileReader(logFile.file, null, defaultBufferSize, charset, lineReadMode, fileCompleteActions)

                val mockCallback = mockFunction[FileReadData, Unit]

                mockCallback expects where {
                  calledBackDataIsSimilarTo(
                    FileReadData(
                      string=line1,
                      baseFile=null,
                      physicalFile=logFile.absolutePath,
                      readEndPos=logFile.length(),
                      writeTimestamp=logFile.lastModified,
                      readTimestamp = -1,
                      newerFilesWithSharedLastModified
                    )
                  )
                }

                fileReader.read(mockCallback, ReadScheduleItem(logFile.file, 0, logFile.length(), logFile.lastModified, newerFilesWithSharedLastModified))
              }


              "if the file contains only one line of text with a newline at the end" in
              new LogFile {
                val line1 = "Line1"
                val text = line1 + "\n"
                logFile = writeStringToFile(logFile, text)

                val fileReader = new FileReader(logFile.file, null, defaultBufferSize, charset, lineReadMode, fileCompleteActions)

                val mockCallback = mockFunction[FileReadData, Unit]

                mockCallback expects where {
                  calledBackDataIsSimilarTo(
                    FileReadData(
                      string=line1,
                      baseFile=null,
                      physicalFile=logFile.absolutePath,
                      readEndPos=byteLen(text),
                      writeTimestamp=logFile.lastModified,
                      readTimestamp = -1,
                      newerFilesWithSharedLastModified
                    )
                  )
                }

                fileReader.read(mockCallback, ReadScheduleItem(logFile.file, 0, byteLen(text), logFile.lastModified, newerFilesWithSharedLastModified))
              }


              "if the file contains multiple lines of text" in
              new LogFile {
                val line1 = "Line1"
                val line2 = "Line2"
                val line3 = "Line3"
                val newline = "\n"
                val text = line1 + newline + line2 + newline + line3
                logFile = writeStringToFile(logFile, text)

                val fileReader = new FileReader(logFile.file, null, defaultBufferSize, charset, lineReadMode, fileCompleteActions)

                val mockCallback = mockFunction[FileReadData, Unit]

                inSequence {
                  mockCallback expects where {
                    calledBackDataIsSimilarTo(

                      FileReadData(
                        string=line1,
                        baseFile=null,
                        physicalFile=logFile.absolutePath,
                        readEndPos=byteLen(line1 + newline),
                        writeTimestamp=logFile.lastModified,
                        readTimestamp = -1,
                        newerFilesWithSharedLastModified
                      )
                    )
                  }

                  mockCallback expects where {
                    calledBackDataIsSimilarTo(
                      FileReadData(
                        string=line2,
                        baseFile=null,
                        physicalFile=logFile.absolutePath,
                        readEndPos=byteLen(line1 + newline + line2 + newline),
                        writeTimestamp=logFile.lastModified,
                        readTimestamp = -1,
                        newerFilesWithSharedLastModified
                      )
                    )
                  }

                  mockCallback expects where {
                    calledBackDataIsSimilarTo(
                      FileReadData(
                        string=line3,
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
                fileReader.read(mockCallback, ReadScheduleItem(logFile.file, 0, byteLen(text), logFile.lastModified, newerFilesWithSharedLastModified))
              }


              "if the file contains multiple newline-characters directly following each other" in
              new LogFile {
                val line1 = "Line1"
                val line3 = "Line3"
                val newline = "\n\n"
                val text = line1 + newline + line3
                logFile = writeStringToFile(logFile, text)

                val fileReader = new FileReader(logFile.file, null, defaultBufferSize, charset, lineReadMode, fileCompleteActions)

                val mockCallback = mockFunction[FileReadData, Unit]

                inSequence {
                  mockCallback expects where {
                    calledBackDataIsSimilarTo(
                      FileReadData(
                        string=line1,
                        baseFile=null,
                        physicalFile=logFile.absolutePath,
                        readEndPos=byteLen(line1 + newline),
                        writeTimestamp=logFile.lastModified,
                        readTimestamp = -1,
                        newerFilesWithSharedLastModified
                      )
                    )
                  }

                  mockCallback expects where {
                    calledBackDataIsSimilarTo(
                      FileReadData(
                        string=line3,
                        baseFile=null,
                        physicalFile=logFile.absolutePath,
                        readEndPos=byteLen(line1 + newline + line3),
                        writeTimestamp=logFile.lastModified,
                        readTimestamp = -1,
                        newerFilesWithSharedLastModified
                      )
                    )
                  }
                }
                fileReader.read(mockCallback, ReadScheduleItem(logFile.file, 0, byteLen(text), logFile.lastModified, newerFilesWithSharedLastModified))
              }


              "if the file contains a Windows newline \\r\\n" in
              new LogFile {
                val line1 = "Line1"
                val line2 = "Line2"
                val newline = "\r\n"
                val text = line1 + newline + line2
                logFile = writeStringToFile(logFile, text)

                val fileReader = new FileReader(logFile.file, null, defaultBufferSize, charset, lineReadMode, fileCompleteActions)

                val mockCallback = mockFunction[FileReadData, Unit]

                inSequence {
                  mockCallback expects where {
                    calledBackDataIsSimilarTo(
                      FileReadData(
                        string=line1,
                        baseFile=null,
                        physicalFile=logFile.absolutePath,
                        readEndPos=byteLen(line1 + newline),
                        writeTimestamp=logFile.lastModified,
                        readTimestamp = -1,
                        newerFilesWithSharedLastModified
                      )
                    )
                  }

                  mockCallback expects where {
                    calledBackDataIsSimilarTo(
                      FileReadData(
                        string=line2,
                        baseFile=null,
                        physicalFile=logFile.absolutePath,
                        readEndPos=byteLen(line1 + newline + line2),
                        writeTimestamp=logFile.lastModified,
                        readTimestamp = -1,
                        newerFilesWithSharedLastModified
                      )
                    )
                  }
                }
                fileReader.read(mockCallback, ReadScheduleItem(logFile.file, 0, byteLen(text), logFile.lastModified, newerFilesWithSharedLastModified))
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

                val fileReader = new FileReader(logFile.file, null, _bufferSize, charset, lineReadMode, fileCompleteActions)

                val mockCallback = mockFunction[FileReadData, Unit]

                inSequence {
                  mockCallback expects where {
                    calledBackDataIsSimilarTo(
                      FileReadData(
                        string=line1,
                        baseFile=null,
                        physicalFile=logFile.absolutePath,
                        readEndPos=byteLen(line1 + newline),
                        writeTimestamp=logFile.lastModified,
                        readTimestamp = -1,
                        newerFilesWithSharedLastModified
                      )
                    )
                  }
                  mockCallback expects where {
                    calledBackDataIsSimilarTo(
                      FileReadData(
                        string=line2,
                        baseFile=null,
                        physicalFile=logFile.absolutePath,
                        readEndPos=byteLen(line1 + newline + line2 + newline),
                        writeTimestamp=logFile.lastModified,
                        readTimestamp = -1,
                        newerFilesWithSharedLastModified
                      )
                    )
                  }

                  mockCallback expects where {
                    calledBackDataIsSimilarTo(
                      FileReadData(
                        string=line3,
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

                fileReader.read(mockCallback, ReadScheduleItem(logFile.file, 0, byteLen(text), logFile.lastModified, newerFilesWithSharedLastModified))
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

                val fileReader = new FileReader(logFile.file, null, _bufferSize, charset, lineReadMode, fileCompleteActions)

                val mockCallback = mockFunction[FileReadData, Unit]

                inSequence {
                  mockCallback expects where {
                    calledBackDataIsSimilarTo(
                      FileReadData(
                        string=line1,
                        baseFile=null,
                        physicalFile=logFile.absolutePath,
                        readEndPos=byteLen(line1 + newline),
                        writeTimestamp=logFile.lastModified,
                        readTimestamp = -1,
                        newerFilesWithSharedLastModified
                      )
                    )
                  }

                  mockCallback expects where {
                    calledBackDataIsSimilarTo(
                      FileReadData(
                        string=line2,
                        baseFile=null,
                        physicalFile=logFile.absolutePath,
                        readEndPos=byteLen(line1 + newline + line2 + newline),
                        writeTimestamp=logFile.lastModified,
                        readTimestamp = -1,
                        newerFilesWithSharedLastModified
                      )
                    )
                  }

                  mockCallback expects where {
                    calledBackDataIsSimilarTo(
                      FileReadData(
                        string=line3,
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

                fileReader.read(mockCallback, ReadScheduleItem(logFile.file, 0, byteLen(text), logFile.lastModified, newerFilesWithSharedLastModified))
              }
            }

            "in whole, if file-wise reading is active" in
            new LogFile {
              val _readMode = ReadMode.File

              val line1 = "Line1"
              val line2 = "Line2"
              val line3 = "Line3"

              val text = line1 + "\n" + line2 + "\n" + line3

              logFile = writeStringToFile(logFile, text)

              val fileReader = new FileReader(logFile.file, null, defaultBufferSize, charset, _readMode, fileCompleteActions)

              val mockCallback = mockFunction[FileReadData, Unit]

              mockCallback expects where {
                calledBackDataIsSimilarTo(
                  FileReadData(
                    string=text,
                    baseFile=null,
                    physicalFile=logFile.absolutePath,
                    readEndPos=byteLen(text),
                    writeTimestamp=logFile.lastModified,
                    readTimestamp = -1,
                    newerFilesWithSharedLastModified
                  )
                )
              }

              fileReader.read(mockCallback, ReadScheduleItem(logFile.file, 0, byteLen(text), logFile.lastModified, newerFilesWithSharedLastModified))
            }
          }

          "should execute given actions after completing to read a file" in
          new LogFile {
            val _readMode = ReadMode.File
            val fileCompleteAction = mockFunction[FileHandle, Unit]
            val _fileCompleteActions = Seq(fileCompleteAction)
            val fileReader = new FileReader(logFile.file, null, defaultBufferSize, charset, _readMode, _fileCompleteActions)

            val mockCallback = mockFunction[FileReadData, Unit]
            mockCallback expects *
            
            fileCompleteAction expects logFile.file
            fileReader.read(mockCallback, ReadScheduleItem(logFile.file, 0, logFile.length(), logFile.lastModified, newerFilesWithSharedLastModified))
          }
        }
      }
    }
  }
}
