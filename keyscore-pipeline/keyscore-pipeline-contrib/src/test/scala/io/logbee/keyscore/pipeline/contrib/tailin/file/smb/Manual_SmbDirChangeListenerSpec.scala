package io.logbee.keyscore.pipeline.contrib.tailin.file.smb

import java.nio.charset.StandardCharsets

import io.logbee.keyscore.pipeline.contrib.tailin.file.PathHandle
import io.logbee.keyscore.pipeline.contrib.tailin.util.Manual_SpecWithSmbShare
import io.logbee.keyscore.pipeline.contrib.tailin.watch.{DirChangeListener, DirChanges}
import org.scalatest.matchers.should.Matchers

class Manual_SmbDirChangeListenerSpec extends Manual_SpecWithSmbShare with Matchers {
  
  val charset = StandardCharsets.UTF_8
  val emptyDirChanges = DirChanges(Seq[SmbDir](), Seq[SmbFile](), Seq[PathHandle](), Seq[SmbDir](), Seq[SmbFile]())
  val dirName = "testDir\\"
  
  "An SmbDirChangeListener should" - {
    "list the changes happening in it" - {
      "when no changes have happened" in withShare { implicit share =>
        withSmbDir(dirName, { dir =>
          val dirChangeListener = new DirChangeListener[SmbDir, SmbFile](dir)
          dirChangeListener.computeChanges shouldEqual emptyDirChanges
        })
      }
      
      "when a dir got created" in withShare { implicit share =>
        withSmbDir(dirName, { dir =>
          val dirChangeListener = new DirChangeListener[SmbDir, SmbFile](dir)
          
          val dir2Name = "testDir2\\"
          
          withSmbDir(dirName + dir2Name, { smbDir =>
            dirChangeListener.computeChanges shouldEqual emptyDirChanges.copy(newlyCreatedDirs = Seq(smbDir))
          })
        })
      }
      
      "when a file got created" in withShare { implicit share =>
        withSmbDir(dirName, { dir =>
          val dirChangeListener = new DirChangeListener[SmbDir, SmbFile](dir)
          
          val fileName = "testFile"
          
          withSmbFile(dirName + fileName, charset.encode("test file"), { smbFile =>
            dirChangeListener.computeChanges shouldEqual emptyDirChanges.copy(newlyCreatedFiles = Seq(smbFile))
          })
        })
      }
    }
  }
}
