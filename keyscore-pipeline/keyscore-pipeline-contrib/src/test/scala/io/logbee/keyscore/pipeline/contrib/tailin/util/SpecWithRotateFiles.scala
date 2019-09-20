package io.logbee.keyscore.pipeline.contrib.tailin.util

import java.nio.ByteBuffer
import java.nio.charset.{Charset, StandardCharsets}

import io.logbee.keyscore.pipeline.contrib.tailin.file.{FileHandle, OpenFileHandle}
import io.logbee.keyscore.pipeline.contrib.tailin.read.FileReadData
import io.logbee.keyscore.pipeline.contrib.tailin.util.TestUtil.OpenableMockFileHandle
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.FreeSpec
import org.scalatestplus.junit.JUnitRunner
import org.slf4j.LoggerFactory

@RunWith(classOf[JUnitRunner])
class SpecWithRotateFiles extends FreeSpec with MockFactory {
  implicit var charset: Charset = StandardCharsets.UTF_8

  def writeStringToFile(fileInfo: TestFileInfo, string: String)(implicit charset: Charset): TestFileInfo = {
    TestFileInfo(
      _absolutePath = fileInfo.absolutePath,
      content = () => string,
      _lastModified= fileInfo.lastModified,
      rotatedFiles = fileInfo.rotatedFiles,
    )
  }
  
  case object TestFileInfo {
    private lazy val log = LoggerFactory.getLogger(classOf[TestFileInfo])

    def apply(
      _absolutePath: String,
      content: () => String,
      _lastModified: Int,
      rotatedFiles: Seq[OpenableMockFileHandle] = Seq.empty,
    )(implicit charset: Charset): TestFileInfo = {

      val _name = _absolutePath.substring(_absolutePath.lastIndexOf("/") + 1)

      val openFile = new OpenFileHandle() {
        override def absolutePath: String = _absolutePath
        override def name: String = _name
        override def parent: String = ???
        override def length: Long = charset.encode(content()).limit
        override def lastModified: Long = _lastModified
        override def listRotatedFiles(rotationPattern: String): Seq[_ <: FileHandle] = rotatedFiles
        override def read(buffer: ByteBuffer, offset: Long): Int = {
          val encoded = charset.encode(content())
          val _offset = offset.asInstanceOf[Int]
          val bytesRead = Math.min(buffer.limit, encoded.limit - _offset)
          buffer.put(encoded.array, _offset, bytesRead)
          bytesRead
        }
      }

      TestFileInfo(
        file = new OpenableMockFileHandle(_absolutePath, openFile),
        openFile,
        _absolutePath,
        _name,
        content,
        length = () => charset.encode(content()).limit,
        _lastModified,
        rotatedFiles,
      )
    }
  }
  case class TestFileInfo private (
    file: OpenableMockFileHandle,
    openFile: OpenFileHandle,
    absolutePath: String,
    name: String,
    content: () => String,
    length: () => Int,
    lastModified: Int,
    rotatedFiles: Seq[OpenableMockFileHandle],
  )

  trait LogFile {
    var logFile = TestFileInfo(
      _absolutePath = "/tmp/log.txt",
      content = () => "Log_File_0_ ",
      _lastModified = 1234567890,
    )
    
    var rotationPattern = logFile.name + ".[1-5]"
  }
  
  
  trait RotateFiles extends LogFile {
    
    val logFile1337 = TestFileInfo("/tmp/log.txt.1337", () => "Log_File_1337 ", logFile.lastModified - 1000 * 3)
    
    val logFileCsv = TestFileInfo("/tmp/log.csv", () => "Log_File_Csv ", logFile.lastModified - 1000 * 3)
    
    val otherLogFile1 = TestFileInfo("/tmp/other_log.txt.1", () => "other_Log_File_1 ", logFile.lastModified - 1000 * 3)
    val otherLogFile = TestFileInfo("/tmp/other_log.txt", () => "other_Log_File ", logFile.lastModified - 1000 * 2)

    var logFile4 = TestFileInfo("/tmp/log.txt.4", () => "Log_File_4_4444 ", logFile.lastModified - 1000 * 4)
    var logFile3 = TestFileInfo("/tmp/log.txt.3", () => "Log_File_3_333 ", logFile.lastModified - 1000 * 3)
    var logFile2 = TestFileInfo("/tmp/log.txt.2", () => "Log_File_2_22 ", logFile.lastModified - 1000 * 2)
    var logFile1 = TestFileInfo("/tmp/log.txt.1", () => "Log_File_1_1 ", logFile.lastModified - 1000 * 1)
    logFile = TestFileInfo(logFile.absolutePath, logFile.content, logFile.lastModified,
                           rotatedFiles = Seq(logFile1, logFile2, logFile3, logFile4).map(_.file)
                          )
    
    var previousReadPosition = logFile3.length() / 2
    var previousReadTimestamp = logFile4.lastModified + 1
    
    def rotate(): Unit = {
      logFile4 = TestFileInfo(logFile4.absolutePath, logFile3.content, logFile3.lastModified)
      logFile3 = TestFileInfo(logFile3.absolutePath, logFile2.content, logFile2.lastModified)
      logFile2 = TestFileInfo(logFile2.absolutePath, logFile1.content, logFile1.lastModified)
      logFile1 = TestFileInfo(logFile1.absolutePath, logFile.content, logFile.lastModified)
      logFile = TestFileInfo(logFile.absolutePath, () => "Rotated", logFile.lastModified + 1000 * 1, Seq(logFile4, logFile3, logFile2, logFile1).map(_.file))
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
