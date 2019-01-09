package io.logbee.keyscore.pipeline.contrib.tailin.util

import java.io.File
import java.nio.file.SimpleFileVisitor
import java.nio.file.Path
import java.nio.file.FileVisitResult
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.OpenOption
import java.nio.file.StandardOpenOption
import java.nio.charset.Charset

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
}
