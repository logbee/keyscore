package io.logbee.keyscore.pipeline.contrib.tailin.watch

import java.io.File


trait FileWatcher {
  def fileModified()
  
  def pathDeleted()
  
  def tearDown()
}

abstract class DefaultFileWatcher(file: File) extends PathWatcher(file.toPath) with FileWatcher {
  
}
