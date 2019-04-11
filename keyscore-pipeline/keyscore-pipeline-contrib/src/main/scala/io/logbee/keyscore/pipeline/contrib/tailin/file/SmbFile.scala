package io.logbee.keyscore.pipeline.contrib.tailin.file

import java.nio.ByteBuffer

import com.hierynomus.smbj
import java.util.concurrent.TimeUnit
import java.util.EnumSet
import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.msfscc.FileAttributes
import com.hierynomus.mssmb2.SMB2CreateOptions
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2ShareAccess


class SmbFile(file: smbj.share.File) extends FileHandle {
  
  def name: String = {
    val path = file.getFileName
    path.substring(path.lastIndexOf("\\") + 1)
  }
  
  
  def absolutePath: String = {
    file.getFileName
  }
  
  
  private def parent: smbj.share.Directory = {
    var path = file.getFileName
    path = path.substring(0, path.lastIndexOf("\\")) //cut off file-name from the end
    
    //TEST
    val directory = file.getDiskShare.openDirectory(
                      path,
                      EnumSet.of(AccessMask.GENERIC_READ),
                      EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL),
                      SMB2ShareAccess.ALL,
                      SMB2CreateDisposition.FILE_OPEN,
                      EnumSet.noneOf(classOf[SMB2CreateOptions])
                    )
    
    directory
  }
  
  
  def listRotatedFiles(rotationPattern: String): Seq[SmbFile] = {
    rotationPattern match {
      case "" =>
        Seq()
      case null =>
        Seq()
      case rotationPattern =>
        ??? //TODO this.parent.resolve(rotationPattern).parent.listFiles.filter(rotationPattern)
    }
  }
  
  
  def length: Long = {
    file.getFileInformation.getStandardInformation.getEndOfFile
  }
  
  
  def lastModified: Long = {
    file.getFileInformation.getBasicInformation.getLastWriteTime.toEpochMillis()
  }
  
  
  def read(buffer: ByteBuffer, offset: Long): Int = {
    file.read(buffer.array, offset)
  }
  
  
  def tearDown() = {
    if (file != null) {
      file.close()
    }
  }
}
