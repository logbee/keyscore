package io.logbee.keyscore.pipeline.contrib.tailin.file.local

import java.nio.file.Path

import io.logbee.keyscore.pipeline.contrib.tailin.file.{DirChangeListener, DirHandle}


class LocalDir(dir: Path) extends DirHandle {
  
  
  override val absolutePath: String = {
    dir.toAbsolutePath.toString
  }
  
  
  override def listDirsAndFiles: (Set[LocalDir], Set[LocalFile]) = {
    val contents = dir.toFile.listFiles
    
    var dirs: Set[LocalDir] = Set.empty
    var files: Set[LocalFile] = Set.empty
    
    contents.foreach { file =>
      if (file.isDirectory) {
        dirs = dirs + new LocalDir(file.toPath)
      }
      else {
        files = files + new LocalFile(file)
      }
    }
    
    (dirs, files)
  }
  
  
  override def getDirChangeListener(): DirChangeListener = new LocalDirChangeListener(this)
  
  
  override def tearDown(): Unit = {}
  
  
  
  def canEqual(other: Any): Boolean = other.isInstanceOf[LocalDir]
  
  override def equals(other: Any): Boolean = other match {
    case that: LocalDir =>
      (that canEqual this) &&
        absolutePath == that.absolutePath
    case _ => false
  }
  
  override def hashCode(): Int = {
    val state = Seq(absolutePath)
    state.map(_.hashCode).foldLeft(0)((a, b) => 31 * a + b)
  }
  
  override def toString: String = {
    absolutePath
  }
}
