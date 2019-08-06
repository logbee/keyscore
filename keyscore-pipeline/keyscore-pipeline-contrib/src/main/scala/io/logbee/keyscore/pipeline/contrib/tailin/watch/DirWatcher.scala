package io.logbee.keyscore.pipeline.contrib.tailin.watch

import com.hierynomus.mserref.NtStatus
import com.hierynomus.mssmb2.SMBApiException

import scala.collection.mutable
import io.logbee.keyscore.pipeline.contrib.tailin.file.DirHandle
import io.logbee.keyscore.pipeline.contrib.tailin.file.FileHandle
import io.logbee.keyscore.pipeline.contrib.tailin.file.PathHandle
import org.slf4j.LoggerFactory

import scala.language.existentials

case class DirChanges(
  newlyCreatedDirs: Set[_ <: DirHandle],
  newlyCreatedFiles: Set[_ <: FileHandle],
  deletedPaths: Set[_ <: PathHandle],
  potentiallyModifiedDirs: Set[_ <: DirHandle],
  potentiallyModifiedFiles: Set[_ <: FileHandle],
)

object WatchDirNotFoundException {
  def unapply(watchDirNotFoundException: WatchDirNotFoundException): Option[DirHandle] = Some(watchDirNotFoundException.watchDir)
}
class WatchDirNotFoundException(val watchDir: DirHandle) extends Exception


class DirWatcher(watchDir: DirHandle, matchPattern: FileMatchPattern, watcherProvider: WatcherProvider) extends BaseDirWatcher {
  
  private lazy val log = LoggerFactory.getLogger(classOf[DirWatcher])
  log.debug("Initializing DirWatcher for watchDir '{}'", watchDir.absolutePath)
  
  private val subDirWatchers = mutable.Map.empty[DirHandle, BaseDirWatcher]
  private val subFileEventHandlers = mutable.Map.empty[FileHandle, FileEventHandler]
  
  
  
  
  //recursive setup
  val (initialSubDirs, initialSubFiles) = watchDir.listDirsAndFiles
  initialSubDirs.foreach(addSubDirWatcher(_))
  initialSubFiles.foreach(addSubFileEventHandler(_))
  
  
  
  
  private def doForEachPathHandler(paths: Set[_ <: PathHandle], func: PathWatcher => Unit): Unit = {
    paths.foreach {
      _ match {
        case dir: DirHandle => subDirWatchers.get(dir).foreach(func(_))
        case file: FileHandle => subFileEventHandlers.get(file).foreach(func(_))
      }
    }
  }
  
  
  
  @throws(classOf[WatchDirNotFoundException])
  def processChanges(): Unit = {
    
    var changes: DirChanges = null
    
    try {
      changes = watchDir.getChanges
    }
    catch {
      case ex: SMBApiException =>
        ex.getStatus match {
          case NtStatus.STATUS_OBJECT_NAME_NOT_FOUND =>
            log.error(ex.getMessage)
            throw new WatchDirNotFoundException(watchDir)

          case _ =>
            throw ex
        }
    }
    
    
    
    try {
      doForEachPathHandler(changes.potentiallyModifiedDirs, _.processChanges())
    }
    catch {
      case WatchDirNotFoundException(subDir) =>
        subDirWatchers.remove(subDir).foreach(_.pathDeleted())
        subDir.tearDown()
    }
    
    
    changes.deletedPaths.foreach {
      _ match {
        case dir: DirHandle => {
          subDirWatchers.remove(dir).foreach(_.pathDeleted())
          dir.tearDown()
        }
        case file: FileHandle => {
          subFileEventHandlers.remove(file).foreach(_.pathDeleted())
          file.tearDown()
        }
      }
    }
    
    
    changes.newlyCreatedDirs.foreach(addSubDirWatcher(_))
    changes.newlyCreatedFiles.foreach(addSubFileEventHandler(_))
    
    
    doForEachPathHandler(changes.potentiallyModifiedFiles, _.processChanges())
  }
  
  
  
  private def addSubDirWatcher(subDir: DirHandle): Unit = {
    
    if (matchPattern.isSuperDir(subDir)) {
      
      val subDirWatcher = watcherProvider.createDirWatcher(
        watchDir = subDir,
        matchPattern
      )
      
      subDirWatcher.processChanges()
      
      subDirWatchers.put(subDir, subDirWatcher)
    }
  }
  
  
  
  private def addSubFileEventHandler(file: FileHandle): Unit = {
    
    if (matchPattern.matches(file)) {
      val fileEventHandler = watcherProvider.createFileEventHandler(file)
      
      fileEventHandler.processChanges()
      
      subFileEventHandlers.put(file, fileEventHandler)
    }
  }
  
  
  
  def pathDeleted(): Unit = {
    tearDown()
  }
  
  
  
  override def tearDown(): Unit = {
    
    //call tearDown on all watchers attached to this
    subDirWatchers.foreach {
      case (_, subDirWatcher) => subDirWatcher.tearDown()
    }
    subFileEventHandlers.foreach {
      case (_, subFileEventHandlers) => subFileEventHandlers.tearDown()
    }
    
    
    watchDir.tearDown()
  }
}
