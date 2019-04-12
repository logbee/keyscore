package io.logbee.keyscore.pipeline.contrib.tailin.watch

trait FileWatcher extends PathWatcher {
  def fileModified()
}
