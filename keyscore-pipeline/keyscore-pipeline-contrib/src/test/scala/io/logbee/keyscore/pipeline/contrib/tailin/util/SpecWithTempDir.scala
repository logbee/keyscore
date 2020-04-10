package io.logbee.keyscore.pipeline.contrib.tailin.util

import org.scalatest.BeforeAndAfter
import java.nio.file.Path
import java.nio.file.Files

import org.scalatest.freespec.AnyFreeSpec

class SpecWithTempDir extends AnyFreeSpec with BeforeAndAfter {
  
  var watchDir: Path = null
  
  before {
    watchDir = Files.createTempDirectory("watchTest")
    
    TestUtil.waitForFileToExist(watchDir.toFile)
  }
  
  after {
    TestUtil.recursivelyDelete(watchDir)
  }
}
