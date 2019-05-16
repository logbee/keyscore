package io.logbee.keyscore.pipeline.contrib.tailin.file

import java.util.EnumSet

import scala.collection.JavaConverters

import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.msfscc.FileAttributes
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2CreateOptions
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.smbj.common.SmbPath
import com.hierynomus.smbj.share.Directory
import com.hierynomus.smbj.share.File


class SmbDir(dir: Directory) extends DirHandle {
  
  override def absolutePath = dir.getFileName
  
  
  
  override def listDirsAndFiles: (Seq[SmbDir], Seq[SmbFile]) = {
    
    val subPaths = JavaConverters.asScalaBuffer(dir.list).toSeq
                     .filterNot(subPath => subPath.getFileName.endsWith("\\.")
                                        || subPath.getFileName.equals(".")
                                        || subPath.getFileName.endsWith("\\..")
                                        || subPath.getFileName.equals("..")
                                )
    
    
    var dirs: Seq[SmbDir] = Seq.empty
    var files: Seq[SmbFile] = Seq.empty
    
    subPaths.foreach { subPath =>
      val dirPathName = SmbPath.parse(absolutePath).getPath //just the directory's name, i.e. not the absolute path
      
      val diskEntry = share.open(
        dirPathName + subPath.getFileName,
        EnumSet.of(AccessMask.GENERIC_ALL),
        EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL),
        SMB2ShareAccess.ALL,
        SMB2CreateDisposition.FILE_OPEN,
        EnumSet.noneOf(classOf[SMB2CreateOptions])
      )
      
      if (diskEntry.isInstanceOf[Directory]) {
        dirs = dirs :+ new SmbDir(diskEntry.asInstanceOf[Directory])
      } else {
        files = files :+ new SmbFile(diskEntry.asInstanceOf[File])
      }
    }
    
    (dirs, files)
  }
  
  
  
  def tearDown() = {
    dir.flush()
    dir.close()
  }
  
  
  def canEqual(other: Any): Boolean = other.isInstanceOf[SmbDir]
  
  override def equals(other: Any): Boolean = other match {
    case that: SmbDir =>
      (that canEqual this) &&
        this.absolutePath == that.absolutePath &&
        this.share == that.share
    case _ => false
  }
  
  override def hashCode(): Int = {
    val state = Seq(this.absolutePath, this.share)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
  
  
  private def share = dir.getDiskShare
}
