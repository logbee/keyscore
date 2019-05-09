package io.logbee.keyscore.pipeline.contrib.tailin.watch

import java.nio.file.FileSystems
import java.nio.file.Paths
import java.util.EnumSet

import scala.collection.JavaConverters
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

import org.slf4j.LoggerFactory

import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.msfscc.FileAttributes
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2CreateOptions
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.smbj.share.Directory
import com.hierynomus.smbj.share.File

import io.logbee.keyscore.pipeline.contrib.tailin.file.SmbFile

class SmbDirWatcher(dirPath: Directory, matchPattern: DirWatcherPattern, watcherProvider: WatcherProvider[Directory]) extends DirWatcher {
  
  private val log = LoggerFactory.getLogger(classOf[SmbDirWatcher])
  
  private val share = dirPath.getDiskShare
  
  //TODO
//  if (Files.isDirectory(dirPath) == false) {
//    throw new InvalidPathException(dirPath.toString, "The given path is not a directory or doesn't exist.")
//  }
  
  
  
  private val fileMatcher = FileSystems.getDefault.getPathMatcher("glob:" + matchPattern.fullFilePattern)
  
  private val subDirWatchers = mutable.Map.empty[Directory, ListBuffer[DirWatcher]]
  private val subFileEventHandlers = mutable.Map.empty[File, ListBuffer[FileEventHandler]]
  
  
  
  
  //recursive setup
  val (subDirs, subFiles) = listDirsAndFiles
  
  subDirs.foreach(addSubDirWatcher(_))
  subFiles.foreach(addSubFileEventHandler(_))
  
  
  
  def listDirsAndFiles: (Seq[Directory], Seq[File]) = {
    val subPaths = JavaConverters.asScalaBuffer(dirPath.list).toSeq
    
    val dirs = Seq.empty
    val files = Seq.empty
    
    subPaths.foreach { subPath =>
      {
        val diskEntry = share.open(
          subPath.getFileName,
          EnumSet.of(AccessMask.GENERIC_ALL),
          EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL),
          SMB2ShareAccess.ALL,
          SMB2CreateDisposition.FILE_OPEN,
          EnumSet.noneOf(classOf[SMB2CreateOptions])
        )
        
        if (diskEntry.isInstanceOf[Directory]) {
          dirs :+ diskEntry.asInstanceOf[Directory]
        } else {
          dirs :+ diskEntry.asInstanceOf[File]
        }
      }
    }
    
