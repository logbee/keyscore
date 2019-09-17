package io.logbee.keyscore.pipeline.contrib.tailin.watch


trait PathWatcher {
  
  /**
   * Determines if files have been changed in this path and executes the implemented action for it.
   */
  def processChanges(): Unit
}


trait BaseDirWatcher extends PathWatcher {
  def pathDeleted(): Unit

  def tearDown(): Unit
}

trait FileEventHandler extends PathWatcher {
  def pathDeleted(): Unit

  def tearDown(): Unit
}
