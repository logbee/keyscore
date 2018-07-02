package io.logbee.keyscore.agent.pipeline.external

import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import akka.util.ByteString
import io.logbee.keyscore.agent.pipeline.TcpFilterLogic.Message
import io.logbee.keyscore.agent.pipeline.{Outbound, TestSystemWithMaterializerAndExecutionContext}
import org.scalatest.WordSpec

class TcpFilterLogicSpec extends WordSpec with TestSystemWithMaterializerAndExecutionContext {

  "foo" should {

    val (source, sink) = Source.fromGraph(TestSource.probe[Message])
      .viaMat(new Outbound())(Keep.left)
      .toMat(TestSink.probe[ByteString])(Keep.both)
      .run()

    "bar" in {

    }
  }
}
