package io.logbee.keyscore.pipeline.contrib.tailin.watch


trait PathWatcher {
  
  /**
   * Determines if files have been changed in this path and executes the implemented action for it.
   */
  def processChanges(): Unit
  
  /**
   * Notifies this PathWatcher that its path has been deleted and allows it to react to that scenario.
   */
  def pathDeleted(): Unit
  
  /**
   * This method is called before a PathWatcher is destroyed.
   * Do cleanup tasks in here.
   */
  def tearDown(): Unit
}


trait BaseDirWatcher extends PathWatcher

trait FileEventHandler extends PathWatcher
