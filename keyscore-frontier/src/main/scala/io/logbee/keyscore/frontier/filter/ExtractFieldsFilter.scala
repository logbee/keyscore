package filter

import akka.stream
import akka.stream.scaladsl.Flow
import akka.stream.stage.{GraphStageLogic, InHandler, OutHandler}
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import org.json4s.native.Serialization

object ExtractFieldsFilter {

  def apply(fieldsToExtract: List[String]) = Flow.fromGraph(new ExtractFieldsFilter(fieldsToExtract))
}

class ExtractFieldsFilter(fieldsToExtract: List[String]) extends Filter {

  implicit val formats = org.json4s.DefaultFormats
  val in = Inlet[CommitableFilterMessage]("extr.in")
  val out = stream.Outlet[CommitableFilterMessage]("extr.out")

  override val shape = FlowShape.of(in, out)

  override def createLogic(attr: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) {
      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val inMsg = grab(in)
          val inMsgMap = inMsg.value
          var outMap = scala.collection.mutable.Map[String, String]()

          fieldsToExtract.foreach { field =>
            outMap += field -> inMsgMap(field)
          }
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
