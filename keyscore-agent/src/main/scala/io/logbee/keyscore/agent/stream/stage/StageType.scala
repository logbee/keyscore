package io.logbee.keyscore.agent.stream.stage

object StageType extends Enumeration {
  type StageType = Value
  val Source, Sink, Filter = Value
}
