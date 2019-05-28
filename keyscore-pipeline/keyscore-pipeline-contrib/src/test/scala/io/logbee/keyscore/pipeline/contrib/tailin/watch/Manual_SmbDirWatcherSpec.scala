package io.logbee.keyscore.pipeline.contrib.tailin.watch

import java.nio.charset.StandardCharsets

import io.logbee.keyscore.pipeline.contrib.tailin.file.{DirHandle, FileHandle, SmbDir}
import io.logbee.keyscore.pipeline.contrib.tailin.util.Manual_SpecWithSmbShare
import org.scalamock.scalatest.MockFactory


class Manual_SmbDirWatcherSpec extends Manual_SpecWithSmbShare with MockFactory {
  //TODO
  
  trait DirWatcherParams {
    var provider = mock[WatcherProvider[DirHandle, FileHandle]]
    var matchPattern = DirWatcherPattern("/*.txt")
  }
  
  
  "A SmbDirWatcher," - {
    val charset = StandardCharsets.UTF_8
    
    "when a sub-directory" - {
      "is created, should create a DirWatcher for that sub-directory" ignore //TODO
      new DirWatcherParams {
        
        withShare { share =>
          
          val dirPath = "testDir\\"
          withSmbDir(share, dirPath, { realDir =>
            
            val dir = new SmbDir(realDir)
            
            matchPattern = DirWatcherPattern(fullFilePattern = "\\\\" + hostName + "\\" + shareName + "\\" + dirPath + "*\\test.txt", depth = 2)
            val dirWatcher = new SmbDirWatcher(dir, matchPattern, provider)
            
            
            val subDirPath = dirPath + "subDir"
            withSmbDir(share, subDirPath, { subDir =>
              
              val subDirWatcher = mock[DirWatcher]
              
              (provider.createDirWatcher _)
                .expects(*, matchPattern.copy(depth = 3))
                .returning(subDirWatcher)
              
              
              (subDirWatcher.processFileChanges _).expects()
              dirWatcher.processFileChanges()
              
              
              
              //call another time to verify that it's called on the sub-DirWatcher
              (subDirWatcher.processFileChanges _).expects()
              dirWatcher.processFileChanges()
              
              
              (subDirWatcher.tearDown _).expects()
              dirWatcher.tearDown()
            })
          })
        }
      }
    }
    
    
    "when a file" - {
      "is created, should create a FileEventHandler" - { //TODO different file patterns
        "" in //TODO
        new DirWatcherParams {
          
          withShare { share =>
            
            val dirPath = "testDir\\"
            withSmbDir(share, dirPath, { realDir =>
              val dir = new SmbDir(realDir)
              println("matchPattern: " + dir.absolutePath + "test.txt")
              matchPattern = DirWatcherPattern(dir.absolutePath + "test.txt") //FIXME correct this filePattern
              val dirWatcher = new SmbDirWatcher(dir, matchPattern, provider)
              
              
              val filePath = dirPath + "test.txt"
              val content = charset.encode("base file")
              withSmbFile(share, filePath, content, { file =>
                
                val fileEventHandler = mock[FileEventHandler]
                
                (fileEventHandler.processFileChanges _).expects()
                
                (provider.createFileEventHandler _)
                  .expects(file)
                  .returning(fileEventHandler)
                
                dirWatcher.processFileChanges()
                
                (fileEventHandler.tearDown _).expects()
                dirWatcher.tearDown()
              })
            })
          }
        }
      }
    }
  }
}