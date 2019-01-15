package io.logbee.keyscore.pipeline.contrib.tailin.persistence

import java.io.File

import scala.io.Source

import org.scalatest.BeforeAndAfter
import org.scalatest.FreeSpec
import org.scalatest.Matchers

import io.logbee.keyscore.pipeline.contrib.tailin.util.TestUtil


class FileChangelogSpec extends FreeSpec with Matchers with BeforeAndAfter {
  
  import io.logbee.keyscore.pipeline.contrib.tailin.persistence.FileChangelog.|
  import io.logbee.keyscore.pipeline.contrib.tailin.persistence.FileChangelog.newline
  
  val changelogFile = new File(".keyscoreTailinChangelog")
  
  
  
  
  before {
    changelogFile.createNewFile()
    TestUtil.waitForFileToExist(changelogFile)
  }
  after {
    changelogFile.delete()
  }
  
  trait FileChangelogSetup {
    val fileChangelog = new FileChangelog(changelogFile)
  }
  
  
  "A FileChangelog should" - {
    
    "queue a FileChange" in new FileChangelogSetup {
      
      val file = new File(".testFile")
      
      val pos1 = 12
      val pos2 = 23
      
      fileChangelog.queue(FileChange(file, pos1, pos2, file.lastModified))
      
      Source.fromFile(changelogFile).mkString shouldBe
        file.getAbsolutePath + | + pos1 + | + pos2 + | + file.lastModified + newline
    }
    
    "queue multiple FileChanges in order" in new FileChangelogSetup {
      
      val file = new File(".testFile")
      
      val pos1 = 12
      val pos2 = 23
      val pos3 = 34
      val pos4 = 45
      
      fileChangelog.queue(FileChange(file, pos1, pos2, file.lastModified))
      fileChangelog.queue(FileChange(file, pos2, pos3, file.lastModified))
      fileChangelog.queue(FileChange(file, pos3, pos4, file.lastModified))
      
      Source.fromFile(changelogFile).mkString shouldBe
        file.getAbsolutePath + | + pos1 + | + pos2 + | + file.lastModified + newline +
        file.getAbsolutePath + | + pos2 + | + pos3 + | + file.lastModified + newline +
        file.getAbsolutePath + | + pos3 + | + pos4 + | + file.lastModified + newline
    }
    
    "return the next FileChange in order" in new FileChangelogSetup {
      
      val file = new File(".testFile")
      
      val pos1 = 12
      val pos2 = 23
      val pos3 = 34
      val pos4 = 45
      
      fileChangelog.queue(FileChange(file, pos1, pos2, file.lastModified))
      fileChangelog.queue(FileChange(file, pos2, pos3, file.lastModified))
      fileChangelog.queue(FileChange(file, pos3, pos4, file.lastModified))
      
      fileChangelog.getNext.get shouldBe FileChange(file.getAbsoluteFile, pos1, pos2, file.lastModified)
    }
    
    "return the latest entry for a given file" in new FileChangelogSetup {
      
      val file = new File(".testFile")
      
      val pos1 = 12
      val pos2 = 23
      val pos3 = 34
      val pos4 = 45
      
      val file2 = new File(".testFile2")
      
      val pos5 = 56
      val pos6 = 67
      val pos7 = 78
      val pos8 = 89
      
      fileChangelog.queue(FileChange(file , pos1, pos2, file.lastModified))
      fileChangelog.queue(FileChange(file2, pos5, pos6, file2.lastModified))
      fileChangelog.queue(FileChange(file2, pos6, pos7, file2.lastModified))
      fileChangelog.queue(FileChange(file , pos2, pos3, file.lastModified))
      fileChangelog.queue(FileChange(file , pos3, pos4, file.lastModified))
      fileChangelog.queue(FileChange(file2, pos7, pos8, file.lastModified))
      
      fileChangelog.getLatestEntry(file).get shouldBe FileChange(file.getAbsoluteFile, pos3, pos4, file.lastModified)
    }
    
    "remove the next FileChange in order" in new FileChangelogSetup {
      
      val file = new File(".testFile")
      
      val pos1 = 12
      val pos2 = 23
      val pos3 = 34
      val pos4 = 45
      
      fileChangelog.queue(FileChange(file, pos1, pos2, file.lastModified))
      fileChangelog.queue(FileChange(file, pos2, pos3, file.lastModified))
      fileChangelog.queue(FileChange(file, pos3, pos4, file.lastModified))
      
      Source.fromFile(changelogFile).mkString shouldBe
        file.getAbsolutePath + | + pos1 + | + pos2 + | + file.lastModified + newline +
        file.getAbsolutePath + | + pos2 + | + pos3 + | + file.lastModified + newline +
        file.getAbsolutePath + | + pos3 + | + pos4 + | + file.lastModified + newline
      
      fileChangelog.removeNext()
      
      Source.fromFile(changelogFile).mkString shouldBe
        file.getAbsolutePath + | + pos2 + | + pos3 + | + file.lastModified + newline +
        file.getAbsolutePath + | + pos3 + | + pos4 + | + file.lastModified + newline
        
      fileChangelog.removeNext()
      
      Source.fromFile(changelogFile).mkString shouldBe
        file.getAbsolutePath + | + pos3 + | + pos4 + | + file.lastModified + newline
        
      fileChangelog.removeNext()
      
      Source.fromFile(changelogFile).mkString shouldBe ""
      
      fileChangelog.removeNext()
      
      Source.fromFile(changelogFile).mkString shouldBe ""
    }
  }
}
