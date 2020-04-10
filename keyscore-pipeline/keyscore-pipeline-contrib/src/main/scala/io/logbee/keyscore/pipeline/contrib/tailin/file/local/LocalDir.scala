package io.logbee.keyscore.pipeline.contrib.tailin.file.local

import java.nio.file.Path

import io.logbee.keyscore.pipeline.contrib.tailin.file.local.LocalDir.OpenLocalDir
import io.logbee.keyscore.pipeline.contrib.tailin.file.{DirHandle, DirNotOpenableException, OpenDirHandle}
import io.logbee.keyscore.pipeline.contrib.tailin.watch.DirChangeListener

import scala.util.{Success, Try}


class LocalDir private (val localDir: Path) extends DirHandle[LocalDir, LocalFile] {
  @throws[DirNotOpenableException]
  override def open[T](func: Try[OpenDirHandle[LocalDir, LocalFile]] => T): T = func(Success(new OpenLocalDir(localDir)))

  override def getDirChangeListener(): DirChangeListener[LocalDir, LocalFile] = new DirChangeListener(this)

  override def absolutePath: String = localDir.toAbsolutePath.toString
  
  def canEqual(other: Any): Boolean = other.isInstanceOf[LocalDir]

  override def equals(other: Any): Boolean = other match {
    case that: LocalDir =>
      (that canEqual this) &&
        localDir == that.localDir
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(localDir)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }

  override def toString = s"LocalDir($absolutePath)"
}


object LocalDir {

  def apply(localDir: Path): LocalDir = {
    new LocalDir(localDir)
  }
  
  class OpenLocalDir private[LocalDir] (dir: Path) extends OpenDirHandle[LocalDir, LocalFile] {

    override val absolutePath: String = dir.toAbsolutePath.toString


    override def listDirsAndFiles: (Seq[LocalDir], Seq[LocalFile]) = {
      val contents = dir.toFile.listFiles

      var dirs: Seq[LocalDir] = Seq.empty
      var files: Seq[LocalFile] = Seq.empty

      contents.foreach { file =>
        if (file.isDirectory) {
          dirs = dirs :+ LocalDir(file.toPath)
        }
        else {
          files = files :+ LocalFile(file)
        }
      }

      (dirs, files)
    }



    def canEqual(other: Any): Boolean = other.isInstanceOf[OpenLocalDir]

    override def equals(other: Any): Boolean = other match {
      case that: OpenLocalDir =>
        (that canEqual this) &&
          absolutePath == that.absolutePath
      case _ => false
    }

    override def hashCode(): Int = {
      val state = Seq(absolutePath)
      state.map(_.hashCode).foldLeft(0)((a, b) => 31 * a + b)
    }

    override def toString = s"OpenLocalDir($absolutePath)"
  }
}

