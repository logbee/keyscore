package io.logbee.keyscore.pipeline.contrib.tailin.file.local

import java.io.File
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file._

import io.logbee.keyscore.pipeline.contrib.tailin.file.{DeleteFileFailedException, FileHandle, MoveFileFailedException, OpenFileHandle}
import org.slf4j.LoggerFactory

import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}

class LocalFile private (val file: File) extends FileHandle {
  private lazy val log = LoggerFactory.getLogger(classOf[LocalFile])
  
  override def absolutePath: String = file.getAbsolutePath
  
  override def open[T](func: Try[OpenFileHandle] => T): T = LocalFile.open(file)(func)
  
  override def delete(): Try[Unit] = {
    if (file.delete()) {
      Success(())
    }
    else {
      Failure(DeleteFileFailedException(s"Failed to delete file '$absolutePath'.", null))
    }
  }
  
  override def move(newPath: String): Try[Unit] = {
    try {
      Files.move(Paths.get(absolutePath), Paths.get(newPath))
      Success(())
    }
    catch {
      case ex: FileAlreadyExistsException =>
        val message = s"Could not move file '$absolutePath' to '$newPath', because a file already exists at that path."
        log.error(message, ex)
        Failure(MoveFileFailedException(message, ex))
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
  
  override def toString = s"LocalFile($absolutePath)"
}

object LocalFile {
  implicit def localFile2File(localFile: LocalFile) = localFile.file
  implicit def openLocalFile2File(localFile: OpenLocalFile) = localFile.file
  
  def apply(file: File): LocalFile = {
    new LocalFile(file)
  }
  
  private def open[T](localFile: File)(func: Try[OpenFileHandle] => T): T = {
    lazy val fileReadChannel = Files.newByteChannel(localFile.toPath, StandardOpenOption.READ).asInstanceOf[FileChannel]
    
    val result = func(Success(new OpenLocalFile(localFile, fileReadChannel)))
    
    if (fileReadChannel != null) {
      fileReadChannel.close()
    }
    
    result
  }
  
  class OpenLocalFile private[LocalFile] (val file: java.io.File, fileReadChannel: FileChannel) extends OpenFileHandle {
    
    private lazy val log = LoggerFactory.getLogger(classOf[OpenLocalFile])
    
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
    
    
    override def read(buffer: ByteBuffer, offset: Long): Int = fileReadChannel.read(buffer, offset)
    
    
    def canEqual(other: Any): Boolean = other.isInstanceOf[OpenLocalFile]
    
    override def equals(other: Any): Boolean = other match {
      case that: OpenLocalFile =>
        (that canEqual this) &&
          file == that.file
      case _ => false
    }
  
    override def hashCode(): Int = {
      val state = Seq(file)
      state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
    }

    override def toString = s"OpenLocalFile($absolutePath)"
  }
}