    (dirs, files)
  }
  
  
  
  
  def processFileChanges() = {
    //call processFileChanges() on subDirWatchers
    subDirWatchers.foreach {
      case (path: Directory, subDirWatchers: ListBuffer[DirWatcher]) => subDirWatchers.foreach {
        case (watcher: DirWatcher) => watcher.processFileChanges()
      }
    }
    
    
    val (currentSubDirs, currentSubFiles) = listDirsAndFiles
    
    val previousSubDirs = subDirWatchers.keys.toSeq
    val deletedDirs = previousSubDirs.diff(currentSubDirs)
    val dirsContinuingToExist = previousSubDirs.intersect(currentSubDirs)
    val newlyCreatedDirs = currentSubDirs.diff(previousSubDirs)
    
    def doForEachDir(directories: Seq[Directory], func: DirWatcher => Unit) = {
      directories.foreach { dir =>
        subDirWatchers.get(dir).foreach { dirWatchers =>
          dirWatchers.foreach { dirWatcher =>
            func(dirWatcher)
          }
        }
      }
    }
    
    doForEachDir(deletedDirs, _.pathDeleted())
    doForEachDir(dirsContinuingToExist, _.processFileChanges())
    newlyCreatedDirs.foreach { dir =>
      val subDirWatcher = addSubDirWatcher(dir)
      subDirWatcher.processFileChanges()
    }
    
    
    
    val previousSubFiles = subFileEventHandlers.keys.toSeq
    val deletedFiles = previousSubFiles.diff(currentSubFiles)
    val filesContinuingToExist = previousSubFiles.intersect(currentSubFiles)
    val newlyCreatedFiles = currentSubFiles.diff(previousSubFiles)
    
    def doForEachFile(files: Seq[File], func: FileEventHandler => Unit) = {
      files.foreach { file =>
        subFileEventHandlers.get(file).foreach { fileEventHandlers =>
          fileEventHandlers.foreach { fileEventHandler =>
            func(fileEventHandler)
          }
        }
      }
    }
    
    doForEachFile(deletedFiles, _.pathDeleted())
    doForEachFile(filesContinuingToExist, _.processFileChanges())
    newlyCreatedFiles.foreach { file =>
      val subFileEventHandler = addSubFileEventHandler(file)
      subFileEventHandler.foreach(_.processFileChanges())
    }
  }
  
  
  
  
  private def addSubDirWatcher(subDir: Directory): DirWatcher = {
    
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
    
    dirWatcher
  }
  
  
  
  
  private def addSubFileEventHandler(file: File): Option[FileEventHandler] = {
    
    if (fileMatcher.matches(Paths.get(file.getFileName))) { //TEST -> maybe we can only match the filesystem-path
      
      val fileEventHandler = watcherProvider.createFileEventHandler(new SmbFile(file))
      
      fileEventHandler.processFileChanges()
      
      val list = subFileEventHandlers.getOrElse(file, mutable.ListBuffer.empty)
      
      subFileEventHandlers.put(file, list)
      list += fileEventHandler
      
      Some(fileEventHandler)
    }
    else {
      None
    }
  }
  
  
  
  def fireFileModified(file: File) = {
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
    
    {
      val subDirWatcherToRemove = subDirWatchers.find {
        case (diskEntry, _) => diskEntry.getFileName equals path
      }
      
      subDirWatcherToRemove.foreach {
        subDirWatcherToRemove => subDirWatcherToRemove match {
          case (diskEntry, _) => {
            val removedSubDirWatcher = subDirWatchers.remove(diskEntry)
            
            removedSubDirWatcher.foreach {
              removedSubDirWatcher => removedSubDirWatcher.foreach(_.tearDown())
            }
          }
        }
      }
    }
    
    
    {
      val subFileEventHandlerToRemove = subFileEventHandlers.find {
        case (diskEntry, _) => diskEntry.getFileName equals path
      }
      
      subFileEventHandlerToRemove.foreach {
        subFileEventHandlerToRemove => subFileEventHandlerToRemove match {
          case (diskEntry, _) => {
            val removedSubFileEventHandler = subFileEventHandlers.remove(diskEntry)
            
            removedSubFileEventHandler.foreach {
              removedSubFileEventHandler => removedSubFileEventHandler.foreach(_.tearDown())
            }
          }
        }
      }
    }
  }
  
  
  
  
  def pathDeleted() {
    firePathDeleted(dirPath.getFileName) //TODO this doesn't do anything
    
    tearDown()
  }
  
  
  /**
   * We can't detect, whether a delete event happened for a file or for a directory,
   * however a directory and a file can't share the same name within the same directory.
   *
   * Therefore we can safely iterate over all files and directories within this directory
   * and just call PathWatcher.pathDeleted() on the one that matches the name.
   */
  private def firePathDeleted(path: String) = {
    //TODO
//    subDirWatchers.remove(path) match {
//      case None =>
//      case Some(watchers: ListBuffer[DirWatcher]) => 
//        watchers.foreach(watcher => watcher.pathDeleted())
//    }
//    
//    subFileEventHandlers.remove(path.toFile) match {
//      case None =>
//      case Some(watchers: ListBuffer[FileEventHandler]) =>
//        watchers.foreach(watcher => watcher.pathDeleted())
//    }
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
      case (_: Directory, subDirWatchers: ListBuffer[DirWatcher]) =>
        subDirWatchers.foreach {
          case watcher =>
            watcher.tearDown() 
        }
    }
    
    
    dirPath.close()
  }
}
