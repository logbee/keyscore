package io.logbee.keyscore.pipeline.contrib.tailin.file.smb

import java.nio.ByteBuffer
import java.nio.file.{ FileSystems, Paths }
import java.util.EnumSet

import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.msfscc.FileAttributes
import com.hierynomus.mssmb2.{ SMB2CreateDisposition, SMB2CreateOptions, SMB2ShareAccess }
import com.hierynomus.smbj.common.SmbPath
import com.hierynomus.smbj.share.DiskShare
import com.hierynomus.smbj.share.File

import io.logbee.keyscore.pipeline.contrib.tailin.file.FileHandle


class SmbFile(path: String, share: DiskShare) extends FileHandle {
  
  private lazy val file: File = share.openFile(
              path,
              EnumSet.of(AccessMask.GENERIC_READ),
              EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL),
              SMB2ShareAccess.ALL,
              SMB2CreateDisposition.FILE_OPEN,
              EnumSet.noneOf(classOf[SMB2CreateOptions])
            )
  
  
  def name: String = {
    SmbPath.parse(absolutePath).getPath
  }
  
  
  def absolutePath: String = {
    file.getFileName
  }
  
  
  private def parentPath: String = {
    
    var parentPath = absolutePath
    
    val fileNameStart = parentPath.lastIndexOf("\\")
    if (fileNameStart != -1) {
      parentPath = parentPath.substring(0, fileNameStart) //cut off file-name from the end
    }
    else {
      parentPath = ""
    }
    
    parentPath
  }
  
  
  override def listRotatedFiles(rotationPattern: String): Seq[SmbFile] = {
    rotationPattern match {
      case "" | null =>
        Seq.empty
      
      case rotationPattern =>
        val rotationDir = Paths.get(parentPath).resolve(rotationPattern).getParent.toString //if the rotationPattern contains a relative path, resolve that
        
        val dirListing = share.list(rotationDir)
        
        var fileNames = Seq[String]()
        for (i <- 0 until dirListing.size) {
          fileNames = fileNames :+ dirListing.get(i).getFileName
        }
        
        val rotateMatcher = FileSystems.getDefault.getPathMatcher("glob:" + parentPath + "/" + rotationPattern)
        
        
        val rotatedFileNamesInSameDir = fileNames.filter(fileName => rotateMatcher.matches(Paths.get(rotationDir + "/" + fileName)))
        
        
        rotatedFileNamesInSameDir.map {
          fileName => new SmbFile(rotationDir + "/" + fileName, share)
        }
    }
  }
  
  
  def length: Long = {
    file.getFileInformation.getStandardInformation.getEndOfFile
  }
  
  
  def lastModified: Long = {
    file.getFileInformation.getBasicInformation.getLastWriteTime.toEpochMillis
  }
  
  
  def read(buffer: ByteBuffer, offset: Long): Int = {
    file.read(buffer.array, offset)
  }



  def canEqual(other: Any): Boolean = other.isInstanceOf[SmbFile]

  override def equals(other: Any): Boolean = other match {
    case that: SmbFile =>
      (that canEqual this) &&
//        share == that.share &&
        absolutePath == that.absolutePath
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(/*share, */absolutePath)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
  

  
  def tearDown(): Unit = {
    if (file != null) {
      file.close()
    }
  }
}
