package io.logbee.keyscore.pipeline.contrib.tailin.file.smb

import java.nio.charset.StandardCharsets

import io.logbee.keyscore.pipeline.contrib.tailin.file.PathHandle
import io.logbee.keyscore.pipeline.contrib.tailin.util.Manual_SpecWithSmbShare
import io.logbee.keyscore.pipeline.contrib.tailin.watch.DirChanges
import org.scalatest.matchers.should.Matchers

class Manual_SmbDirSpec extends Manual_SpecWithSmbShare with Matchers {
  
  val charset = StandardCharsets.UTF_8
  val emptyDirChanges = DirChanges(Seq[SmbDir](), Seq[SmbFile](), Seq[PathHandle](), Seq[SmbDir](), Seq[SmbFile]())
  val dirName = "testDir\\"
  
  "An SmbDir should" - {
    "list the directories and files that are contained in it" - {
      "with no directories or files inside" in withShare { implicit share =>
        withOpenSmbDir(dirName, { smbDir =>
          smbDir.listDirsAndFiles shouldEqual (Seq(), Seq())
        })
      }
      
      "with a directory inside" in withShare { implicit share =>
        withOpenSmbDir(dirName, { smbDir =>
          val dir2Name = "testDir2\\"
          withSmbDir(dirName + dir2Name, { smbDir2 =>
            smbDir.listDirsAndFiles shouldEqual (Seq(smbDir2), Seq())
          })
        })
      }
      
      "with a file inside" in withShare { implicit share =>
        withOpenSmbDir(dirName, { smbDir =>
          val fileName = "testFile"
          withSmbFile(dirName + fileName, charset.encode("test file"), { smbFile =>
            smbDir.listDirsAndFiles shouldEqual (Seq(), Seq(smbFile))
          })
        })
      }
    }
  }
}
