package io.logbee.keyscore.frontier.filter

import akka.kafka.ConsumerMessage

case class CommitableFilterMessage (value:Map[String,String],committableOffset:ConsumerMessage.CommittableOffset)
