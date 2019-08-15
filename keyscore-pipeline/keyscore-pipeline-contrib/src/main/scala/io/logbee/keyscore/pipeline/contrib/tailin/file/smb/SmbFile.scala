package io.logbee.keyscore.pipeline.contrib.tailin.file.smb

import java.nio.ByteBuffer
import java.nio.file.{FileSystems, Paths}
import java.util.EnumSet

import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.mserref.NtStatus
import com.hierynomus.msfscc.FileAttributes
import com.hierynomus.mssmb2.{SMB2CreateDisposition, SMB2CreateOptions, SMB2ShareAccess, SMBApiException}
import com.hierynomus.smbj.common.SmbPath
import com.hierynomus.smbj.share.DiskShare
import com.hierynomus.smbj.share.File
import io.logbee.keyscore.pipeline.contrib.tailin.file.FileHandle
import org.slf4j.LoggerFactory


class SmbFile(path: String, share: DiskShare) extends FileHandle {

  private lazy val log = LoggerFactory.getLogger(classOf[SmbFile])

  private def withFile[T](func: File => T): T = {

    var file: File = null

    try {
      file = share.openFile(
        path,
        EnumSet.of(AccessMask.GENERIC_ALL),
        EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL),
        SMB2ShareAccess.ALL,
        SMB2CreateDisposition.FILE_OPEN,
        EnumSet.noneOf(classOf[SMB2CreateOptions])
      )

      func(file)
    }
    catch {
      case exception: Throwable =>
        log.debug("Uncaught exception in SmbFile when trying to connect: " + exception.getMessage)
        throw exception
    }
    finally {
      if (file != null)
        file.close()
    }
  }


  override val absolutePath: String = withFile(_.getFileName)
  
  
  /**
   * \\hostname\share\path\to\ -> path\to\
   */
  private val pathWithinShare: String = SmbPath.parse(absolutePath).getPath


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
      case "" | null =>
        Seq.empty
      
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
  
  
  override def length: Long = withFile(_.getFileInformation.getStandardInformation.getEndOfFile)
  
  
  override def lastModified: Long = withFile(_.getFileInformation.getBasicInformation.getLastWriteTime.toEpochMillis)
  
  
  override def read(buffer: ByteBuffer, offset: Long): Int = withFile(_.read(buffer.array, offset))
  
  
  override def delete(): Unit = withFile(_.deleteOnClose())
  
  
  override def move(newPath: String) = {
    try {
      val relativeNewPath = SmbPath.parse(newPath).getPath
      withFile(file => file.rename(relativeNewPath))
    }
    catch {
      case ex: SMBApiException =>
        if (ex.getStatus == NtStatus.STATUS_OBJECT_NAME_COLLISION) {
          log.error("Could not move file '{}' to '{}', because a file already exists at that path.", absolutePath, newPath)
        }
        else {
          throw ex
        }
    }
  }
  
  
  override def tearDown(): Unit = {}
  
  
  
  def canEqual(other: Any): Boolean = other.isInstanceOf[SmbFile]
  
  override def equals(other: Any): Boolean = other match {
    case that: SmbFile =>
      (that canEqual this) &&
//        share == that.share &&
        this.absolutePath.equals(that.absolutePath)
    case _ => false
  }
  
  override def hashCode(): Int = {
    val state = Seq(/*share, */absolutePath)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
  
  override def toString: String = {
    absolutePath
  }
}
