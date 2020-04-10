package io.logbee.keyscore.pipeline.contrib.tailin.util

import io.logbee.keyscore.pipeline.contrib.tailin.read.FileReadRecord
import io.logbee.keyscore.pipeline.contrib.tailin.watch.ReadScheduler.FileInfo
import org.junit.runner.RunWith
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RotationHelperSpec extends SpecWithRotateFiles with Matchers {
  
  
  //TODO test what happens if null is passed as previousReadRecord
  val noPreviousReadRecord = FileReadRecord(previousReadPosition=0, previousReadTimestamp=0, newerFilesWithSharedLastModified=0)
  
  "RotationHelper should" - {
    "retrieve the list of files it has to read from," - {
      "which should contain" - {
        "only the baseFile" - {
          "if it got passed null as rotationPattern" in new LogFile {

            rotationPattern = null

            val logFileWithInfo = FileInfo(logFile.file, logFile.name, logFile.lastModified, logFile.length())
            val rotationFiles = RotationHelper.getRotationFilesToRead(logFile.openFile, logFileWithInfo, rotationPattern, noPreviousReadRecord)

            rotationFiles.length shouldBe 1
            rotationFiles(0).file shouldBe logFile.file
          }

          "if it got passed an empty rotationPattern" in new LogFile {

            rotationPattern = ""

            val logFileWithInfo = FileInfo(logFile.file, logFile.name, logFile.lastModified, logFile.length())
            val rotationFiles = RotationHelper.getRotationFilesToRead(logFile.openFile, logFileWithInfo, rotationPattern, noPreviousReadRecord)

            rotationFiles.length shouldBe 1
            rotationFiles(0).file shouldBe logFile.file
          }
        }

        "all files matching the rotationPattern," - {
          "if it got passed a non-empty rotationPattern" in new RotateFiles {

            rotationPattern = logFile.name + ".[1-2]"

            val logFileWithInfo = FileInfo(logFile.file, logFile.name, logFile.lastModified, logFile.length())
            val rotationFiles = RotationHelper.getRotationFilesToRead(logFile.openFile, logFileWithInfo, rotationPattern, noPreviousReadRecord)

            rotationFiles.map(_.file) should contain allOf (logFile.file, logFile1.file, logFile2.file)
          }
        }


        "all files matching the rotationPattern that are newer than the previousReadTimestamp," - {
          "if it got passed a non-empty rotationPattern and a previousReadTimestamp" in new RotateFiles {

            rotationPattern = logFile.name + ".[1-5]"

            val logFileWithInfo = FileInfo(logFile.file, logFile.name, logFile.lastModified, logFile.length())
            val rotationFiles = RotationHelper
              .getRotationFilesToRead(logFile.openFile, logFileWithInfo, rotationPattern, new FileReadRecord(previousReadPosition=0, previousReadTimestamp, newerFilesWithSharedLastModified=0))
              .map(_.file)

            rotationFiles should contain allOf (logFile.file, logFile1.file, logFile2.file, logFile3.file)
            rotationFiles should not contain logFile4
          }
        }
      }

      "when we have files with shared lastModified-timestamp and" - {

        "no files were deleted (simplest case)" in
        new RotateFiles {

          val sharedLastModified = logFile1.lastModified - 1000

          logFile4 = TestFileInfo(logFile4.absolutePath,
                                                                      logFile4.content,
                                                                      sharedLastModified)
          logFile3 = TestFileInfo(logFile3.absolutePath,
                                                                     logFile3.content,
                                                                     sharedLastModified)
          logFile2 = TestFileInfo(logFile2.absolutePath,
                                  logFile2.content,
                                  sharedLastModified)

          logFile = TestFileInfo(logFile.absolutePath,
                                 logFile.content,
                                 logFile.lastModified,
                                 Seq(logFile4, logFile3, logFile2, logFile1).map(_.file))


          val previousReadRecord = FileReadRecord(previousReadPosition=0,
                                                  previousReadTimestamp=logFile3.lastModified, //==sharedLastModified (except that it may be rounded down, because the filesystem has a lower resolution for lastModified-timestamps)
                                                  newerFilesWithSharedLastModified=1)

          val logFileWithInfo = FileInfo(logFile.file, logFile.name, logFile.lastModified, logFile.length())
          val rotationFiles = RotationHelper
                                .getRotationFilesToRead(logFile.openFile, logFileWithInfo, rotationPattern, previousReadRecord)
                                .map(_.file)


          rotationFiles should contain allOf (logFile3.file, logFile2.file, logFile1.file, logFile.file)
          rotationFiles should not contain logFile4.file
        }


        "1 file was deleted" ignore
        new RotateFiles {
          //TODO
        }


        "less files than the number of newerFilesWithSharedLastModified would suggest" ignore
        new RotateFiles {
          //TODO
        }


        "no files with the shared lastModified-timestamp left" ignore //TODO also do such a test without shared-lastModified-timestamp
        new RotateFiles {
          //TODO
        }
      }
    }
  }
}
