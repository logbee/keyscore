package io.logbee.keyscore.pipeline.contrib.tailin.watch

import java.time.{Duration, Instant}

import io.logbee.keyscore.pipeline.contrib.tailin.file.{DirHandle, DirNotOpenableException, FileHandle, PathHandle}
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.language.existentials
import scala.util.{Failure, Success, Try}


case class WatchDirNotFoundException() extends Exception


class DirWatcher[D <: DirHandle[D, F], F <: FileHandle](watchDir: DirHandle[D, F], matchPattern: FileMatchPattern[D, F], watcherProvider: WatcherProvider[D, F], processChangesErrorTimeout: Duration) extends BaseDirWatcher {
  
  private lazy val log = LoggerFactory.getLogger(classOf[DirWatcher[D, F]])
  log.debug("Initializing DirWatcher for {}", watchDir.toString)
  
  private case class PathWatcherInfo[T <: PathWatcher](dirWatcher: T, processChangesBlockedUntil: Instant)
  private val subDirWatchers = mutable.Map.empty[DirHandle[D, F], PathWatcherInfo[BaseDirWatcher]]
  private val subFileEventHandlers = mutable.Map.empty[FileHandle, PathWatcherInfo[FileEventHandler]]
  
  
  //recursive setup
  val (initialSubDirs, initialSubFiles): (Seq[_ <: DirHandle[D, F]], Seq[_ <: FileHandle]) = watchDir.open {
    case Success(watchDir) => watchDir.listDirsAndFiles
    case Failure(ex) =>
      val message = s"Could not open $watchDir when initializing DirWatcher."
      log.error(message)
      throw DirNotOpenableException(message, ex)
  }
  initialSubDirs.foreach(addSubDirWatcher(_))
  initialSubFiles.foreach(addSubFileEventHandler(_))
  
  

  private val dirChangeListener = watchDir.getDirChangeListener()
  
  def processChanges(): Try[Unit] = {
    
    dirChangeListener.computeChanges match {
      case Failure(ex) => Failure(ex)
      case Success(changes) => {
        
        changes.newlyCreatedDirs.foreach(addSubDirWatcher(_))
        changes.newlyCreatedFiles.foreach(addSubFileEventHandler(_))
        
        
        
        changes.potentiallyModifiedDirs.foldLeft[Try[Unit]](Success(())) { case (result, subDir) =>
          val processChangesResult = handleProcessChanges(subDir, subDirWatchers)
          
          (result, processChangesResult) match {
            case (result,                None) => result
            case (result: Failure[Unit], _) => result
            case (Success(()),           Some(Failure(ex: WatchDirNotFoundException))) =>
              subDirWatchers.remove(subDir)
              Failure(ex)
            case (Success(()),           Some(processChangesResult)) => processChangesResult
          }
        }
    
    
        changes.potentiallyModifiedFiles.foldLeft[Try[Unit]](Success(())) { case (result, subFile) =>
          val processChangesResult = handleProcessChanges(subFile, subFileEventHandlers)
          
          (result, processChangesResult) match {
            case (result, None) => result
            case (Success(()), Some(processChangesResult)) => processChangesResult
            case (result: Failure[Unit], _) => result
          }
        }
        
        
        changes.deletedPaths.foreach {
          case dir: DirHandle[D, F] => {
            subDirWatchers.remove(dir)
          }
          case file: FileHandle => {
            subFileEventHandlers.remove(file)
          }
        }
        
        Success(())
      }
    }
  }
  
  
  
  private def addSubDirWatcher(subDir: DirHandle[D, F]): Unit = {
    
    if (matchPattern.isSuperDir(subDir)) {
      val subDirWatcher = watcherProvider.createDirWatcher(
        watchDir = subDir,
        matchPattern
      )
      
      subDirWatcher.processChanges()
      
      subDirWatchers.put(subDir, PathWatcherInfo(subDirWatcher, Instant.now))
    }
  }
  
  
  
  private def addSubFileEventHandler(file: FileHandle): Unit = {
    
    if (matchPattern.matches(file)) {
      val fileEventHandler = watcherProvider.createFileEventHandler(file)
      
      fileEventHandler.processChanges()
      
      subFileEventHandlers.put(file, PathWatcherInfo(fileEventHandler, Instant.now))
    }
  }
  
  
  
  private def handleProcessChanges[H <: PathHandle, W <: PathWatcher](subPathHandle: H, subPathWatchers: mutable.Map[H, PathWatcherInfo[W]]): Option[Try[Unit]] = {
    val processChangesResult = subPathWatchers.get(subPathHandle).map {
      case PathWatcherInfo(subDirWatcher, processChangesBlockedUntil) if Instant.now.isAfter(processChangesBlockedUntil) => subDirWatcher.processChanges()
      case _ => Success(()) //do nothing
    }
    
    processChangesResult match {
      case Some(Failure(_)) =>
        subPathWatchers.get(subPathHandle).foreach { subPathWatcherInfo =>
          subPathWatchers.put(subPathHandle, subPathWatcherInfo.copy(processChangesBlockedUntil = Instant.now.plus(processChangesErrorTimeout)))
          val timeout = processChangesErrorTimeout.getSeconds.toString + " seconds" + {
            if (processChangesErrorTimeout.getNano > 0) {
                ", " + processChangesErrorTimeout.getNano.toString + " nanoseconds"
            }
            else ""
          }
          
          log.warn(s"Error when accessing $subPathHandle, blocking it for $timeout.")
        }
      case _ => //do nothing
    }
    
    processChangesResult
  }
}
