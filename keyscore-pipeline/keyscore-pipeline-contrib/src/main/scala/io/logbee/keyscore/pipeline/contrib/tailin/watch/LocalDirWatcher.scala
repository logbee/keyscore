package io.logbee.keyscore.pipeline.contrib.tailin.watch

import java.io.File
import java.nio.file.ClosedWatchServiceException
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchKey

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

import org.slf4j.LoggerFactory

import io.logbee.keyscore.pipeline.contrib.tailin.file.LocalFile


class LocalDirWatcher(dirPath: Path, matchPattern: DirWatcherPattern, watcherProvider: WatcherProvider[Path]) extends DirWatcher {
  
  private val log = LoggerFactory.getLogger(classOf[LocalDirWatcher])
  
  
  
  if (Files.isDirectory(dirPath) == false) {
    throw new InvalidPathException(dirPath.toString, "The given path is not a directory or doesn't exist.")
  }
  
  
  private val watchService = FileSystems.getDefault.newWatchService()
  private val watchKey = dirPath.register(
    watchService,
    StandardWatchEventKinds.ENTRY_CREATE,
    StandardWatchEventKinds.ENTRY_MODIFY,
    StandardWatchEventKinds.ENTRY_DELETE)
  
  
  private val fileMatcher = FileSystems.getDefault.getPathMatcher("glob:" + matchPattern.fullFilePattern)
  
  private val subDirWatchers = mutable.Map.empty[Path, ListBuffer[DirWatcher]]
  private val subFileEventHandlers = mutable.Map.empty[File, ListBuffer[FileEventHandler]]
  
  
  
  
  //recursive setup
  val subPaths = dirPath.toFile.listFiles
  subPaths.foreach { path =>
    {
      if (path.isDirectory) {
        addSubDirWatcher(path.toPath)
      } else {
        addSubFileEventHandler(path)
      }
    }
  }
  
  
  
  
  def processFileChanges() = {
    //call processEvents() on subDirWatchers
    subDirWatchers.foreach {
      case (path: Path, subDirWatchers: ListBuffer[DirWatcher]) => subDirWatchers.foreach {
        case (watcher: DirWatcher) => watcher.processFileChanges()
      }
    }
    
    var key: Option[WatchKey] = None
    try {
      key = Option(watchService.poll)
    }
    catch {
      case e: ClosedWatchServiceException =>
        if (dirPath.toFile.isDirectory == false) {
          pathDeleted()
        }
    }
    
    key.foreach(key => key.pollEvents.asScala.foreach { event =>
      
      val path: Path = dirPath.resolve(event.context.asInstanceOf[Path])
      
      event.kind match {
        
        case StandardWatchEventKinds.ENTRY_CREATE => {
          if (Files.isDirectory(path)) {
            addSubDirWatcher(path)
          } else if (Files.isRegularFile(path)) {
            addSubFileEventHandler(path.toFile)
          }
        }
        
        case StandardWatchEventKinds.ENTRY_DELETE => {
          firePathDeleted(path)
        }
        
        case StandardWatchEventKinds.ENTRY_MODIFY => { //renaming a file does not trigger this (on Linux+tmpfs at least)
          fireFileModified(path.toFile)
        }
      }
      
      //do in all cases
      val valid: Boolean = key.reset()
      if (!valid) { //directory no longer accessible
        tearDown()
      }
    })
  }
  
  
  
  
  private def addSubDirWatcher(subDir: Path) = {
    
    //TODO if no further subDirWatcher necessary, don't create one  -> don't use a matcher -> somehow just check that we don't need to create another dirWatcher
    // in what cases do we need another dirWatcher:
    // if there is a / anywhere
    // if there is an *,?,[ followed at some point by a /
    // if there is a ** anywhere, doesn't matter if it's followed at some point by a /
    
    val dirWatcher = watcherProvider.createDirWatcher(
      dirPath = subDir,
      matchPattern = matchPattern.copy(depth = matchPattern.depth + 1)
    )
    
    val list = subDirWatchers.getOrElse(subDir, mutable.ListBuffer.empty)
    
    subDirWatchers.put(subDir, list)
    list += dirWatcher
  }
  
  
  
  
  private def addSubFileEventHandler(file: File) = {
    
    if (fileMatcher.matches(file.toPath)) {
      
      val fileEventHandler = watcherProvider.createFileEventHandler(new LocalFile(file))
      
      fileEventHandler.fileModified()
      
      val list = subFileEventHandlers.getOrElse(file, mutable.ListBuffer.empty)
      
      subFileEventHandlers.put(file, list)
      list += fileEventHandler
    }
  }
  
  
  
  def fireFileModified(file: File) = {
    subFileEventHandlers.get(file) match {
      case None => //can't notify anyone
      case Some(watchers: ListBuffer[FileEventHandler]) => {
        watchers.foreach(watcher => watcher.fileModified())
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
  private def removeSubPathWatcher(path: Path) = {
    
    subDirWatchers.remove(path) match {
      case None =>
      case Some(watchers: ListBuffer[DirWatcher]) => 
        watchers.foreach(watcher => watcher.tearDown())
    }
    
    subFileEventHandlers.remove(path.toFile) match {
      case None =>
      case Some(watchers: ListBuffer[FileEventHandler]) =>
        watchers.foreach(watcher => watcher.tearDown())
    }
  }
  
  
  
  
  def pathDeleted() {
    firePathDeleted(dirPath) //TODO this doesn't do anything
    
    tearDown()
  }
  
  
  /**
   * We can't detect, whether a delete event happened for a file or for a directory,
   * however a directory and a file can't share the same name within the same directory.
   *
   * Therefore we can safely iterate over all files and directories within this directory
   * and just call PathWatcher.pathDeleted() on the one that matches the name.
   */
  private def firePathDeleted(path: Path) = {
    
    subDirWatchers.remove(path) match {
      case None =>
      case Some(watchers: ListBuffer[DirWatcher]) => 
        watchers.foreach(watcher => watcher.pathDeleted())
    }
    
    subFileEventHandlers.remove(path.toFile) match {
      case None =>
      case Some(watchers: ListBuffer[FileEventHandler]) =>
        watchers.foreach(watcher => watcher.pathDeleted())
    }
  }
  
  
  
  def tearDown() {
    
    log.info("Teardown for " + dirPath)
    
    //call tearDown on all watchers attached to this
    subFileEventHandlers.foreach {
      case (_: File, subFileEventHandlers: ListBuffer[FileEventHandler]) =>
        subFileEventHandlers.foreach {
          case watcher =>
            watcher.tearDown()
        }
    }
    subDirWatchers.foreach {
      case (_: Path, subDirWatchers: ListBuffer[DirWatcher]) =>
        subDirWatchers.foreach {
          case watcher =>
            watcher.tearDown() 
        }
    }
    
    
    watchKey.cancel()
    watchService.close()
  }
}
