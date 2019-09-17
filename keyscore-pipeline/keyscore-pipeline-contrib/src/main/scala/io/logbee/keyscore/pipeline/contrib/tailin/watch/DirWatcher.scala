package io.logbee.keyscore.pipeline.contrib.tailin.watch

import io.logbee.keyscore.pipeline.contrib.tailin.file.DirHandle
import io.logbee.keyscore.pipeline.contrib.tailin.file.{DirNotOpenableException, FileHandle, PathHandle}
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.language.existentials
import scala.util.{Failure, Success}


case class WatchDirNotFoundException() extends Exception


class DirWatcher[D <: DirHandle[D, F], F <: FileHandle](watchDir: DirHandle[D, F], matchPattern: FileMatchPattern[D, F], watcherProvider: WatcherProvider[D, F]) extends BaseDirWatcher {
  
  private lazy val log = LoggerFactory.getLogger(classOf[DirWatcher[D, F]])
  log.debug("Initializing DirWatcher for {}", watchDir.toString)
  
  private val subDirWatchers = mutable.Map.empty[DirHandle[D, F], BaseDirWatcher]
  private val subFileEventHandlers = mutable.Map.empty[FileHandle, FileEventHandler]
  
  
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
  
  

  val dirChangeListener = watchDir.getDirChangeListener()
  
  @throws[WatchDirNotFoundException]
  def processChanges(): Unit = {
    
    val changes = dirChangeListener.getChanges
    

    changes.newlyCreatedDirs.foreach(addSubDirWatcher(_))
    changes.newlyCreatedFiles.foreach(addSubFileEventHandler(_))


    changes.potentiallyModifiedDirs.foreach { subDir =>
      try {
        subDirWatchers.get(subDir).foreach(_.processChanges())
      }
      catch {
        case WatchDirNotFoundException() =>
          subDirWatchers.remove(subDir)
      }
    }


    changes.potentiallyModifiedFiles.foreach { subFile =>
      subFileEventHandlers.get(subFile).foreach(_.processChanges())
    }
    
    
    changes.deletedPaths.foreach {
      case dir: DirHandle[D, F] => {
        subDirWatchers.remove(dir).foreach(_.pathDeleted())
      }
      case file: FileHandle => {
        subFileEventHandlers.remove(file).foreach(_.pathDeleted())
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
  
  
  def pathDeleted(): Unit = tearDown()

  override def tearDown(): Unit = dirChangeListener.tearDown()
}
