package io.logbee.keyscore.pipeline.contrib.tailin.file.local

import java.nio.file.{ClosedWatchServiceException, FileSystems, Files, Path, Paths, StandardWatchEventKinds, WatchEvent, WatchKey, WatchService}

import io.logbee.keyscore.pipeline.contrib.tailin.file.{DirChangeListener, DirChanges, DirHandle, FileHandle, PathHandle}

import scala.jdk.javaapi.CollectionConverters

class LocalDirChangeListener(dir: LocalDir) extends DirChangeListener(dir) {
  
  val dirPath = Paths.get(dir.absolutePath)
  
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
  
  
  
  
  override def getChanges: DirChanges = {
    
    if (Files.isDirectory(dirPath) == false) {
      return DirChanges(Set.empty,
                        Set.empty,
                        Set.empty,
                        Set.empty,
                        Set.empty,
                       )
    }
    
    
    if (watchService == null || watchKey == null) {
      watchService = FileSystems.getDefault.newWatchService()
      watchKey = dirPath.register( //TODO why do we get a WatchKey here and one by doing watchService.poll ?
        watchService,
        StandardWatchEventKinds.ENTRY_CREATE,
        StandardWatchEventKinds.ENTRY_MODIFY,
        StandardWatchEventKinds.ENTRY_DELETE)
    }
    
    
    var newlyCreatedDirs: Set[DirHandle] = Set.empty
    var newlyCreatedFiles: Set[FileHandle] = Set.empty
    var deletedPaths: Set[PathHandle] = Set.empty
    val (potentiallyModifiedDirs, _) = dir.listDirsAndFiles
    var potentiallyModifiedFiles: Set[FileHandle] = Set.empty
    
    
    var key: Option[WatchKey] = None
    try {
      key = Option(watchService.poll)
    }
    catch {
      case e: ClosedWatchServiceException =>
        if (dirPath.toFile.isDirectory == false) {
          tearDown() //TODO is this correct?
        }
    }
    
    
    key.foreach(key => CollectionConverters.asScala(key.pollEvents).toSet.foreach { event: WatchEvent[_] =>
      
      val path: Path = dirPath.resolve(event.context.asInstanceOf[Path])
      
      event.kind match {
        
        case StandardWatchEventKinds.ENTRY_CREATE => {
          if (Files.isDirectory(path)) {
            newlyCreatedDirs = newlyCreatedDirs + new LocalDir(path)
          } else if (Files.isRegularFile(path)) {
            newlyCreatedFiles = newlyCreatedFiles + new LocalFile(path.toFile) //TODO is .toFile here okay? (transforms non-existent paths to append just the path to the current working directory)
          }
        }
        
        case StandardWatchEventKinds.ENTRY_DELETE => {
          deletedPaths = deletedPaths + new LocalDir(path) //not actually necessarily a dir, we just need it to be some instance of PathHandle
        }
        
        case StandardWatchEventKinds.ENTRY_MODIFY => { //renaming a file does not trigger this (on Linux+tmpfs at least)
          potentiallyModifiedFiles = potentiallyModifiedFiles + new LocalFile(path.toFile)
        }
      }
      
      //do in all cases
      val valid: Boolean = key.reset()
      if (!valid) { //directory no longer accessible
        tearDown()
      }
    })
    
    
    
    DirChanges(newlyCreatedDirs,
               newlyCreatedFiles,
               deletedPaths,
               potentiallyModifiedDirs,
               potentiallyModifiedFiles,
              )
  }
  
  
  
  def tearDown(): Unit = {
    if (watchKey != null)
      watchKey.cancel()

    if (watchService != null)
      watchService.close()
  }
}
