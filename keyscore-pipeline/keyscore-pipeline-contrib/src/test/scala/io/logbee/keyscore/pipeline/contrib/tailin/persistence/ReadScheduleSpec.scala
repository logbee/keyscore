package io.logbee.keyscore.pipeline.contrib.tailin.persistence

import java.io.File

import scala.io.Source

import org.scalatest.BeforeAndAfter
import org.scalatest.FreeSpec
import org.scalatest.Matchers

import io.logbee.keyscore.pipeline.contrib.tailin.util.TestUtil


class ReadScheduleSpec extends FreeSpec with Matchers with BeforeAndAfter {
  
  import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadSchedule.|
  import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadSchedule.newline
  
  val changelogFile = new File(".keyscoreTailinChangelog")
  
  
  
  
  before {
    changelogFile.createNewFile()
    TestUtil.waitForFileToExist(changelogFile)
  }
  after {
    changelogFile.delete()
  }
  
  trait ReadScheduleSetup {
    val readSchedule = new ReadSchedule(changelogFile)
  }
  
  
  "A ReadSchedule should" - {
    
    "queue a read for a change in a file" in new ReadScheduleSetup {
      
      val file = new File(".testFile")
      
      val pos1 = 12
      val pos2 = 23
      
      readSchedule.queue(ReadScheduleItem(file, pos1, pos2, file.lastModified))
      
      Source.fromFile(changelogFile).mkString shouldBe
        file.getAbsolutePath + | + pos1 + | + pos2 + | + file.lastModified + newline
    }
    
    "queue multiple reads in order" in new ReadScheduleSetup {
      
      val file = new File(".testFile")
      
      val pos1 = 12
      val pos2 = 23
      val pos3 = 34
      val pos4 = 45
      
      readSchedule.queue(ReadScheduleItem(file, pos1, pos2, file.lastModified))
      readSchedule.queue(ReadScheduleItem(file, pos2, pos3, file.lastModified))
      readSchedule.queue(ReadScheduleItem(file, pos3, pos4, file.lastModified))
      
      Source.fromFile(changelogFile).mkString shouldBe
        file.getAbsolutePath + | + pos1 + | + pos2 + | + file.lastModified + newline +
        file.getAbsolutePath + | + pos2 + | + pos3 + | + file.lastModified + newline +
        file.getAbsolutePath + | + pos3 + | + pos4 + | + file.lastModified + newline
    }
    
    "return the next read schedule item in order" in new ReadScheduleSetup {
      
      val file = new File(".testFile")
      
      val pos1 = 12
      val pos2 = 23
      val pos3 = 34
      val pos4 = 45
      
      readSchedule.queue(ReadScheduleItem(file, pos1, pos2, file.lastModified))
      readSchedule.queue(ReadScheduleItem(file, pos2, pos3, file.lastModified))
      readSchedule.queue(ReadScheduleItem(file, pos3, pos4, file.lastModified))
      
      readSchedule.getNext.get shouldBe ReadScheduleItem(file.getAbsoluteFile, pos1, pos2, file.lastModified)
    }
    
    "return the latest schedule item for a given file" in new ReadScheduleSetup {
      
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
      
      readSchedule.queue(ReadScheduleItem(file , pos1, pos2, file.lastModified))
      readSchedule.queue(ReadScheduleItem(file2, pos5, pos6, file2.lastModified))
      readSchedule.queue(ReadScheduleItem(file2, pos6, pos7, file2.lastModified))
      readSchedule.queue(ReadScheduleItem(file , pos2, pos3, file.lastModified))
      readSchedule.queue(ReadScheduleItem(file , pos3, pos4, file.lastModified))
      readSchedule.queue(ReadScheduleItem(file2, pos7, pos8, file.lastModified))
      
      readSchedule.getLatestEntry(file).get shouldBe ReadScheduleItem(file.getAbsoluteFile, pos3, pos4, file.lastModified)
    }
    
    "remove the next schedule item in order" in new ReadScheduleSetup {
      
      val file = new File(".testFile")
      
      val pos1 = 12
      val pos2 = 23
      val pos3 = 34
      val pos4 = 45
      
      readSchedule.queue(ReadScheduleItem(file, pos1, pos2, file.lastModified))
      readSchedule.queue(ReadScheduleItem(file, pos2, pos3, file.lastModified))
      readSchedule.queue(ReadScheduleItem(file, pos3, pos4, file.lastModified))
      
      Source.fromFile(changelogFile).mkString shouldBe
        file.getAbsolutePath + | + pos1 + | + pos2 + | + file.lastModified + newline +
        file.getAbsolutePath + | + pos2 + | + pos3 + | + file.lastModified + newline +
        file.getAbsolutePath + | + pos3 + | + pos4 + | + file.lastModified + newline
      
      readSchedule.removeNext()
      
      Source.fromFile(changelogFile).mkString shouldBe
        file.getAbsolutePath + | + pos2 + | + pos3 + | + file.lastModified + newline +
        file.getAbsolutePath + | + pos3 + | + pos4 + | + file.lastModified + newline
        
      readSchedule.removeNext()
      
      Source.fromFile(changelogFile).mkString shouldBe
        file.getAbsolutePath + | + pos3 + | + pos4 + | + file.lastModified + newline
        
      readSchedule.removeNext()
      
      Source.fromFile(changelogFile).mkString shouldBe ""
      
      readSchedule.removeNext()
      
      Source.fromFile(changelogFile).mkString shouldBe ""
    }
  }
}
