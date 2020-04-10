package io.logbee.keyscore.pipeline.contrib.tailin.file.smb

import java.nio.ByteBuffer
import java.nio.file.{FileSystems, Paths}
import java.util
import java.util.EnumSet

import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.mserref.NtStatus
import com.hierynomus.msfscc.FileAttributes
import com.hierynomus.mssmb2.{SMB2CreateDisposition, SMB2CreateOptions, SMB2ShareAccess, SMBApiException}
import com.hierynomus.smbj.share.{DiskShare, File}
import io.logbee.keyscore.pipeline.contrib.tailin.file.smb.SmbFile.OpenSmbFile
import io.logbee.keyscore.pipeline.contrib.tailin.file.{DeleteFileFailedException, FileHandle, MoveFileFailedException, OpenFileHandle}
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}


class SmbFile private (path: String, share: DiskShare) extends FileHandle {
  private lazy val log = LoggerFactory.getLogger(classOf[SmbFile])
  
  private val _absolutePath = SmbUtil.absolutePath(path)(share)
  override def absolutePath: String = _absolutePath
  
  override def open[R](func: Try[OpenFileHandle] => R): R = SmbFile.open(path, share, util.EnumSet.of(AccessMask.FILE_READ_DATA, AccessMask.FILE_READ_ATTRIBUTES))(smbFile => func(smbFile.map(new OpenSmbFile(_, share))))

  override def delete(): Try[Unit] = {
    SmbFile.open(path, share, util.EnumSet.of(AccessMask.DELETE)) {
      case Success(file) =>
        file.deleteOnClose()
        Success(())
      case Failure(exception) =>
        val message = s"Failed to delete file '$absolutePath'."
        log.error(message, exception)
        Failure(DeleteFileFailedException(message, exception))
    }
  }
  
  override def move(newPath: String): Try[Unit] = {
    
    SmbFile.open(path, share, util.EnumSet.of(AccessMask.DELETE)) {
      case Success(file) =>
        try {
          val relativeNewPath = SmbUtil.relativePath(newPath)(share)
          file.rename(relativeNewPath)
          Success(())
        }
        catch {
          case ex: SMBApiException =>
            if (ex.getStatus == NtStatus.STATUS_OBJECT_NAME_COLLISION) {
              val message = s"Could not move file '$absolutePath' to '$newPath', because a file already exists at that path."
              log.error(message, ex)
              Failure(MoveFileFailedException(message, ex))
            }
            else {
              val message = s"Failed to move file '$absolutePath'."
              log.error(message, ex)
              Failure(MoveFileFailedException(message, ex))
            }
        }
      case Failure(exception) =>
        val message = s"Failed to move file '$absolutePath'."
        log.error(message, exception)
        Failure(MoveFileFailedException(message, exception))
    }
  }
  
  
  def canEqual(other: Any): Boolean = other.isInstanceOf[SmbFile]
  
  override def equals(other: Any): Boolean = other match {
    case that: SmbFile =>
      (that canEqual this) &&
        absolutePath == that.absolutePath
    case _ => false
  }
  
  override def hashCode(): Int = {
    val state = Seq(absolutePath)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }

  override def toString = s"SmbFile($absolutePath)"
}

object SmbFile {
  private lazy val log = LoggerFactory.getLogger(classOf[SmbFile])
  
  def apply(path: String, share: DiskShare): SmbFile = {
    open(path, share, util.EnumSet.of(AccessMask.FILE_READ_DATA, AccessMask.FILE_READ_ATTRIBUTES)) {
      case Success(file) => new SmbFile(new OpenSmbFile(file, share).absolutePath, share)
      case Failure(ex) => throw ex
    }
  }
  
  private def open[T](path: String, share: DiskShare, accessMask: util.EnumSet[AccessMask])(func: Try[File] => T): T = {
    var smbFile: File = null
    
    try {
      smbFile = share.openFile(
        SmbUtil.relativePath(path)(share),
        accessMask,
        EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL),
        SMB2ShareAccess.ALL,
        SMB2CreateDisposition.FILE_OPEN,
        EnumSet.noneOf(classOf[SMB2CreateOptions])
      )

      func(Success(smbFile))
    }
    catch {
      case ex: Throwable =>
        func(Failure(ex))
    }
    finally {
      if (smbFile != null)
        smbFile.close()
    }
  }
  
  class OpenSmbFile private[SmbFile] (file: File, share: DiskShare) extends OpenFileHandle {
    private lazy val log = LoggerFactory.getLogger(classOf[OpenSmbFile])
    
    override val absolutePath: String = SmbUtil.absolutePath(file.getFileName)(share)
  
    override val name: String = absolutePath.substring(absolutePath.lastIndexOf("\\") + 1)
    
    override val parent: String = {
      
      val fileNameStart = absolutePath.lastIndexOf("\\")
      if (fileNameStart != -1) {
        absolutePath.substring(0, fileNameStart + 1) //cut off file-name from the end
      }
      else {
        ""
      }
    }
    
    override def listRotatedFiles(rotationPattern: String): Seq[SmbFile] = {
      rotationPattern match {
        case "" | null => Seq.empty
        
        case rotationPattern =>
          var rotationDir = Paths.get(parent).resolve(rotationPattern).getParent.toString //if the rotationPattern contains a relative path, resolve that
          rotationDir = rotationDir.substring(rotationDir.lastIndexOf("\\") + 1) //extract the dir name from the absolute path
          
          val dirListing = share.list(rotationDir)
          
          var fileNames = Seq[String]()
          for (i <- 0 until dirListing.size) {
            fileNames = fileNames :+ dirListing.get(i).getFileName
          }
          
          val rotateMatcher = FileSystems.getDefault.getPathMatcher("glob:" + parent + rotationPattern)
          
          
          val rotatedFileNamesInSameDir = fileNames.filter(fileName => rotateMatcher.matches(Paths.get(rotationDir + "/" + fileName)))
          
          
          rotatedFileNamesInSameDir.map {
            fileName => new SmbFile(rotationDir + "/" + fileName, share)
          }
      }
    }
    
    override def length: Long = file.getFileInformation.getStandardInformation.getEndOfFile
    
    override def lastModified: Long = file.getFileInformation.getBasicInformation.getLastWriteTime.toEpochMillis
    
    override def read(buffer: ByteBuffer, offset: Long): Int = file.read(buffer.array, offset)
    
    
    def canEqual(other: Any): Boolean = other.isInstanceOf[OpenSmbFile]
    
    override def equals(other: Any): Boolean = other match {
      case that: OpenSmbFile =>
        (that canEqual this) &&
  //        share == that.share &&
          this.absolutePath == that.absolutePath
      case _ => false
    }
    
    override def hashCode(): Int = {
      val state = Seq(/*share, */absolutePath)
      state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
    }
    
    override def toString = s"OpenSmbFile($absolutePath)"
  }
}
