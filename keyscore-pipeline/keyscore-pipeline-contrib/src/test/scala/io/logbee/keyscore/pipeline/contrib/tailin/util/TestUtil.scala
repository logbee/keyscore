package io.logbee.keyscore.pipeline.contrib.tailin.util

import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.OpenOption
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.BasicFileAttributes

import org.slf4j.LoggerFactory

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy
import ch.qos.logback.core.util.FileSize

object TestUtil {

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
  
  
  def createFile(dir: Path, name: String, content: String = ""): File = {
    
    val file = dir.resolve(name).toFile
    
    file.createNewFile()
    TestUtil.waitForFileToExist(file)
    
    TestUtil.writeStringToFile(file, content, StandardOpenOption.CREATE)
    
    file
  }


  def recursivelyDelete(dir: Path) {
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
  

  def writeStringToFile(file: File, string: String, writeMode: OpenOption = StandardOpenOption.APPEND, encoding: Charset = StandardCharsets.UTF_8) {

    var fileWriter: java.io.BufferedWriter = null
    try {
      fileWriter = Files.newBufferedWriter(file.toPath, encoding, writeMode)
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
      testFileLogger.info(i +":"+ logMessage + randomString + "| ")
    }
  }
}
