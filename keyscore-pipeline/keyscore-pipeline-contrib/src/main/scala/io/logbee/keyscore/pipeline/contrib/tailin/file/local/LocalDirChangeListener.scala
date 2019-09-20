package io.logbee.keyscore.pipeline.contrib.tailin.file.local

import java.nio.file.{ClosedWatchServiceException, FileSystems, Files, Path, Paths, StandardWatchEventKinds, WatchEvent, WatchKey, WatchService}

import io.logbee.keyscore.pipeline.contrib.tailin.file.{DirChangeListener, DirChanges, DirHandle, DirNotOpenableException, FileHandle, OpenPathHandle, PathHandle}
import io.logbee.keyscore.pipeline.contrib.tailin.watch.WatchDirNotFoundException
import org.slf4j.LoggerFactory

import scala.jdk.javaapi.CollectionConverters
import scala.util.{Failure, Success}

class LocalDirChangeListener(dir: LocalDir) extends DirChangeListener[LocalDir, LocalFile](dir) {

  private lazy val log = LoggerFactory.getLogger(classOf[LocalDirChangeListener])
  
  val dirPath = Paths.get(dir.absolutePath)
  
  var (potentiallyModifiedDirs, _) = dir.open {
    case Success(dir) => dir.listDirsAndFiles

    case Failure(ex) =>
      val message = s"Failed to open: $dir"
      log.error(message, ex)
      throw DirNotOpenableException(message, ex)
  }

  private var watchService: WatchService = _
  private var watchKey: WatchKey = _
  
  if (Files.isDirectory(dirPath)) {
    watchService = FileSystems.getDefault.newWatchService()
    watchKey = dirPath.register( //TODO why do we get a WatchKey here and one by doing watchService.poll ?
      watchService,
      StandardWatchEventKinds.ENTRY_CREATE,
      StandardWatchEventKinds.ENTRY_MODIFY,
      StandardWatchEventKinds.ENTRY_DELETE)
  }
  
  
  
  @throws[WatchDirNotFoundException]
  override def getChanges: DirChanges[LocalDir, LocalFile] = {
    
    if (watchService == null || watchKey == null) {
      watchService = FileSystems.getDefault.newWatchService()
      watchKey = dirPath.register( //TODO why do we get a WatchKey here and one by doing watchService.poll ?
        watchService,
        StandardWatchEventKinds.ENTRY_CREATE,
        StandardWatchEventKinds.ENTRY_MODIFY,
        StandardWatchEventKinds.ENTRY_DELETE)
    }
    
    
    var newlyCreatedDirs: Seq[LocalDir] = Seq.empty
    var newlyCreatedFiles: Seq[LocalFile] = Seq.empty
    var deletedPaths: Seq[PathHandle] = Seq.empty
    var potentiallyModifiedFiles: Seq[LocalFile] = Seq.empty
    
    
    var key: Option[WatchKey] = None
    try {
      key = Option(watchService.poll)
    }
    catch {
      case e: ClosedWatchServiceException =>
        if (dirPath.toFile.isDirectory == false) {
          tearDown()
          throw WatchDirNotFoundException()
        }
    }
    
    
    key.foreach(key => CollectionConverters.asScala(key.pollEvents).toSet.foreach { event: WatchEvent[_] =>
      
      val path: Path = dirPath.resolve(event.context.asInstanceOf[Path])
      
      event.kind match {
        
        case StandardWatchEventKinds.ENTRY_CREATE => {
          if (Files.isDirectory(path)) {
            newlyCreatedDirs = newlyCreatedDirs :+ LocalDir(path)
          } else if (Files.isRegularFile(path)) {
            newlyCreatedFiles = newlyCreatedFiles :+ LocalFile(path.toFile)
          }
        }
        
        case StandardWatchEventKinds.ENTRY_DELETE => {
          deletedPaths = deletedPaths :+ LocalDir(path) //not actually necessarily a dir, we just need it to be some instance of PathHandle
        }
        
        case StandardWatchEventKinds.ENTRY_MODIFY => { //renaming a file does not trigger this (on Linux+tmpfs at least)
          potentiallyModifiedFiles = potentiallyModifiedFiles :+ LocalFile(path.toFile)
        }
      }
      
      //do in all cases
      val valid: Boolean = key.reset()
      if (!valid) { //directory no longer accessible
        tearDown()
        throw WatchDirNotFoundException()
      }
    })
    
    
    
    val changes = DirChanges(
      newlyCreatedDirs,
      newlyCreatedFiles,
      deletedPaths,
      potentiallyModifiedDirs,
      potentiallyModifiedFiles,
    )

    potentiallyModifiedDirs = potentiallyModifiedDirs ++ newlyCreatedDirs

    changes
  }
  
  
  
  def tearDown(): Unit = {
    if (watchKey != null)
      watchKey.cancel()

    if (watchService != null)
      watchService.close()
  }
}
