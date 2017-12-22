package io.logbee.keyscore.frontier.filter

import akka.stream
import akka.stream.scaladsl.Flow
import akka.stream.stage.{GraphStageLogic, InHandler, OutHandler}
import akka.stream.{Attributes, FlowShape, Inlet}

object ExtractToNewFieldFilter {
  def apply(extractFrom: String, extractTo: String, regExRule: String, removeFrom: Boolean = false) =
    Flow.fromGraph(new ExtractToNewFieldFilter(extractFrom, extractTo, regExRule, removeFrom))
}

class ExtractToNewFieldFilter(extractFrom: String, extractTo: String, regExRule: String, removeFrom: Boolean = false)
  extends Filter {
  implicit val formats = org.json4s.DefaultFormats
  val in = Inlet[CommitableFilterMessage]("extrToNew.in")
  val out = stream.Outlet[CommitableFilterMessage]("extrToNew.out")

  override val shape = FlowShape.of(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {
      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val inMsg = grab(in)
          val inMsgMap = inMsg.value
          var outMap = scala.collection.mutable.Map[String, String]()

          outMap ++= inMsgMap

          val pattern = regExRule.r
          val value = pattern findFirstIn inMsgMap(extractFrom) match {
            case Some(x) => x
            case None => ""
          }
          outMap += extractTo -> value

          if (removeFrom) outMap -= outMap(extractFrom)

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
