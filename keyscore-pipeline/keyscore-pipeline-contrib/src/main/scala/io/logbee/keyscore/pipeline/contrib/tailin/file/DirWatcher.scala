package io.logbee.keyscore.pipeline.contrib.tailin.file

import java.io.File
import java.nio.file._
import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import org.slf4j.LoggerFactory


trait DirWatcher {
  def tearDown()
  
  def pathDeleted()

  def processEvents()
}


case class DirWatcherConfiguration(dirPath: Path, matchPattern: DirWatcherPattern)


class DefaultDirWatcher(val configuration: DirWatcherConfiguration, val watcherProvider: WatcherProvider, callback: (String) => Unit) extends PathWatcher(configuration.dirPath) with DirWatcher {
  
  private val log = LoggerFactory.getLogger(classOf[DefaultDirWatcher])
  
  
  
  if (Files.isDirectory(configuration.dirPath) == false) {
    throw new InvalidPathException(configuration.dirPath.toString, "The given path is not a directory or doesn't exist.")
  }
  
  log.info("Instantiating for " + configuration.dirPath + " with fileMatchPattern: \"" + configuration.matchPattern.fullFilePattern + "\"")
  
  private val watchService = FileSystems.getDefault.newWatchService()
  private val watchKey = configuration.dirPath.register(
    watchService,
    StandardWatchEventKinds.ENTRY_CREATE,
    StandardWatchEventKinds.ENTRY_MODIFY,
    StandardWatchEventKinds.ENTRY_DELETE)
  
  
  private val fileMatcher = FileSystems.getDefault.getPathMatcher("glob:" + configuration.matchPattern.fullFilePattern)
  
  private val subDirWatchers = mutable.Map.empty[Path, ListBuffer[DirWatcher]]
  private val subFileWatchers = mutable.Map.empty[File, ListBuffer[FileWatcher]]

  

  
  //recursive setup
  val subPaths = configuration.dirPath.toFile.listFiles
  subPaths.foreach { path =>
    {
      if (path.isDirectory) {
        addSubDirWatcher(path.toPath)
      } else {
        addSubFileWatcher(path)
      }
    }
  }
  
  
  
  
  def processEvents() = {
    //call processEvents() on subDirWatchers
    subDirWatchers.foreach {
      case (path: Path, subDirWatchers: ListBuffer[DirWatcher]) => subDirWatchers.foreach {
        case (watcher: DirWatcher) => watcher.processEvents()
      }
    }
    
    var key: Option[WatchKey] = None
    try {
      key = Option(watchService.poll)
    }
    catch {
      case e: ClosedWatchServiceException =>
        if (configuration.dirPath.toFile.isDirectory == false) {
          pathDeleted()
        }
    }
    
    key.foreach(key => key.pollEvents.asScala.foreach { event =>
    
      val path: Path = configuration.dirPath.resolve(event.context.asInstanceOf[Path])
      
      event.kind match {
      
        case StandardWatchEventKinds.ENTRY_CREATE => {
          if (Files.isDirectory(path)) {
            addSubDirWatcher(path)
          } else if (Files.isRegularFile(path)) {
            addSubFileWatcher(path.toFile)
          }
        }

        case StandardWatchEventKinds.ENTRY_DELETE => {
          firePathDeleted(path)
        }

        case StandardWatchEventKinds.ENTRY_MODIFY => { //renaming a directory does not trigger this (on Linux+tmpfs at least)
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
    
    val dirWatcher = watcherProvider.createDirWatcher(
      configuration.copy(
          dirPath = subDir,
          matchPattern = configuration.matchPattern.copy(depth = configuration.matchPattern.depth + 1)
      )
    )
    
    val list = subDirWatchers.getOrElse(subDir, mutable.ListBuffer.empty)
    
    subDirWatchers.put(subDir, list)
    list += dirWatcher
  }
  
  
  
  
  private def addSubFileWatcher(file: File) = {

    if (fileMatcher.matches(file.toPath)) {
      
      val fileWatcher = watcherProvider.createFileWatcher(file)
      
      fileWatcher.fileModified(callback)
      
      val list = subFileWatchers.getOrElse(file, mutable.ListBuffer.empty)

      subFileWatchers.put(file, list)
      list += fileWatcher
    }
  }


  
  def fireFileModified(file: File) = {
    subFileWatchers.get(file) match {
      case None => //can't notify anyone
      case Some(watchers: ListBuffer[FileWatcher]) => {
        watchers.foreach(watcher => watcher.fileModified(callback))
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

    subFileWatchers.remove(path.toFile) match {
      case None =>
      case Some(watchers: ListBuffer[FileWatcher]) =>
        watchers.foreach(watcher => watcher.tearDown())
    }
  }
  
  
  
  
  def pathDeleted() {
    firePathDeleted(configuration.dirPath)
    
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
    
    subFileWatchers.remove(path.toFile) match {
      case None =>
      case Some(watchers: ListBuffer[FileWatcher]) =>
        watchers.foreach(watcher => watcher.pathDeleted())
    }
  }
  
  
  
  def tearDown() {
    
    log.info("Teardown for " + configuration.dirPath)
    
    //call tearDown on all watchers attached to this
    subFileWatchers.foreach { case (_: File, subFileWatchers: ListBuffer[FileWatcher]) =>
      subFileWatchers.foreach { case watcher =>
        watcher.tearDown()
      }
    }
    subDirWatchers.foreach { case (_: Path, subDirWatchers: ListBuffer[DirWatcher]) => 
      subDirWatchers.foreach { case watcher => 
        watcher.tearDown() 
      }
    }
    
    
    watchKey.cancel()
    watchService.close()
  }
}
