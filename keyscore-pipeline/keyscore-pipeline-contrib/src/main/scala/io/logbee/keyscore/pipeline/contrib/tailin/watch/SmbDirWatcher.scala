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
import com.hierynomus.smbj.common.SmbPath
import com.hierynomus.smbj.share.Directory
import com.hierynomus.smbj.share.DiskEntry
import com.hierynomus.smbj.share.File

import io.logbee.keyscore.pipeline.contrib.tailin.file.SmbFile

class SmbDirWatcher(dirPath: Directory, matchPattern: DirWatcherPattern, watcherProvider: WatcherProvider[Directory]) extends DirWatcher {
  
  private val log = LoggerFactory.getLogger(classOf[SmbDirWatcher])
  
  private val share = dirPath.getDiskShare
  
  //TODO
//  if (Files.isDirectory(dirPath) == false) {
//    throw new InvalidPathException(dirPath.toString, "The given path is not a directory or doesn't exist.")
//  }
  
  
  private val pattern = DirWatcherPattern.getUnixLikePath(matchPattern.fullFilePattern)
  private val fileMatcher = FileSystems.getDefault.getPathMatcher("glob:" + pattern)
  
  private val subDirWatchers = mutable.Map.empty[Directory, ListBuffer[DirWatcher]]
  private val subFileEventHandlers = mutable.Map.empty[File, ListBuffer[FileEventHandler]]
  
  private def subPathWatchers: mutable.Map[DiskEntry, ListBuffer[PathWatcher]] =
      subDirWatchers.asInstanceOf[mutable.Map[DiskEntry, ListBuffer[PathWatcher]]] ++
      subFileEventHandlers.asInstanceOf[mutable.Map[DiskEntry, ListBuffer[PathWatcher]]]
  
  
  //recursive setup
  val (subDirs, subFiles) = listDirsAndFiles
  
  subDirs.foreach(addSubDirWatcher(_))
  subFiles.foreach(addSubFileEventHandler(_))
  
  
  
  def listDirsAndFiles: (Seq[Directory], Seq[File]) = {
    
    val subPaths = JavaConverters.asScalaBuffer(dirPath.list).toSeq
                     .filterNot(subPath => subPath.getFileName.endsWith("\\.")
                                        || subPath.getFileName.equals(".")
                                        || subPath.getFileName.endsWith("\\..")
                                        || subPath.getFileName.equals("..")
                                )
    
    
    var dirs: Seq[Directory] = Seq.empty
    var files: Seq[File] = Seq.empty
    
    subPaths.foreach { subPath =>
      val dirPathName = SmbPath.parse(dirPath.getFileName).getPath //just the directory's name, i.e. not the absolute path
      
      val diskEntry = share.open(
        dirPathName + subPath.getFileName,
        EnumSet.of(AccessMask.GENERIC_ALL),
        EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL),
        SMB2ShareAccess.ALL,
        SMB2CreateDisposition.FILE_OPEN,
        EnumSet.noneOf(classOf[SMB2CreateOptions])
      )
      
      if (diskEntry.isInstanceOf[Directory]) {
        dirs = dirs :+ diskEntry.asInstanceOf[Directory]
      } else {
        files = files :+ diskEntry.asInstanceOf[File]
      }
    }
    
