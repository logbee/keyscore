package io.logbee.keyscore.pipeline.contrib.tailin.file

trait PathHandle {
  
  /**
   * Usually used as unique identifier.
   */
  val absolutePath: String
  
  def tearDown(): Unit
}
