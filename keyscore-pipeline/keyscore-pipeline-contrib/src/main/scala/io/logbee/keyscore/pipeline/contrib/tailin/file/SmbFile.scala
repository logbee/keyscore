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
import com.hierynomus.smbj.share.DiskShare
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation
import java.util.function.Consumer
import scala.collection.JavaConverters


class SmbFile(file: smbj.share.File) extends FileHandle {
  
  def name: String = {
    val path = file.getFileName
    path.substring(path.lastIndexOf("\\") + 1)
  }
  
  
  def absolutePath: String = {
    file.getFileName
  }
  
  
  //TODO consider moving these v private methods to some SmbTestUtil-class, so that they don't have to be private and a Spec can be written for them
  
  private def openDir(share: DiskShare, path: String): smbj.share.Directory = {
    share.openDirectory(
      path,
      EnumSet.of(AccessMask.GENERIC_READ),
      EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL),
      SMB2ShareAccess.ALL,
      SMB2CreateDisposition.FILE_OPEN,
      EnumSet.noneOf(classOf[SMB2CreateOptions])
    )
  }
  
  
  private def parent: smbj.share.Directory = {
    var path = file.getFileName
    path = path.substring(0, path.lastIndexOf("\\")) //cut off file-name from the end
    
    //TEST
    val directory = openDir(file.getDiskShare, path.substring(path.lastIndexOf("\\") + 1))
    
    directory
  }
  
  
  def listRotatedFiles(rotationPattern: String): Seq[SmbFile] = {
    rotationPattern match {
      case "" =>
        Seq()
      case null =>
        Seq()
      case rotationPattern =>
        val parentPath = this.parent.getFileName
        //FIXME already fails before this
        println("\n\n\n\n\n\n\nParent path: " + parentPath + "\n\n\n\n\n\n\n")
        
        val resolvedPath = java.nio.file.Paths.get(parentPath).resolve(rotationPattern).toString //TEST
        
        val dir = openDir(file.getDiskShare, resolvedPath)
        
        val list = dir.list(classOf[FileIdBothDirectoryInformation], rotationPattern) //TEST
        
        val seq = JavaConverters.asScalaIteratorConverter(list.iterator).asScala.toSeq //convert to Seq
        
        seq.map {fileIdBothDirectoryInformation => 
          new SmbFile(fileIdBothDirectoryInformation.asInstanceOf[smbj.share.File]) //TEST
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
  
  
  def tearDown() = {
    if (file != null) {
      file.close()
    }
  }
}
