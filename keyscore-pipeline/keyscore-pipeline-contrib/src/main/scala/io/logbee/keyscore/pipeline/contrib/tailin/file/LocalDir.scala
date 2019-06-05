package io.logbee.keyscore.pipeline.contrib.tailin.file

import java.nio.file.Path

class LocalDir(path: Path) extends DirHandle {
  
  def absolutePath: String = {
    path.toAbsolutePath().toString()
  }
  
  
  def listDirsAndFiles: (Seq[DirHandle], Seq[FileHandle]) = {
    val contents = path.toFile.listFiles
    
    var dirs: Seq[DirHandle] = Seq.empty
    var files: Seq[FileHandle] = Seq.empty
    
    contents.foreach { file =>
      if (file.isDirectory) {
        dirs = dirs :+ new LocalDir(file.toPath)
      }
      else {
        files = files :+ new LocalFile(file)
      }
    }
    
    (dirs, files)
  }
  
  
  def tearDown(): Unit = {}
}
