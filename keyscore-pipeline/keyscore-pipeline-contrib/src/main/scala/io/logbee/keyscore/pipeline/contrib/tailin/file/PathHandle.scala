package io.logbee.keyscore.pipeline.contrib.tailin.file

trait PathHandle {
  /**
   * Usually used as unique identifier.
   */
  def absolutePath: String
}

trait OpenPathHandle {
  
  /**
   * Usually used as unique identifier.
   */
  def absolutePath: String
}
