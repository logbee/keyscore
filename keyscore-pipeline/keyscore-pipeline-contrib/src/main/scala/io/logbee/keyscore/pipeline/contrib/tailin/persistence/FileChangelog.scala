package io.logbee.keyscore.pipeline.contrib.tailin.persistence

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption


case class FileChange(file: File, startPos: Long, endPos: Long, lastModified: Long)



object FileChangelog {
  val encoding = StandardCharsets.UTF_8
  val | = " " //column separator
  val newline = '\n' //OS-independent line separator
}
class FileChangelog(changelogFile: File) {
  
  import io.logbee.keyscore.pipeline.contrib.tailin.persistence.FileChangelog._
  
  
  private def appendToFile(string: String) {
    var fileWriter: java.io.BufferedWriter = null
    try {
      fileWriter = Files.newBufferedWriter(changelogFile.toPath, encoding, StandardOpenOption.APPEND)
      fileWriter.write(string)
      fileWriter.flush()
    }
    finally {
      if (fileWriter != null)
        fileWriter.close
    }
  }
  
  
  def queue(fileChange: FileChange) = {
    
    val string = fileChange.file.getAbsolutePath +
                 | + fileChange.startPos +
                 | + fileChange.endPos +
                 | + fileChange.lastModified + newline
    
    appendToFile(string)
  }
  
  
  
  private def parse(line: String): FileChange = {
    val cols = line.split(|)
    
    val file = new File(cols(0))
    val startPos = Integer.parseInt(cols(1))
    val endPos = Integer.parseInt(cols(2))
    val lastModified = java.lang.Long.parseLong(cols(3))
    
    FileChange(file, startPos, endPos, lastModified)
  }
  
  
  def getNext: Option[FileChange] = {
    
    val reader = Files.newBufferedReader(changelogFile.toPath, encoding)
    
    val line = reader.readLine
    if (line != null) {
      Some(parse(line))
    }
    else {
      None
    }
  }
  
  def getLatestEntry(file: File): Option[FileChange] = { //TODO can we iterate in reverse and just stop when we've found the first entry?
    
    val pathString = file.getAbsolutePath
    
    var latestLine: String = null
    var reader: BufferedReader = null
    try {
      reader = Files.newBufferedReader(changelogFile.toPath)
      
      var line = reader.readLine
      while (line != null) {
        if (line.startsWith(pathString + |)) {
          latestLine = line
        }
        line = reader.readLine
      }
    }
    finally {
      if (reader != null) {
        reader.close()
      }
    }
    
    if (latestLine != null) {
      Some(parse(latestLine))
    }
    else {
      None
    }
  }
  
  
  def removeNext() = {
    
    var tmpWriteFile = Paths.get(changelogFile.getAbsolutePath + "-tmp")
    
    //if a file with the name of the temp-file already exists, choose a different name until one is found that isn't used
    var i = 1
    while (tmpWriteFile.toFile.exists) {
      tmpWriteFile = Paths.get(changelogFile.getAbsolutePath + "-tmp" + i)
      i += 1
    }
    
    
    tmpWriteFile.toFile.createNewFile()
    
    var reader: BufferedReader = null
    var writer: BufferedWriter = null
    try {
      reader = Files.newBufferedReader(changelogFile.toPath, encoding)
      writer = Files.newBufferedWriter(tmpWriteFile, encoding)
      
      
      reader.readLine //skip first line
      
      var line = reader.readLine
      while (line != null) {
        writer.write(line + "\n")
        line = reader.readLine //in our file-format, lines should always be terminated by a newline
      }
      
      //replace the current changelogFile with the temp-file
      changelogFile.delete()
      while (changelogFile.exists) Thread.sleep(100) //wait for the file to be deleted
      tmpWriteFile.toFile.renameTo(changelogFile)
    }
    finally {
      if (reader != null) {
        reader.close()
      }
      if (writer != null) {
        writer.close()
      }
    }
  }
}
