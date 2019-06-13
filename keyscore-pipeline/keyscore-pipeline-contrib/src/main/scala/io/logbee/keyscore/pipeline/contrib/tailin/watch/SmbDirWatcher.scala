package io.logbee.keyscore.pipeline.contrib.tailin.watch

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

import io.logbee.keyscore.pipeline.contrib.tailin.file.DirHandle
import io.logbee.keyscore.pipeline.contrib.tailin.file.FileHandle
import io.logbee.keyscore.pipeline.contrib.tailin.file.PathHandle


case class DirChanges(
  newlyCreatedDirs: Set[_ <: DirHandle],
  newlyCreatedFiles: Set[_ <: FileHandle],
  deletedPaths: Set[_ <: PathHandle],
  potentiallyModifiedDirs: Set[_ <: DirHandle],
  potentiallyModifiedFiles: Set[_ <: FileHandle],
)


class SmbDirWatcher(watchDir: DirHandle, matchPattern: DirWatcherPattern, watcherProvider: SmbWatcherProvider) extends DirWatcher {
  //TODO rename to DirWatcher
  private val subDirWatchers = mutable.Map.empty[DirHandle, ListBuffer[DirWatcher]]
  private val subFileEventHandlers = mutable.Map.empty[FileHandle, ListBuffer[FileEventHandler]]
  
  private def subPathHandlers: mutable.Map[PathHandle, ListBuffer[PathWatcher]] =
      subDirWatchers.asInstanceOf[mutable.Map[PathHandle, ListBuffer[PathWatcher]]] ++
      subFileEventHandlers.asInstanceOf[mutable.Map[PathHandle, ListBuffer[PathWatcher]]]
  
  
  
  
  //recursive setup
  val (initialSubDirs, initialSubFiles) = watchDir.listDirsAndFiles
  initialSubDirs.foreach(addSubDirWatcher(_))
  initialSubFiles.foreach(addSubFileEventHandler(_))
  
  
  
  
  private def doForEachPath(paths: Set[_ <: PathHandle], func: PathWatcher => Unit) = {
    paths.foreach { path =>
      subPathHandlers.get(path).foreach { pathHandler =>
        pathHandler.foreach { pathHandler =>
          func(pathHandler)
        }
      }
    }
  }
  
  
  def processFileChanges() = { //TODO rename to processChanges()
    
    val changes = watchDir.getChanges
    
    
    doForEachPath(changes.potentiallyModifiedDirs, _.processFileChanges()) //call processFileChanges() on subDirWatchers
    
    
    doForEachPath(changes.deletedPaths, _.pathDeleted())
    
    changes.deletedPaths.foreach { path =>
      if (path.isInstanceOf[DirHandle]) {
        subDirWatchers.remove(path.asInstanceOf[DirHandle])
      } else {
        subFileEventHandlers.remove(path.asInstanceOf[FileHandle])
      }
    }
    
    
    changes.newlyCreatedDirs.foreach {addSubDirWatcher(_)}
    changes.newlyCreatedFiles.foreach {addSubFileEventHandler(_)}
    
    
    doForEachPath(changes.potentiallyModifiedFiles, _.processFileChanges())
  }
  
  
  
  private def addSubDirWatcher(subDir: DirHandle) = {
    
    if (matchPattern.isSuperDir(subDir)) {
      
      val subDirWatcher = watcherProvider.createDirWatcher(
        watchDir = subDir,
        matchPattern
      )
      
      subDirWatcher.processFileChanges()
      
      val list = subDirWatchers.getOrElse(subDir, mutable.ListBuffer.empty)
      
      subDirWatchers.put(subDir, list)
      list += subDirWatcher
    }
  }
  
  
  
  private def addSubFileEventHandler(file: FileHandle) = {
    
    if (matchPattern.matches(file)) {
      val fileEventHandler = watcherProvider.createFileEventHandler(file)
      
      fileEventHandler.processFileChanges()
      
      val list = subFileEventHandlers.getOrElse(file, mutable.ListBuffer.empty)
      
      subFileEventHandlers.put(file, list)
      list += fileEventHandler
    }
  }
  
  
  
  def fireFileModified(file: FileHandle) = {
    subFileEventHandlers.get(file) match {
      case None => //can't notify anyone
      case Some(watchers: ListBuffer[FileEventHandler]) => {
        watchers.foreach(watcher => watcher.processFileChanges())
      }
    }
  }
  
  
  
  
  
  /**
   * We can't detect, whether a delete event happened for a file or for a directory,
   * however a directory and a file can't share the same name within the same directory.
   *
   * Therefore we can safely iterate over all files and directories within this directory
   * and just PathWatcher.tearDown() the one that matches the name.
   */
  private def removeSubPathWatcher(path: String) = {
    
    val subPathHandlerToRemove = subPathHandlers.find {
      case (path, _) => path.absolutePath equals path
    }
    
    subPathHandlerToRemove.foreach {
      var removedSubPathHandlers: Option[ListBuffer[_ <: PathWatcher]] = null
      
      subPathHandlerToRemove => subPathHandlerToRemove match {
        case (dir: DirHandle, _) => {
          removedSubPathHandlers = subDirWatchers.remove(dir)
        }
        case (file: FileHandle, _) => {
          removedSubPathHandlers = subFileEventHandlers.remove(file)
        }
        case (_, _) => {}
      }
      
      removedSubPathHandlers.foreach {
        removedSubPathHandler => removedSubPathHandler.foreach(_.tearDown())
      }
    }
  }
  
  
  
  def pathDeleted() {
    tearDown()
  }
  
  
  /**
   * We can't detect, whether a delete event happened for a file or for a directory,
   * however a directory and a file can't share the same name within the same directory.
   *
   * Therefore we can safely iterate over all files and directories within this directory
   * and just call PathWatcher.pathDeleted() on the one that matches the name.
   */
  private def firePathDeleted(path: PathHandle) = {
    path match {
      case dir: DirHandle =>
        subDirWatchers.remove(dir) match {
          case None =>
          case Some(watchers: ListBuffer[DirWatcher]) =>
            watchers.foreach(_.pathDeleted())
        }
      case file: FileHandle =>
        subFileEventHandlers.remove(file) match {
          case None =>
          case Some(watchers: ListBuffer[FileEventHandler]) =>
            watchers.foreach(_.pathDeleted())
        }
    }
  }
  
  
  
  override def tearDown() {
    
    //call tearDown on all watchers attached to this
    subPathHandlers.foreach {
      case (_, subPathWatchers) =>
        subPathWatchers.foreach {
          case subPathWatcher =>
            subPathWatcher.tearDown()
        }
    }
    
    
    watchDir.tearDown()
  }
}
