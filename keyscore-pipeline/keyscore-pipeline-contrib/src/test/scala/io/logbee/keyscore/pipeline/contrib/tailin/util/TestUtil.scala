package io.logbee.keyscore.pipeline.contrib.tailin.util

import java.io.{File, IOException}
import java.nio.charset.Charset
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.rolling.{FixedWindowRollingPolicy, RollingFileAppender, SizeBasedTriggeringPolicy}
import ch.qos.logback.core.util.FileSize
import io.logbee.keyscore.pipeline.contrib.tailin.file.local.LocalFile
import io.logbee.keyscore.pipeline.contrib.tailin.file.local.LocalFile.OpenLocalFile
import io.logbee.keyscore.pipeline.contrib.tailin.file.{DirHandle, FileHandle, OpenDirHandle, OpenFileHandle}
import io.logbee.keyscore.pipeline.contrib.tailin.watch.DirChangeListener
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}

object TestUtil {

  class OpenableMockDirHandle(openDirHandle: OpenDirHandle[OpenableMockDirHandle, OpenableMockFileHandle]) extends DirHandle[OpenableMockDirHandle, OpenableMockFileHandle] {
    override def open[T](dir: Try[OpenDirHandle[OpenableMockDirHandle, OpenableMockFileHandle]] => T): T = dir(Success(openDirHandle))

    override def getDirChangeListener(): DirChangeListener[OpenableMockDirHandle, OpenableMockFileHandle] = ???

    override def absolutePath: String = openDirHandle.absolutePath
  }

  class OpenableMockFileHandle(_absolutePath: String, openFileHandle: OpenFileHandle) extends FileHandle {
    override def absolutePath: String = _absolutePath
    
    override def open[T](file: Try[OpenFileHandle] => T): T = file(Success(openFileHandle))
    
    override def delete(): Try[Unit] = ???
    override def move(newPath: String): Try[Unit] = ???
    
    
    def canEqual(other: Any): Boolean = other.isInstanceOf[OpenableMockFileHandle]

    override def equals(other: Any): Boolean = other match {
      case that: OpenableMockFileHandle =>
        (that canEqual this) &&
          absolutePath == that.absolutePath
      case _ => false
    }

    override def hashCode(): Int = {
      val state = Seq(absolutePath)
      state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
    }


    override def toString = s"OpenableMockFileHandle-${super.hashCode}($absolutePath)"
  }

  def waitForFileToExist(file: File): Unit = {
    waitForFileToBe(file, deleted=false)
  }

  def waitForFileToBeDeleted(file: File): Unit = {
    waitForFileToBe(file, deleted=true)
  }

  private def waitForFileToBe(file: File, deleted: Boolean): Unit = {
    for (i <- 1 to 20) {
      if (file.exists != deleted) {
        waitForWatchService()
        return
      }
      else {
        Thread.sleep(100)
      }
    }
  }
  
  
  def createFile(dir: Path, name: String, content: String = "")(implicit charset: Charset): LocalFile = {
    
    val file = dir.resolve(name).toFile
    
    file.createNewFile()
    TestUtil.waitForFileToExist(file)
    
    TestUtil.writeStringToFile(file, content, StandardOpenOption.CREATE)
    
    LocalFile(file)
  }



  def withOpenLocalFile(dir: Path, fileName: String, content: String = "")(testCode: OpenLocalFile => Any)(implicit charset: Charset) = {

    val localFile = TestUtil.createFile(dir, fileName, content)

    try {
      localFile.open {
        case Success(localFile: OpenLocalFile) => testCode(localFile)
        case Success(_) => ???
        case Failure(ex) => throw ex
      }
    }
    finally {
      if (localFile != null) {
        localFile.delete()
      }
    }
  }


  def recursivelyDelete(dir: Path): Unit = {
    Files.walkFileTree(dir, new SimpleFileVisitor[Path]() {
      override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
        Files.delete(file)
        FileVisitResult.CONTINUE
      }

      override def postVisitDirectory(dir: Path, ex: IOException): FileVisitResult = {
        Files.delete(dir)
        FileVisitResult.CONTINUE
      }
    })
  }
  

  def waitForWatchService() = {
    Thread.sleep(10)
  }
  
  
  def writeStringToFile(file: File, string: String, writeMode: OpenOption = StandardOpenOption.APPEND)(implicit charset: Charset): Unit = {

    var fileWriter: java.io.BufferedWriter = null
    try {
      fileWriter = Files.newBufferedWriter(file.toPath, charset, writeMode)
      fileWriter.write(string)
      fileWriter.flush
    }
    finally {
      if (fileWriter != null)
        fileWriter.close
    }
  }
  
  
  def writeLogToFileWithRotation(logFile: File, numberOfLines: Int = 1000, rotatePattern: String) = {
    
    //logger setup
    val context = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
    
    val rollingFileAppender = new RollingFileAppender[ILoggingEvent]()
    rollingFileAppender.setContext(context)
    rollingFileAppender.setFile(logFile.getAbsolutePath)
    
    val rollingPolicy = new FixedWindowRollingPolicy()
    rollingPolicy.setContext(context)
    rollingPolicy.setParent(rollingFileAppender)
    rollingPolicy.setFileNamePattern(logFile.toPath.getParent.resolve(rotatePattern).toString)
    rollingPolicy.start()
    rollingFileAppender.setRollingPolicy(rollingPolicy)
    
    val triggeringPolicy = new SizeBasedTriggeringPolicy[ILoggingEvent]()
    triggeringPolicy.setMaxFileSize(FileSize.valueOf("1KB")) //relatively small to trigger rotations
    triggeringPolicy.start()
    rollingFileAppender.setTriggeringPolicy(triggeringPolicy)
    
    val encoder = new PatternLayoutEncoder()
    encoder.setContext(context)
    encoder.setPattern("%-4relative [%thread] %-5level %logger - %msg%n")
    encoder.start()
    rollingFileAppender.setEncoder(encoder)
    
    rollingFileAppender.start()
    
    val testFileLogger = context.getLogger("TestUtil")
    testFileLogger.addAppender(rollingFileAppender)
    
    testFileLogger.setAdditive(false) //stop it from also logging everything to the console
    
    
    
    //do logging
    val logMessage = "Hello Wörld "
    
    val strings = Seq("", "1", "22", "333", "4444", "55555", "666666", "7777777") //different lengths to potentially have different chars appear near the end of buffers and files
    val randomIndex = (Math.random() * strings.length).asInstanceOf[Int]
    def randomString = strings(randomIndex)
    
    for (i <- 1 to numberOfLines) {
      testFileLogger.info(s"$i:$logMessage$randomString| ")
    }
  }
}
