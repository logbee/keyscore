package filter

import java.text.SimpleDateFormat
import java.util.{Calendar, NoSuchElementException}

import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.stage.{GraphStageLogic, InHandler, OutHandler}

import scala.util.parsing.json.JSONObject
import scala.util.matching.Regex

class RegExFilter(rules: String) extends Filter {
  val in = Inlet[CommitableFilterMessage]("RegEx.in")
  val out = Outlet[CommitableFilterMessage]("RegEx.out")

  override val shape = FlowShape.of(in, out)

  override def createLogic(attr: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) {
      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val inMsg = grab(in)
          val groupNamePattern: Regex = "\\(\\?<(\\w*)>".r
          /* For every received message, the given regex rule is applied and a Seq with the new tags is created */
          val patter = rules.r(groupNamePattern.findAllMatchIn(rules).map(_.group(1)).toSeq: _*)
          /* A new map includes now the new tags and their values from the given regex */
          var properties = Map[String,String]()
          try {
            properties = Map(patter.findFirstMatchIn(inMsg.value).map(m => m.groupNames.map(name => (name, m.group(name)))).get: _*)
          }catch{
            //TODO: some useful exception handling
            case nse:NoSuchElementException => println("Did not find pattern")
          }
          properties += "akka_timestamp" -> FilterUtils.getCurrentTimeFormatted
          println(JSONObject(properties).toString())

          /* Create Json from map for filter chain and push it*/
          push(out, CommitableFilterMessage(JSONObject(properties).toString(), inMsg.committableOffset))
        }
      })

      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          pull(in)
        }
      })
    }


}
