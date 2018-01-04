package io.logbee.keyscore.frontier.filters

import akka.kafka.ConsumerMessage

case class CommitableFilterMessage (value:Map[String,String],committableOffset:ConsumerMessage.CommittableOffset)
