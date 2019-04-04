package io.logbee.keyscore.pipeline.contrib.tailin.file

import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.StandardOpenOption

import scala.language.implicitConversions

object LocalFile {
  implicit def localFile2File(localFile: LocalFile) = localFile.file
}

class LocalFile(val file: java.io.File) extends File {
  
  def name: String = {
    file.getName
  }
  
  def absolutePath: String = {
    file.getAbsolutePath
  }
  
  def listRotatedFiles(rotationPattern: String): Seq[LocalFile] = {
    rotationPattern match {
      case "" =>
        Seq()
      case null =>
        Seq()
      case rotationPattern =>
        val filesInSameDir = file.getParentFile.toPath.resolve(rotationPattern).getParent.toFile.listFiles //resolve a relative path, if the rotationPattern contains one
            
        if (filesInSameDir == null) //if the directory doesn't exist
          Seq()
        else {
          val rotateMatcher = FileSystems.getDefault.getPathMatcher("glob:" + file.getParent + "/" + rotationPattern)
          
          val rotatedFilesInSameDir = filesInSameDir.filter(fileInSameDir => rotateMatcher.matches(fileInSameDir.toPath))
          
          rotatedFilesInSameDir.toSeq.map(file => new LocalFile(file))
        }
    }
  }
  
  def length: Long = {
    file.length
  }
  
  def lastModified: Long = {
    file.lastModified
  }
  
  
  private lazy val fileReadChannel = Files.newByteChannel(file.toPath, StandardOpenOption.READ).asInstanceOf[FileChannel]
  
  def read(buffer: ByteBuffer, offset: Long): Int = {
    fileReadChannel.read(buffer, offset)
  }
  
  
  
  //Currently, this is only necessary in LocalFile (for testing)
  override def equals(other: Any): Boolean = {
    other match {
      case that: LocalFile =>
        this.isInstanceOf[LocalFile] && file == that.file
      case _ => false
    }
  }
  
  
  def tearDown() = {
    if (fileReadChannel != null) {
      fileReadChannel.close()
    }
  }
}
