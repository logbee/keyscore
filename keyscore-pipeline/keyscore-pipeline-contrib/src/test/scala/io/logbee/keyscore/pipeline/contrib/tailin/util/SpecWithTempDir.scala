package io.logbee.keyscore.pipeline.contrib.tailin.util

import org.scalatest.FreeSpec
import org.scalatest.BeforeAndAfter
import java.nio.file.Path
import java.nio.file.Files

class SpecWithTempDir extends FreeSpec with BeforeAndAfter {
  
  var watchDir: Path = null
  
  before {
    watchDir = Files.createTempDirectory("watchTest")
    
    TestUtil.waitForFileToExist(watchDir.toFile)
  }
  
  after {
    TestUtil.recursivelyDelete(watchDir)
  }
}
