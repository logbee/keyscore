package io.logbee.keyscore.pipeline.contrib.tailin.file.local

import java.nio.charset.StandardCharsets
import java.nio.file.Paths

import io.logbee.keyscore.pipeline.contrib.tailin.file.local.LocalFile.openLocalFile2File
import io.logbee.keyscore.pipeline.contrib.tailin.util.{SpecWithTempDir, TestUtil}
import org.junit.runner.RunWith
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class LocalDirChangeListenerSpec extends SpecWithTempDir with Matchers {
  
  trait LocalDirChangeListenerSetup {
    implicit val charset = StandardCharsets.UTF_8

    val localDirChangeListener = new LocalDirChangeListener(LocalDir(watchDir))
  }
  
  
  "A LocalDirChangeListener should" - {
    "correctly list the changes happening in its associated directory" - {
      
      "when a directory gets created" in
      new LocalDirChangeListenerSetup {
        val subDir = Paths.get(s"$watchDir/testDir/")
        subDir.toFile.mkdir()
        TestUtil.waitForFileToExist(subDir.toFile)
        
        localDirChangeListener.getChanges.newlyCreatedDirs should contain (LocalDir(subDir))
      }
      
      
      "when a file gets created" in
      new LocalDirChangeListenerSetup {
        TestUtil.withOpenLocalFile(watchDir, "testFile.txt", "Hello World!") { subFile =>
          localDirChangeListener.getChanges.newlyCreatedFiles should contain (LocalFile(subFile))
        }
      }
      
      
      "when a directory gets deleted" in
      new LocalDirChangeListenerSetup {
        val subDir = Paths.get(s"$watchDir/testDir")
        subDir.toFile.mkdir()
        TestUtil.waitForFileToExist(subDir.toFile)
        
        localDirChangeListener.getChanges //make these changes the current state
        
        subDir.toFile.delete()
        TestUtil.waitForFileToBeDeleted(subDir.toFile)
        
        val changes = localDirChangeListener.getChanges
        
        changes.newlyCreatedDirs shouldBe empty
        changes.deletedPaths should contain (LocalDir(subDir))
      }
      
      
      "when a file gets deleted" in
      new LocalDirChangeListenerSetup {
        TestUtil.withOpenLocalFile(watchDir, "testFile.txt", "Hello World!") { subFile =>
        
          localDirChangeListener.getChanges //make these changes the current state

          subFile.delete()
          TestUtil.waitForFileToBeDeleted(subFile)

          val changes = localDirChangeListener.getChanges

          changes.newlyCreatedFiles shouldBe empty
          changes.deletedPaths should contain (LocalDir(subFile.toPath)) //gets converted to a LocalDir, because we can't check after deletion whether the deleted path contained a dir or a file
        }
      }
      
      
      "when a directory was potentially modified" in
      new LocalDirChangeListenerSetup {
        val subDir = watchDir.resolve("subDir")
        subDir.toFile.mkdir()
        TestUtil.waitForFileToExist(subDir.toFile)
        
        val nonChanges = localDirChangeListener.getChanges //make these changes the current state
        nonChanges.newlyCreatedDirs should contain (LocalDir(subDir))
        
        val changes = localDirChangeListener.getChanges
        
        changes.potentiallyModifiedDirs should contain (LocalDir(subDir))
      }
      
      
      "when a file was potentially modified" in
      new LocalDirChangeListenerSetup {
        TestUtil.withOpenLocalFile(watchDir, "testFile.txt", "foo") { subFile =>

          localDirChangeListener.getChanges //make these changes the current state

          TestUtil.writeStringToFile(subFile, "bar")
          TestUtil.waitForWatchService()

          val changes = localDirChangeListener.getChanges

          changes.potentiallyModifiedFiles should contain (LocalFile(subFile))
        }
      }
      
      
      
      "when a previous change was already queried" in
      new LocalDirChangeListenerSetup {
        TestUtil.withOpenLocalFile(watchDir, "testFile.txt", "foo") { subFile =>

          localDirChangeListener.getChanges //make these changes the current state

          TestUtil.withOpenLocalFile(watchDir, "testFile2.txt", "bar") { subFile2 =>

            val changes = localDirChangeListener.getChanges

            changes.newlyCreatedFiles should not contain (LocalFile(subFile))
            changes.newlyCreatedFiles should contain (LocalFile(subFile2))
          }
        }
      }
      
      
      
      //TODO this test can't be made to work, as the Java WatchService API will return just the deletion event -> make the DirWatcher resilient for files being deleted that never existed
      "when a path gets created and deleted in between polls" ignore
      new LocalDirChangeListenerSetup {
        TestUtil.withOpenLocalFile(watchDir, "testFile.txt", "Hello World!") { subFile =>

          subFile.delete()
          TestUtil.waitForFileToBeDeleted(subFile)

          val changes = localDirChangeListener.getChanges

          changes.newlyCreatedFiles shouldBe empty
          changes.deletedPaths shouldBe empty
        }
      }
    }
  }
}
