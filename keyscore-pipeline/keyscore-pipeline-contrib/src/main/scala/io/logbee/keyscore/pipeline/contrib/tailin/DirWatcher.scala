package io.logbee.keyscore.pipeline.contrib.tailin

import java.io.File
import java.nio.file._

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection.mutable
import scala.collection.mutable.ListBuffer


trait DirWatcher {
  def teardown()
  
  def pathDeleted()

  def processEvents()
}

case class DirWatcherConfiguration(dirPath: Path, filePattern: String)

class DefaultDirWatcher(val configuration: DirWatcherConfiguration, val watcherProvider: WatcherProvider, callback: (String) => Unit) extends PathWatcher(configuration.dirPath) with DirWatcher {
  
  private val dirPath = configuration.dirPath
  
  
  
  private val watchService = FileSystems.getDefault().newWatchService()
  private val watchKey = dirPath.register(
    watchService,
    StandardWatchEventKinds.ENTRY_CREATE,
    StandardWatchEventKinds.ENTRY_MODIFY,
    StandardWatchEventKinds.ENTRY_DELETE)
    

  private val matcher = FileSystems.getDefault.getPathMatcher(s"glob:${dirPath.toString}${configuration.filePattern}")
  
  private val subDirWatchers = mutable.Map.empty[Path, ListBuffer[DirWatcher]]
  private val subFileWatchers = mutable.Map.empty[File, ListBuffer[FileWatcher]]

  if (Files.isDirectory(dirPath) == false) {
    throw new InvalidPathException(dirPath.toString, "The given path is not a directory or doesn't exist.")
  }

  
  //recursive setup
  val subPaths = dirPath.toFile().listFiles()
  subPaths.foreach { path =>
    {
      if (path.isDirectory()) {
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
    
    
    val key = Option(watchService.poll())
    
    key.foreach(key => key.pollEvents().asScala.foreach { event =>
    
      val path: Path = dirPath.resolve(event.context().asInstanceOf[Path])
      
      event.kind match {
      
        case StandardWatchEventKinds.ENTRY_CREATE => {
          if (Files.isDirectory(path)) {
            addSubDirWatcher(path)
          } else if (Files.isRegularFile(path)) {
            addSubFileWatcher(path.toFile())
          }
        }

        case StandardWatchEventKinds.ENTRY_DELETE => {
          firePathDeleted(path)
        }

        case StandardWatchEventKinds.ENTRY_MODIFY => { //renaming a directory does not trigger this (on Linux+tmpfs at least)
          fireFileModified(path.toFile())
        }
      }

      //do in all cases
      val valid: Boolean = key.reset()
      if (!valid) { //directory no longer accessible
        teardown()
      }
    })
  }

  
  
  
  private def addSubDirWatcher(dir: Path) = {
    
    val dirWatcher = watcherProvider.createDirWatcher(configuration.copy(dirPath = dir))
    
    val list = subDirWatchers.getOrElse(dir, mutable.ListBuffer.empty)
    
    subDirWatchers.put(dir, list)
    list += dirWatcher
  }
  
  
  
  private def addSubFileWatcher(file: File) = {

    if (matcher.matches(file.toPath)) {
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
   * and just PathWatcher.teardown() the one that matches the name.
   */
  private def removeSubPathWatcher(path: Path) = {
    
    subDirWatchers.remove(path) match {
      case None =>
      case Some(watchers: ListBuffer[DirWatcher]) => 
        watchers.foreach(watcher => watcher.teardown())
    }

    subFileWatchers.remove(path.toFile()) match {
      case None =>
      case Some(watchers: ListBuffer[FileWatcher]) =>
        watchers.foreach(watcher => watcher.teardown())
    }
  }
  
  
  
  
  def pathDeleted() {
    firePathDeleted(dirPath)
    
    teardown()
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
    
    subFileWatchers.remove(path.toFile()) match {
      case None =>
      case Some(watchers: ListBuffer[FileWatcher]) =>
        watchers.foreach(watcher => watcher.pathDeleted())
    }
  }
  
  
  
  def teardown() {
    
    //call teardown on all watchers attached to this
    subFileWatchers.foreach { case (_: File, subFileWatchers: ListBuffer[FileWatcher]) =>
      subFileWatchers.foreach { case watcher =>
        watcher.teardown
      }
    }
    subDirWatchers.foreach { case (_: Path, subDirWatchers: ListBuffer[DirWatcher]) => 
      subDirWatchers.foreach { case watcher => 
        watcher.teardown 
      }
    }
    
    
    watchKey.cancel()
    watchService.close()
  }
}
