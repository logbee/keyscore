package io.logbee.keyscore.pipeline.contrib.tailin

import java.io.File


trait FileWatcher {
  def fileModified(callback: (String) => Unit)
  
  def pathDeleted()
  
  def teardown()
}

abstract class DefaultFileWatcher(file: File) extends PathWatcher(file.toPath) with FileWatcher {
  
}
