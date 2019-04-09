package io.logbee.keyscore.pipeline.contrib.tailin.watch

trait FileWatcher {
  def fileModified()
  
  def pathDeleted()
  
  def tearDown()
}