    (dirs, files)
  }
  
  
  
  private def doForEachDiskEntry(diskEntries: Seq[DiskEntry], func: PathWatcher => Unit) = {
    diskEntries.foreach { diskEntry =>
      subPathWatchers.get(diskEntry).foreach { dirWatchers =>
        dirWatchers.foreach { dirWatcher =>
          func(dirWatcher)
        }
      }
    }
  }
  
  
  
  
  def processFileChanges() = {
    //call processFileChanges() on subDirWatchers
    subDirWatchers.foreach {
      case (path: Directory, subDirWatchers: ListBuffer[DirWatcher]) => subDirWatchers.foreach {
        case (watcher: DirWatcher) => watcher.processFileChanges()
      }
    }
    
    
    val (currentSubDirs, currentSubFiles) = listDirsAndFiles
    
    { //process dir-changes
      val previousSubDirs = subDirWatchers.keys.toSeq
      val deletedDirs = previousSubDirs.diff(currentSubDirs)
      val dirsContinuingToExist = previousSubDirs.intersect(currentSubDirs)
      val newlyCreatedDirs = currentSubDirs.diff(previousSubDirs)
      
      doForEachDiskEntry(deletedDirs, _.pathDeleted())
      doForEachDiskEntry(dirsContinuingToExist, _.processFileChanges())
      newlyCreatedDirs.foreach {addSubDirWatcher(_)}
    }
    
    
    { //process file-changes
      val previousSubFiles = subFileEventHandlers.keys.toSeq
      val deletedFiles = previousSubFiles.diff(currentSubFiles)
      val filesContinuingToExist = previousSubFiles.intersect(currentSubFiles)
      val newlyCreatedFiles = currentSubFiles.diff(previousSubFiles)
      
      doForEachDiskEntry(deletedFiles, _.pathDeleted())
      doForEachDiskEntry(filesContinuingToExist, _.processFileChanges())
      newlyCreatedFiles.foreach {addSubFileEventHandler(_)}
    }
  }
  
  
  
  
  private def addSubDirWatcher(subDir: Directory) = {
    
    //TODO if no further subDirWatcher necessary, don't create one  -> don't use a matcher -> somehow just check that we don't need to create another dirWatcher
    // in what cases do we need another dirWatcher:
    // if there is a / anywhere
    // if there is an *,?,[ followed at some point by a /
    // if there is a ** anywhere, doesn't matter if it's followed at some point by a /
    
    val subDirWatcher = watcherProvider.createDirWatcher(
      dirPath = subDir,
      matchPattern = matchPattern.copy(depth = matchPattern.depth + 1)
    )
    
    subDirWatcher.processFileChanges()
    
    val list = subDirWatchers.getOrElse(subDir, mutable.ListBuffer.empty)
    
    subDirWatchers.put(subDir, list)
    list += subDirWatcher
  }
  
  
  
  
  private def addSubFileEventHandler(file: File) = {
    
    val path = Paths.get(DirWatcherPattern.getUnixLikePath(file.getFileName))
    
    if (fileMatcher.matches(path)) {
      
      val fileEventHandler = watcherProvider.createFileEventHandler(new SmbFile(file))
      
      fileEventHandler.processFileChanges()
      
      val list = subFileEventHandlers.getOrElse(file, mutable.ListBuffer.empty)
      
      subFileEventHandlers.put(file, list)
      list += fileEventHandler
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
    
    val subDiskEntryHandlerToRemove = subPathWatchers.find {
      case (diskEntry, _) => diskEntry.getFileName equals path
    }
    
    subDiskEntryHandlerToRemove.foreach {
      var removedSubDiskEntryHandlers: Option[ListBuffer[_ <: PathWatcher]] = null
      
      subDiskEntryHandlerToRemove => subDiskEntryHandlerToRemove match {
        case (diskEntry: Directory, _) => {
          removedSubDiskEntryHandlers = subDirWatchers.remove(diskEntry)
        }
        case (diskEntry: File, _) => {
          removedSubDiskEntryHandlers = subFileEventHandlers.remove(diskEntry)
        }
        case (_, _) => {}
      }
      
      removedSubDiskEntryHandlers.foreach {
        removedSubDiskEntryHandler => removedSubDiskEntryHandler.foreach(_.tearDown())
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
  private def firePathDeleted(path: DiskEntry) = {
    path match {
      case dir: Directory =>
        subDirWatchers.remove(dir) match {
          case None =>
          case Some(watchers: ListBuffer[DirWatcher]) =>
            watchers.foreach(_.pathDeleted())
        }
      case file: File =>
        subFileEventHandlers.remove(file) match {
          case None =>
          case Some(watchers: ListBuffer[FileEventHandler]) =>
            watchers.foreach(_.pathDeleted())
        }
    }
  }
  
  
  
  def tearDown() {
    
    log.info("Teardown for " + dirPath)
    
    //call tearDown on all watchers attached to this
    subPathWatchers.foreach {
      case (_, subPathWatchers) =>
        subPathWatchers.foreach {
          case subPathWatcher =>
            subPathWatcher.tearDown()
        }
    }
    
    
    dirPath.flush()
    dirPath.close()
  }
}
