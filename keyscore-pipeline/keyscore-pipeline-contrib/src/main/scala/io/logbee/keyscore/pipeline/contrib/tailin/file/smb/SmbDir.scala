package io.logbee.keyscore.pipeline.contrib.tailin.file.smb

import java.util.EnumSet

import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.mserref.NtStatus
import com.hierynomus.msfscc.FileAttributes
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation
import com.hierynomus.mssmb2.{SMB2CreateDisposition, SMB2CreateOptions, SMB2ShareAccess, SMBApiException}
import com.hierynomus.smbj.share.{Directory, DiskShare}
import io.logbee.keyscore.pipeline.contrib.tailin.file.{DirHandle, DirNotOpenableException, OpenDirHandle}
import io.logbee.keyscore.pipeline.contrib.tailin.watch.DirChangeListener
import org.slf4j.LoggerFactory

import scala.jdk.javaapi.CollectionConverters
import scala.util.{Failure, Success, Try}


class SmbDir private (path: String, share: DiskShare) extends DirHandle[SmbDir, SmbFile] {
  private lazy val log = LoggerFactory.getLogger(classOf[SmbDir])
  
  @throws[DirNotOpenableException]
  override def open[T](func: Try[OpenDirHandle[SmbDir, SmbFile]] => T): T = SmbDir.open(path, share)(func)
  
  override def getDirChangeListener(): DirChangeListener[SmbDir, SmbFile] = new DirChangeListener(this)
  
  private val _absolutePath = SmbUtil.absolutePath(path)(share)
  override def absolutePath: String = _absolutePath
  
  def canEqual(other: Any): Boolean = other.isInstanceOf[SmbDir]
  
  override def equals(other: Any): Boolean = other match {
    case that: SmbDir =>
      (that canEqual this) &&
        absolutePath == that.absolutePath
    case _ => false
  }
  
  override def hashCode(): Int = {
    val state = Seq(absolutePath)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
  
  override def toString = s"SmbDir($absolutePath)"
}

object SmbDir {
  private lazy val log = LoggerFactory.getLogger(classOf[SmbDir])
  
  def apply(path: String, share: DiskShare): SmbDir = {
    open(path, share) {
      case Success(dir) => new SmbDir(dir.absolutePath, share)
      case Failure(ex) => throw ex
    }
  }
  
  
  @throws[DirNotOpenableException]
  private def open[T](path: String, share: DiskShare)(func: Try[OpenDirHandle[SmbDir, SmbFile]] => T): T = {
    var smbDir: Directory = null
    
    try {
      smbDir = share.openDirectory(
        SmbUtil.relativePath(path)(share),
        EnumSet.of(AccessMask.GENERIC_READ),
        EnumSet.of(FileAttributes.FILE_ATTRIBUTE_DIRECTORY),
        SMB2ShareAccess.ALL,
        SMB2CreateDisposition.FILE_OPEN,
        EnumSet.noneOf(classOf[SMB2CreateOptions])
      )

      func(Success(new OpenSmbDir(smbDir, share)))
    }
    catch {
      case ex: Throwable =>
        func(Failure(ex))
    }
    finally {
      if (smbDir != null)
        smbDir.close()
    }
  }
  
  
  class OpenSmbDir private[SmbDir] (dir: Directory, share: DiskShare) extends OpenDirHandle[SmbDir, SmbFile] {
    
    private lazy val log = LoggerFactory.getLogger(classOf[SmbDir])
    
    override val absolutePath: String = SmbUtil.absolutePath(dir.getFileName)(share)
    
    private def isDirectory(fileIdBothDirectoryInformation: FileIdBothDirectoryInformation): Boolean = {
      import com.hierynomus.msfscc.FileAttributes._
      (fileIdBothDirectoryInformation.getFileAttributes & FILE_ATTRIBUTE_DIRECTORY.getValue) == FILE_ATTRIBUTE_DIRECTORY.getValue
    }
    
    override def listDirsAndFiles: (Seq[SmbDir], Seq[SmbFile]) = {
      
      val subPaths = CollectionConverters.asScala(dir.list).toSet
        .filterNot(subPath =>
             subPath.getFileName.endsWith("\\.")
          || subPath.getFileName.equals(".")
          || subPath.getFileName.endsWith("\\..")
          || subPath.getFileName.equals("..")
        )
      
      var dirs: Seq[SmbDir] = Seq.empty
      var files: Seq[SmbFile] = Seq.empty
      
      subPaths.foreach { subPath =>
        try {
          if (isDirectory(subPath)) {
            dirs = dirs :+ SmbDir(SmbUtil.joinPath(absolutePath, subPath.getFileName + "\\"), share)
          } else {
            files = files :+ SmbFile(SmbUtil.joinPath(absolutePath, subPath.getFileName), share)
          }
        }
        catch {
          case smbException: SMBApiException =>
            smbException.getStatus match {
              case NtStatus.STATUS_DELETE_PENDING =>
                //this file/dir is being deleted, so don't add it to the listing
                log.debug(s"Listed dir which is pending to be deleted: $dir")
                
              case NtStatus.STATUS_OBJECT_NAME_NOT_FOUND =>
                //this file/dir is already deleted, so don't add it to the listing
                log.error(s"The dir to list or one of its contents does not exist.", smbException)
                
              case _ =>
                log.error(s"SMBApiException while listing files and dirs for directory: $dir", smbException)
                throw smbException
            }
            
          case otherException: Throwable =>
            log.error(s"Uncaught exception while listing files and dirs for directory: &dir", otherException)
            throw otherException
        }
      }
      
      (dirs, files)
    }
  
  
    def canEqual(other: Any): Boolean = other.isInstanceOf[OpenSmbDir]
    
    override def equals(other: Any): Boolean = other match {
      case that: OpenSmbDir =>
        (that canEqual this) &&
          this.absolutePath.equals(that.absolutePath)// &&
  //        this.share.getSmbPath.equals(that.share.getSmbPath)
      case _ => false
    }
    
    override def hashCode(): Int = {
      val state = Seq(this.absolutePath/*, this.share*/)
      state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
    }
    
    override def toString = s"OpenSmbDir($absolutePath)"
  }
}
