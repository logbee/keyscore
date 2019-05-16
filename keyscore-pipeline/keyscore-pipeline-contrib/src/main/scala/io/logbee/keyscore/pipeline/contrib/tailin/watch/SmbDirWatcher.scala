package io.logbee.keyscore.pipeline.contrib.tailin.watch

import java.nio.file.FileSystems
import java.nio.file.Paths

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

import org.slf4j.LoggerFactory

import io.logbee.keyscore.pipeline.contrib.tailin.file.PathHandle
import io.logbee.keyscore.pipeline.contrib.tailin.file.SmbDir
import io.logbee.keyscore.pipeline.contrib.tailin.file.SmbFile

class SmbDirWatcher(watchDir: SmbDir, matchPattern: DirWatcherPattern, watcherProvider: WatcherProvider[SmbDir, SmbFile]) extends DirWatcher {
  
  private val log = LoggerFactory.getLogger(classOf[SmbDirWatcher])
  
  
  private val pattern = DirWatcherPattern.getUnixLikePath(matchPattern.fullFilePattern)
  private val fileMatcher = FileSystems.getDefault.getPathMatcher("glob:" + pattern)
  
  private val subDirWatchers = mutable.Map.empty[SmbDir, ListBuffer[DirWatcher]]
  private val subFileEventHandlers = mutable.Map.empty[SmbFile, ListBuffer[FileEventHandler]]
  
  private def subPathHandlers: mutable.Map[PathHandle, ListBuffer[PathWatcher]] =
      subDirWatchers.asInstanceOf[mutable.Map[PathHandle, ListBuffer[PathWatcher]]] ++
      subFileEventHandlers.asInstanceOf[mutable.Map[PathHandle, ListBuffer[PathWatcher]]]
  
  
  //recursive setup
  val (subDirs, subFiles) = watchDir.listDirsAndFiles
  
  subDirs.foreach(addSubDirWatcher(_))
  subFiles.foreach(addSubFileEventHandler(_))
  
  
  
  private def doForEachPath(paths: Seq[PathHandle], func: PathWatcher => Unit) = {
    paths.foreach { path =>
      subPathHandlers.get(path).foreach { pathHandler =>
        pathHandler.foreach { pathHandler =>
          func(pathHandler)
        }
      }
    }
  }
  
  
  def processFileChanges() = {
    //call processFileChanges() on subDirWatchers
    subDirWatchers.foreach {
      case (path: SmbDir, subDirWatchers: ListBuffer[DirWatcher]) => subDirWatchers.foreach {
        case (watcher: DirWatcher) => watcher.processFileChanges()
      }
    }
    
    
    val (currentSubDirs, currentSubFiles) = watchDir.listDirsAndFiles
    
    { //process dir-changes
      val previousSubDirs = subDirWatchers.keys.toSeq
      val deletedDirs = previousSubDirs.diff(currentSubDirs)
      val dirsContinuingToExist = previousSubDirs.intersect(currentSubDirs)
      val newlyCreatedDirs = currentSubDirs.diff(previousSubDirs)
      
      doForEachPath(deletedDirs, _.pathDeleted())
      doForEachPath(dirsContinuingToExist, _.processFileChanges())
      newlyCreatedDirs.foreach {addSubDirWatcher(_)}
    }
    
    
    { //process file-changes
      val previousSubFiles = subFileEventHandlers.keys.toSeq
      val deletedFiles = previousSubFiles.diff(currentSubFiles)
      val filesContinuingToExist = previousSubFiles.intersect(currentSubFiles)
      val newlyCreatedFiles = currentSubFiles.diff(previousSubFiles)
      
      doForEachPath(deletedFiles, _.pathDeleted())
      doForEachPath(filesContinuingToExist, _.processFileChanges())
      newlyCreatedFiles.foreach {addSubFileEventHandler(_)}
    }
  }
  
  
  
  private def addSubDirWatcher(subDir: SmbDir) = {
    
    //TODO if no further subDirWatcher necessary, don't create one  -> don't use a matcher -> somehow just check that we don't need to create another dirWatcher
    // in what cases do we need another dirWatcher:
    // if there is a / anywhere
    // if there is an *,?,[ followed at some point by a /
    // if there is a ** anywhere, doesn't matter if it's followed at some point by a /
    
    val subDirWatcher = watcherProvider.createDirWatcher(
      watchDir = subDir,
      matchPattern = matchPattern.copy(depth = matchPattern.depth + 1)
    )
    
    subDirWatcher.processFileChanges()
    
    val list = subDirWatchers.getOrElse(subDir, mutable.ListBuffer.empty)
    
    subDirWatchers.put(subDir, list)
    list += subDirWatcher
  }
  
  
  
  private def addSubFileEventHandler(file: SmbFile) = {
    
    val path = Paths.get(DirWatcherPattern.getUnixLikePath(file.absolutePath))
    
    if (fileMatcher.matches(path)) {
      
      val fileEventHandler = watcherProvider.createFileEventHandler(file)
      
      fileEventHandler.processFileChanges()
      
      val list = subFileEventHandlers.getOrElse(file, mutable.ListBuffer.empty)
      
      subFileEventHandlers.put(file, list)
      list += fileEventHandler
    }
  }
  
  
  
  def fireFileModified(file: SmbFile) = {
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
        case (dir: SmbDir, _) => {
          removedSubPathHandlers = subDirWatchers.remove(dir)
        }
        case (file: SmbFile, _) => {
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
      case dir: SmbDir =>
        subDirWatchers.remove(dir) match {
          case None =>
          case Some(watchers: ListBuffer[DirWatcher]) =>
            watchers.foreach(_.pathDeleted())
        }
      case file: SmbFile =>
        subFileEventHandlers.remove(file) match {
          case None =>
          case Some(watchers: ListBuffer[FileEventHandler]) =>
            watchers.foreach(_.pathDeleted())
        }
    }
  }
  
  
  
  override def tearDown() {
    
    log.info("Teardown for " + watchDir)
    
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
