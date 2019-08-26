package io.logbee.keyscore.pipeline.contrib.tailin.file.smb

import io.logbee.keyscore.pipeline.contrib.tailin.util.Manual_SpecWithSmbShare
import org.scalatest.Matchers
import java.nio.charset.StandardCharsets

import io.logbee.keyscore.pipeline.contrib.tailin.file.{DirChanges, PathHandle}

class Manual_SmbDirSpec extends Manual_SpecWithSmbShare with Matchers {
  
  val charset = StandardCharsets.UTF_8
  val emptyDirChanges = DirChanges(Set[SmbDir](), Set[SmbFile](), Set[PathHandle](), Set[SmbDir](), Set[SmbFile]())
  val dirName = "testDir\\"
  
  "An SmbDir should" - {
    "list the directories and files that are contained in it" - {
      "with no directories or files inside" in withShare { implicit share =>
        withSmbDir(dirName, { smbDir =>
          smbDir.listDirsAndFiles shouldEqual (Set(), Set())
        })
      }
      
      "with a directory inside" in withShare { implicit share =>
        withSmbDir(dirName, { smbDir =>
          val dir2Name = "testDir2\\"
          withSmbDir(dirName + dir2Name, { smbDir2 =>
            smbDir.listDirsAndFiles shouldEqual (Set(smbDir2), Set())
          })
        })
      }
      
      "with a file inside" in withShare { implicit share =>
        withSmbDir(dirName, { smbDir =>
          val fileName = "testFile"
          withSmbFile(dirName + fileName, charset.encode("test file"), { smbFile =>
            smbDir.listDirsAndFiles shouldEqual (Set(), Set(smbFile))
          })
        })
      }
    }
  }
}
