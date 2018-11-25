package io.logbee.keyscore.pipeline.contrib.tailin.util

import java.io.File

object FileUtility {
  def waitForFileToExist(file: File): Unit = {
    for (i <- 1 to 20) {
      if (file.exists) {
        return
      }
      else {
        Thread.sleep(100)
      }
    }
  }}
