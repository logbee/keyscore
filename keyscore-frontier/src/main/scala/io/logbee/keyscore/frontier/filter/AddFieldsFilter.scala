package io.logbee.keyscore.frontier.filter

import akka.{NotUsed, stream}
import akka.stream.scaladsl.Flow
import akka.stream.stage.{GraphStageLogic, InHandler, OutHandler}
import akka.stream.{Attributes, FlowShape, Inlet}
import org.json4s.DefaultFormats
import org.json4s.native.Serialization

object AddFieldsFilter {
  def apply(fieldsToAdd: Map[String, String]): Flow[CommitableFilterMessage, CommitableFilterMessage, NotUsed] =
    Flow.fromGraph(new AddFieldsFilter(fieldsToAdd))
}

class AddFieldsFilter(fieldsToAdd: Map[String, String]) extends Filter {
  implicit val formats: DefaultFormats.type = org.json4s.DefaultFormats

  val in = Inlet[CommitableFilterMessage]("addFields.in")
  val out = stream.Outlet[CommitableFilterMessage]("addFields.out")

  override val shape = FlowShape.of(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {

      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val inMsg = grab(in)
          val inMsgMap = inMsg.value
          var outMap = scala.collection.mutable.Map[String, String]()

          outMap ++= inMsgMap
          outMap ++= fieldsToAdd

          println(Serialization.write(outMap))
          push(out, CommitableFilterMessage(outMap.toMap, inMsg.committableOffset))
        }
      })

      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          pull(in)
        }
      })
    }

  }
}
