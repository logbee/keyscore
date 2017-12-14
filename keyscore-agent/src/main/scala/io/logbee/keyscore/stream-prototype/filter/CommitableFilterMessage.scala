package filter

import akka.kafka.ConsumerMessage

case class CommitableFilterMessage (value:String,committableOffset:ConsumerMessage.CommittableOffset)
