package io.logbee.keyscore.pipeline.contrib.tailin.read

import java.nio.charset.StandardCharsets

import io.logbee.keyscore.pipeline.contrib.tailin.file.FileHandle
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.{ReadPersistence, ReadSchedule, ReadScheduleItem}
import io.logbee.keyscore.pipeline.contrib.tailin.read.FileReadRecord
import io.logbee.keyscore.pipeline.contrib.tailin.util.SpecWithRotateFiles
import org.junit.runner.RunWith
import org.scalamock.matchers.ArgCapture.CaptureAll
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class FileReaderManagerSpec extends SpecWithRotateFiles with Matchers with MockFactory {


  trait FileReaderManagerSetup extends RotateFiles {
    val readSchedule = mock[ReadSchedule]
    val readPersistence = mock[ReadPersistence]
    val fileReaderProvider = mock[FileReaderProvider]

    val fileReaderManager = new FileReaderManager(fileReaderProvider, readSchedule, readPersistence, rotationPattern)

    val callback = mockFunction[FileReadData, Unit]("callback")
    val capturedFileReadData = CaptureAll[FileReadData]()
  }



  "A FileReaderManager should" - { //TODO more tests
    def postReadFileActionFunc_noop(file: FileHandle): Unit = {}

    "read out a scheduled entry" in
    new FileReaderManagerSetup {
      inSequence {
        val readScheduleItem = ReadScheduleItem(logFile.file, startPos=previousReadPosition, endPos=logFile3.length(), previousReadTimestamp, newerFilesWithSharedLastModified=0)

        (readSchedule.dequeue _)
          .expects()
          .returning(Option(readScheduleItem))

        (readPersistence.getCompletedRead _)
          .expects(logFile.file)
          .returning(FileReadRecord(previousReadPosition, previousReadTimestamp, newerFilesWithSharedLastModified=0))

        (fileReaderProvider.create _)
          .expects(logFile3.file)
          .returning(new FileReader(logFile3.file, byteBufferSize=1024, charset=charset, readMode=ReadMode.Line, postReadFileActionFunc = postReadFileActionFunc_noop))
      }
      
      callback expects capture(capturedFileReadData) once
      
      fileReaderManager.getNextString(callback)
      
      calledBackDataIsSimilarTo(
        capturedFileReadData,
        FileReadData(
          readData = logFile3.content().substring(previousReadPosition),
          baseFile = logFile.file,
          physicalFile = logFile3.absolutePath,
          readEndPos = logFile3.length(),
          writeTimestamp = previousReadTimestamp,
          readTimestamp = -1,
          newerFilesWithSharedLastModified = 0
        )
      )
    }



    "read out multiple scheduled entries" in
    new FileReaderManagerSetup {
      inSequence {
        val readScheduleItem = ReadScheduleItem(logFile.file, startPos=previousReadPosition, endPos=logFile3.length(), previousReadTimestamp, newerFilesWithSharedLastModified=0)

        (readSchedule.dequeue _)
          .expects()
          .returning(Option(readScheduleItem))

        (readPersistence.getCompletedRead _)
          .expects(logFile.file)
          .returning(FileReadRecord(previousReadPosition, previousReadTimestamp, newerFilesWithSharedLastModified=0))

        (fileReaderProvider.create _)
          .expects(logFile3.file)
          .returning(new FileReader(fileToRead=logFile3.file,
                                    byteBufferSize=1024,
                                    charset=StandardCharsets.UTF_8,
                                    readMode=ReadMode.Line,
                                    postReadFileActionFunc = postReadFileActionFunc_noop))

        val readScheduleItem2 = ReadScheduleItem(logFile.file, startPos=0, endPos=logFile2.length(), logFile2.lastModified, newerFilesWithSharedLastModified=0)

        (readSchedule.dequeue _)
          .expects()
          .returning(Option(readScheduleItem2))

        (readPersistence.getCompletedRead _)
          .expects(logFile.file)
          .returning(FileReadRecord(previousReadPosition=logFile3.length(),
                                    previousReadTimestamp=readScheduleItem2.lastModified,
                                    newerFilesWithSharedLastModified=0))

        (fileReaderProvider.create _)
          .expects(logFile2.file)
          .returning(new FileReader(logFile2.file, byteBufferSize=1024, charset=charset, readMode=ReadMode.Line, postReadFileActionFunc = postReadFileActionFunc_noop))
      }
      
      callback expects capture(capturedFileReadData) twice
      
      fileReaderManager.getNextString(callback)
      fileReaderManager.getNextString(callback)
      
      calledBackDataIsSimilarTo(
        capturedFileReadData,
        FileReadData(
          readData = logFile3.content().substring(previousReadPosition),
          baseFile = logFile.file,
          physicalFile = logFile3.absolutePath,
          readEndPos = logFile3.length(),
          writeTimestamp = previousReadTimestamp,
          readTimestamp = -1,
          newerFilesWithSharedLastModified = 0
        ),
        FileReadData(
          readData = logFile2.content(),
          baseFile = logFile.file,
          physicalFile = logFile2.absolutePath,
          readEndPos = logFile2.length(),
          writeTimestamp = logFile2.lastModified,
          readTimestamp = -1,
          newerFilesWithSharedLastModified = 0
        )
      )
    }


    "read out a scheduled entry from the correct file when rotation has occurred after scheduling and before reading out" in
    new FileReaderManagerSetup {

      val oldLogFile3 = logFile3.copy()

      rotate()

      inSequence {
        val readScheduleItem = ReadScheduleItem(logFile.file, startPos=previousReadPosition, endPos=oldLogFile3.length(), previousReadTimestamp, newerFilesWithSharedLastModified=0)

        (readSchedule.dequeue _)
          .expects()
          .returning(Option(readScheduleItem))

        (readPersistence.getCompletedRead _)
          .expects(logFile.file)
          .returning(FileReadRecord(previousReadPosition, previousReadTimestamp, newerFilesWithSharedLastModified=0))

          //Test works, if the rotate happens beforehand, which would confirm it,
          //but changing fileReaderManager to create a new fileReader every time doesn't seem to affect it.
          //
          //maybe because we always return the same in the following, it shadows the behaviour:
          //(real test does not currently work either, though)
        (fileReaderProvider.create _)
          .expects(logFile4.file)
          .returning(new FileReader(fileToRead=logFile4.file,
                                    byteBufferSize=1024,
                                    charset=charset,
                                    readMode=ReadMode.Line,
                                    postReadFileActionFunc = postReadFileActionFunc_noop))
      }

//      rotate()
      callback expects capture(capturedFileReadData) once
      
      fileReaderManager.getNextString(callback)
      
      calledBackDataIsSimilarTo(
        capturedFileReadData,
        FileReadData(
          readData=oldLogFile3.content().substring(previousReadPosition),
          baseFile=logFile.file,
          physicalFile=logFile4.absolutePath,
          readEndPos=oldLogFile3.length(),
          writeTimestamp=previousReadTimestamp,
          readTimestamp = -1,
          newerFilesWithSharedLastModified=0
        )
      )
    }

    //TODO test where rotation happens between readings, so that the newly rotated file is directly on the file-path we just read out
  }
}
