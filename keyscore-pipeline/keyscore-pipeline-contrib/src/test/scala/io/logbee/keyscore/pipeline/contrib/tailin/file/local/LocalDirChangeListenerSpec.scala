package io.logbee.keyscore.pipeline.contrib.tailin.file.local

import io.logbee.keyscore.pipeline.contrib.tailin.util.SpecWithTempDir
import io.logbee.keyscore.pipeline.contrib.tailin.util.TestUtil
import java.nio.file.Paths
import org.scalatest.Matchers
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class LocalDirChangeListenerSpec extends SpecWithTempDir with Matchers {
  
  trait LocalDirChangeListenerSetup {
    val localDirChangeListener = new LocalDirChangeListener(new LocalDir(watchDir))
  }
  
  
  "A LocalDir should" - {
    "correctly list the changes happening in it" - {
      
      "when a directory gets created" in
      new LocalDirChangeListenerSetup {
        val subDir = Paths.get(s"$watchDir/testDir/")
        subDir.toFile.mkdir()
        TestUtil.waitForFileToExist(subDir.toFile)
        
        localDirChangeListener.getChanges.newlyCreatedDirs should contain (new LocalDir(subDir))
      }
      
      
      "when a file gets created" in
      new LocalDirChangeListenerSetup {
        val subFile = TestUtil.createFile(watchDir, "testFile.txt", "Hello World!")
        
        localDirChangeListener.getChanges.newlyCreatedFiles should contain (new LocalFile(subFile))
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
        changes.deletedPaths should contain (new LocalDir(subDir))
      }
      
      
      "when a file gets deleted" in
      new LocalDirChangeListenerSetup {
        val subFile = TestUtil.createFile(watchDir, "testFile.txt", "Hello World!")
        
        localDirChangeListener.getChanges //make these changes the current state
        
        subFile.delete()
        TestUtil.waitForFileToBeDeleted(subFile)
        
        val changes = localDirChangeListener.getChanges
        
        changes.newlyCreatedFiles shouldBe empty
        changes.deletedPaths should contain (new LocalDir(subFile.toPath)) //gets converted to a LocalDir, because we can't check after deletion whether the deleted path contained a dir or a file
      }
      
      
      "when a directory was potentially modified" in
      new LocalDirChangeListenerSetup {
        val subDir = Paths.get(s"$watchDir/testDir")
        subDir.toFile.mkdir()
        TestUtil.waitForFileToExist(subDir.toFile)
        
        localDirChangeListener.getChanges //make these changes the current state
        
        val subFile = TestUtil.createFile(subDir, "testFile.txt", "Hello World!")
        
        val changes = localDirChangeListener.getChanges
        
        changes.potentiallyModifiedDirs should contain (new LocalDir(subDir))
      }
      
      
      "when a file was potentially modified" in
      new LocalDirChangeListenerSetup {
        val subFile = TestUtil.createFile(watchDir, "testFile.txt", "foo")
        
        localDirChangeListener.getChanges //make these changes the current state
        
        TestUtil.writeStringToFile(subFile, "bar")
        TestUtil.waitForWatchService()
        
        val changes = localDirChangeListener.getChanges
        
        changes.potentiallyModifiedFiles should contain (new LocalFile(subFile))
      }
      
      
      
      "when a previous change was already queried" in
      new LocalDirChangeListenerSetup {
        val subFile = TestUtil.createFile(watchDir, "testFile.txt", "foo")
        
        localDirChangeListener.getChanges //make these changes the current state
        
        val subFile2 = TestUtil.createFile(watchDir, "testFile2.txt", "bar")
        
        val changes = localDirChangeListener.getChanges
        
        changes.newlyCreatedFiles should not contain (new LocalFile(subFile))
        changes.newlyCreatedFiles should contain (new LocalFile(subFile2))
      }
      
      
      
      //TODO this test can't be made to work, as the Java WatchService API will return just the deletion event -> make the DirWatcher resilient for files being deleted that never existed
      "when a path gets created and deleted in between polls" ignore
      new LocalDirChangeListenerSetup {
        val subFile = TestUtil.createFile(watchDir, "testFile.txt", "Hello World!")
        
        subFile.delete()
        TestUtil.waitForFileToBeDeleted(subFile)
        
        val changes = localDirChangeListener.getChanges
        
        changes.newlyCreatedFiles shouldBe empty
        changes.deletedPaths shouldBe empty
      }
    }
  }
}
