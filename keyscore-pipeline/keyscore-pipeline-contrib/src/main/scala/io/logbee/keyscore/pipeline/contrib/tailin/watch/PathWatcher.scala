package io.logbee.keyscore.pipeline.contrib.tailin.watch

import scala.util.Try


trait PathWatcher {
  
  /**
   * Determines if files have been changed in this path and executes the implemented action for it.
   */
  def processChanges(): Try[Unit]
}

trait BaseDirWatcher extends PathWatcher {}

trait FileEventHandler extends PathWatcher {}
