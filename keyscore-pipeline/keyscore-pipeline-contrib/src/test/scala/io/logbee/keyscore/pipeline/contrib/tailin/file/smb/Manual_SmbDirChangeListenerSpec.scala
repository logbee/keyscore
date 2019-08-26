package io.logbee.keyscore.pipeline.contrib.tailin.file.smb

import java.nio.charset.StandardCharsets

import io.logbee.keyscore.pipeline.contrib.tailin.file.{DirChanges, PathHandle}
import io.logbee.keyscore.pipeline.contrib.tailin.util.Manual_SpecWithSmbShare
import org.scalatest.Matchers

class Manual_SmbDirChangeListenerSpec extends Manual_SpecWithSmbShare with Matchers {
  
  val charset = StandardCharsets.UTF_8
  val emptyDirChanges = DirChanges(Set[SmbDir](), Set[SmbFile](), Set[PathHandle](), Set[SmbDir](), Set[SmbFile]())
  val dirName = "testDir\\"
  
  "An SmbDirChangeListener should" - {
    "list the changes happening in it" - {
      "when no changes have happened" in withShare { implicit share =>
        withSmbDir(dirName, { dir =>
          val dirChangeListener = new SmbDirChangeListener(dir)
          dirChangeListener.getChanges shouldEqual emptyDirChanges
        })
      }
      
      "when a dir got created" in withShare { implicit share =>
        withSmbDir(dirName, { dir =>
          val dirChangeListener = new SmbDirChangeListener(dir)
          
          val dir2Name = "testDir2\\"
          
          withSmbDir(dirName + dir2Name, { smbDir =>
            dirChangeListener.getChanges shouldEqual emptyDirChanges.copy(newlyCreatedDirs = Set(smbDir))
          })
        })
      }
      
      "when a file got created" in withShare { implicit share =>
        withSmbDir(dirName, { dir =>
          val dirChangeListener = new SmbDirChangeListener(dir)
          
          val fileName = "testFile"
          
          withSmbFile(dirName + fileName, charset.encode("test file"), { smbFile =>
            dirChangeListener.getChanges shouldEqual emptyDirChanges.copy(newlyCreatedFiles = Set(smbFile))
          })
        })
      }
    }
  }
}
