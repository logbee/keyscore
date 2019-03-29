package io.logbee.keyscore.pipeline.contrib.tailin.watch

import java.nio.file.Path

abstract class PathWatcher(path: Path) {
  
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
