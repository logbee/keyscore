package filter

import akka.stream
import akka.stream.stage.{GraphStageLogic, InHandler, OutHandler}
import akka.stream.{Attributes, FlowShape, Inlet}

class ExtractToNewField(extractFrom:String,extractTo:String,regExRule:String,removeFrom:Boolean=false)
  extends Filter {
  implicit val formats = org.json4s.DefaultFormats
  val in = Inlet[CommitableFilterMessage]("extrToNew.in")
  val out = stream.Outlet[CommitableFilterMessage]("extrToNew.out")

  override val shape = FlowShape.of(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {
      setHandler(in,new InHandler {
        override def onPush(): Unit = {
          val inMsg = grab(in)
          val inMsgMap = inMsg.value
          var outMap = scala.collection.mutable.Map[String, String]()

          
        }
      })
      setHandler(out,new OutHandler {
        override def onPull(): Unit = {
          pull(in)
        }
      })
    }
  }
}
