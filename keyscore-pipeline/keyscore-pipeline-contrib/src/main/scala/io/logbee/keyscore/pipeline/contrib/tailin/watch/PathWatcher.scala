package io.logbee.keyscore.pipeline.contrib.tailin.watch


trait PathWatcher {
  
  /**
   * Notifies this PathWatcher that its path has been deleted and allows it to react to that scenario.
   */
  def pathDeleted()
  
  /**
   * This method is called before a PathWatcher is destroyed.
   * Do cleanup tasks in here.
   */
  def tearDown()
}


trait DirWatcher extends PathWatcher {
  def processEvents()
}

trait FileWatcher extends PathWatcher {
  def fileModified()
}
