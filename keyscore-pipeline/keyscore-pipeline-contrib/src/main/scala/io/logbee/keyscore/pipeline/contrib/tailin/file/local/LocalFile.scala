package io.logbee.keyscore.pipeline.contrib.tailin.file.local

import java.io.File
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.{FileAlreadyExistsException, FileSystems, Files, Paths, StandardOpenOption}

import scala.language.implicitConversions
import io.logbee.keyscore.pipeline.contrib.tailin.file.FileHandle
import org.slf4j.LoggerFactory

object LocalFile {
  implicit def localFile2File(localFile: LocalFile) = localFile.file
}

class LocalFile(val file: java.io.File) extends FileHandle {
  
  private lazy val log = LoggerFactory.getLogger(classOf[LocalFile])
  
  override val name: String = file.getName
  
  override val absolutePath: String = file.getAbsolutePath
  
  override val parent: String = Paths.get(absolutePath).getParent.toString + "/"
  
  def listRotatedFiles(rotationPattern: String): Seq[LocalFile] = {
    rotationPattern match {
      case "" | null => Seq()
      
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
  
  override def length: Long = file.length
  
  override def lastModified: Long = file.lastModified
  
  
  private lazy val fileReadChannel = Files.newByteChannel(file.toPath, StandardOpenOption.READ).asInstanceOf[FileChannel]
  
  override def read(buffer: ByteBuffer, offset: Long): Int = fileReadChannel.read(buffer, offset)
  
  
  override def delete(): Unit = file.delete()
  
  
  override def move(newPath: String) = {
    try {
      Files.move(Paths.get(absolutePath), Paths.get(newPath))
    }
    catch {
      case ex: FileAlreadyExistsException =>
        log.error("Could not move file '{}' to '{}', because a file already exists at that path.", absolutePath, newPath)
    }
  }
  
  
  def canEqual(other: Any): Boolean = other.isInstanceOf[LocalFile]
  
  override def equals(other: Any): Boolean = other match {
    case that: LocalFile =>
      (that canEqual this) &&
        file == that.file
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(file)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
  
  
  def tearDown() = {
    if (fileReadChannel != null) {
      fileReadChannel.close()
    }
  }
  
  override def toString: String = {
    absolutePath
  }
}
