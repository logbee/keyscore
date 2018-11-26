package io.logbee.keyscore.contrib.tailin

import java.io.File
import java.nio.file.SimpleFileVisitor
import java.nio.file.Path
import java.nio.file.FileVisitResult
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.StandardOpenOption
import java.nio.file.OpenOption

object TestUtility {

  def waitForFileToExist(file: File): Unit = {
    waitForFileToBe(file, deleted=false)
  }

  def waitForFileToBeDeleted(file: File): Unit = {
    waitForFileToBe(file, deleted=true)
  }

  private def waitForFileToBe(file: File, deleted: Boolean): Unit = {
    for (i <- 1 to 20) {
      if (file.exists() != deleted) {
        waitForWatchService()
        return
      }
      else {
        Thread.sleep(100)
      }
    }
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
    Thread.sleep(500)
  }

  def writeStringToFile(file: File, string: String, writeMode: OpenOption) {

    var fileWriter: java.io.BufferedWriter = null
    try {
      fileWriter = Files.newBufferedWriter(file.toPath, StandardCharsets.UTF_8, writeMode)
      fileWriter.write(string)
      fileWriter.flush
    }
    finally {
      if (fileWriter != null)
        fileWriter.close
    }
  }
}
