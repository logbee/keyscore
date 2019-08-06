package io.logbee.keyscore.model.pipeline

object StageSupervisor {
  val noop: StageSupervisor = new StageSupervisor {
    override def complete(): Unit = {}
    override def fail(ex: Throwable): Unit = {}
  }
}

trait StageSupervisor {
  def complete(): Unit
  def fail(ex: Throwable) : Unit
}
