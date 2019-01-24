package io.logbee.keyscore.pipeline.contrib.tailin.file

import java.io.File


trait FileWatcher {
  def fileModified()
  
  def pathDeleted()
  
  def tearDown()
}

abstract class DefaultFileWatcher(file: File) extends PathWatcher(file.toPath) with FileWatcher {
  
}
