package io.logbee.keyscore.pipeline.contrib.tailin.watch

import io.logbee.keyscore.pipeline.contrib.tailin.util.Manual_SpecWithSmbShare
import org.scalamock.scalatest.MockFactory
import com.hierynomus.smbj.share.Directory
import scala.collection.JavaConverters
import java.util.EnumSet
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.msfscc.FileAttributes
import com.hierynomus.mssmb2.SMB2CreateOptions
import java.nio.charset.StandardCharsets


class Manual_SmbDirWatcherSpec extends Manual_SpecWithSmbShare with MockFactory {
  //TODO
  
  trait DirWatcherParams {
    var provider = mock[WatcherProvider[Directory]]
    var matchPattern = DirWatcherPattern("/*.txt")
  }
  
  
  "A SmbDirWatcher," - {
    val charset = StandardCharsets.UTF_8
    
    "when a sub-directory" - {
      "is created, should create a DirWatcher for that sub-directory" ignore //TODO
      new DirWatcherParams {
        
        withShare { share =>
          
          val dirPath = "testDir\\"
          withSmbDir(share, dirPath, { dir =>
            
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
              
              //TODO due .equals() not being implemented correctly for smbj's DiskEntry,
              //the code that compares previously existing files with new files does not work correctly
              //and therefore the following calls are erroneously made
              //////////////////////////////////////////
              (subDirWatcher.pathDeleted _).expects()
              (provider.createDirWatcher _)
                .expects(*, matchPattern.copy(depth = 3))
                .returning(subDirWatcher)
              (subDirWatcher.processFileChanges _).expects()
              /////////////////////////////////////////
              
              dirWatcher.processFileChanges()
              
              
              
              subDirWatcher.tearDown()
              dirWatcher.tearDown()
            })
          })
        }
      }
    }
    
    
    "when a file" - {
      "is created, should create a FileEventHandler" - { //TODO different file patterns
        "" ignore //TODO
        new DirWatcherParams {
          
          withShare { share =>
            
            val dirPath = "testDir\\"
            withSmbDir(share, dirPath, { dir =>
              println("matchPattern: " + dir.getFileName + "test.txt")
              matchPattern = DirWatcherPattern(dir.getFileName + "test.txt") //FIXME correct this filePattern
              val dirWatcher = new SmbDirWatcher(dir, matchPattern, provider)
              
              
              val filePath = dirPath + "test.txt"
              val content = charset.encode("base file")
              withSmbFile(share, filePath, content, { file =>
                
                val fileEventHandler = mock[FileEventHandler]
                
                (fileEventHandler.processFileChanges _)
                  .expects()
                
                (provider.createFileEventHandler _)
                  .expects(file)
                  .returning(fileEventHandler)
                
                dirWatcher.processFileChanges()
              })
            })
          }
        }
      }
    }
  }
}