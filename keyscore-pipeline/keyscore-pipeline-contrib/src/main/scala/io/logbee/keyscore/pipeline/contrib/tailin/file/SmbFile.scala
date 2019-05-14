package io.logbee.keyscore.pipeline.contrib.tailin.file

import java.nio.ByteBuffer
import java.nio.file.FileSystems
import java.nio.file.Paths
import java.util.EnumSet

import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.msfscc.FileAttributes
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2CreateOptions
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.smbj
import com.hierynomus.smbj.common.SmbPath


class SmbFile(val file: smbj.share.File) extends FileHandle {
  
  private val share = file.getDiskShare
  
  def name: String = {
    SmbPath.parse(absolutePath).getPath
  }
  
  
  def absolutePath: String = {
    file.getFileName
  }
  
  
  private def parentPath: String = {
    
    var filePath = SmbPath.parse(file.getFileName).getPath
    
    val fileNameStart = filePath.lastIndexOf("\\")
    if (fileNameStart != -1) {
      filePath = filePath.substring(0, fileNameStart) //cut off file-name from the end
    }
    else {
      //TODO
    }
    
    filePath
  }
  
  
  def listRotatedFiles(rotationPattern: String): Seq[SmbFile] = {
    rotationPattern match {
      case "" =>
        Seq()
      case null =>
        Seq()
      case rotationPattern =>
        val rotationDir = Paths.get(parentPath).resolve(rotationPattern).getParent.toString //if the rotationPattern contains a relative path, resolve that
        
        val dirListing = share.list(rotationDir)
        
        var fileNames = Seq[String]()
        for (i <- 0 until dirListing.size) {
          fileNames = fileNames :+ dirListing.get(i).getFileName
        }
        
        val rotateMatcher = FileSystems.getDefault.getPathMatcher("glob:" + parentPath + "/" + rotationPattern)
        
        
        val rotatedFileNamesInSameDir = fileNames.filter(fileName => rotateMatcher.matches(Paths.get(rotationDir + "/" + fileName)))
        
        
        val rotatedFilesInSameDir = rotatedFileNamesInSameDir.map {
          fileName =>
            share.openFile(
              rotationDir + "/" + fileName,
              EnumSet.of(AccessMask.GENERIC_ALL),
              EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL),
              SMB2ShareAccess.ALL,
              SMB2CreateDisposition.FILE_OPEN,
              EnumSet.noneOf(classOf[SMB2CreateOptions])
            )
        }
        
        rotatedFilesInSameDir.map(new SmbFile(_)) //wrap in SmbFile
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
  
  
  
  override def equals(other: Any): Boolean = {
    other match {
      case that: SmbFile =>
        this.isInstanceOf[SmbFile] && file.getFileName.equals(that.file.getFileName)
      case _ => false
    }
  }
  
  
  def tearDown() = {
    if (file != null) {
      file.flush()
      file.close()
    }
  }
}
