package io.logbee.keyscore.pipeline.contrib.tailin.watch

import scala.collection.mutable

import io.logbee.keyscore.pipeline.contrib.tailin.file.DirHandle
import io.logbee.keyscore.pipeline.contrib.tailin.file.FileHandle
import io.logbee.keyscore.pipeline.contrib.tailin.file.PathHandle
import scala.language.existentials

case class DirChanges(
  newlyCreatedDirs: Set[_ <: DirHandle],
  newlyCreatedFiles: Set[_ <: FileHandle],
  deletedPaths: Set[_ <: PathHandle],
  potentiallyModifiedDirs: Set[_ <: DirHandle],
  potentiallyModifiedFiles: Set[_ <: FileHandle],
)


class DirWatcher(watchDir: DirHandle, matchPattern: FileMatchPattern, watcherProvider: WatcherProvider) extends BaseDirWatcher {
  
  private val subDirWatchers = mutable.Map.empty[DirHandle, BaseDirWatcher]
  private val subFileEventHandlers = mutable.Map.empty[FileHandle, FileEventHandler]
  
  private def subPathHandlers: mutable.Map[PathHandle, _ <: PathWatcher] = subDirWatchers ++ subFileEventHandlers
  
  
  
  
  //recursive setup
  val (initialSubDirs, initialSubFiles) = watchDir.listDirsAndFiles
  initialSubDirs.foreach(addSubDirWatcher(_))
  initialSubFiles.foreach(addSubFileEventHandler(_))
  
  
  
  
  private def doForEachPathHandler(paths: Set[_ <: PathHandle], func: PathWatcher => Unit): Unit = {
    paths.foreach { path =>
      subPathHandlers.get(path).foreach { pathHandler =>
        func(pathHandler)
      }
    }
  }
  
  
  
  def processChanges(): Unit = {
    
    val changes = watchDir.getChanges
    
    
    doForEachPathHandler(changes.potentiallyModifiedDirs, _.processChanges())
    
    
    doForEachPathHandler(changes.deletedPaths, _.pathDeleted())
    
    changes.deletedPaths.foreach {
      _ match {
        case dir: DirHandle => {
          subDirWatchers.remove(dir)
        }
        case file: FileHandle => {
          subFileEventHandlers.remove(file)
        }
      }
    }
    
    
    changes.newlyCreatedDirs.foreach {addSubDirWatcher(_)}
    changes.newlyCreatedFiles.foreach {addSubFileEventHandler(_)}
    
    
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
    subPathHandlers.foreach {
      case (_, subPathWatcher) =>
        subPathWatcher.tearDown()
    }
    
    watchDir.tearDown()
  }
}
