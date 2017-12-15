/*
package filter

import java.util.NoSuchElementException

import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.stage.{GraphStageLogic, InHandler, OutHandler}

import scala.util.parsing.json.JSONObject
import scala.util.matching.Regex


class ExtractFieldsFilterOld(fieldsToExtract: List[String]) extends Filter {
  implicit val formats = org.json4s.DefaultFormats
  val in = Inlet[CommitableFilterMessage]("RegEx.in")
  val out = Outlet[CommitableFilterMessage]("RegEx.out")

  override val shape = FlowShape.of(in, out)

  override def createLogic(attr: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) {
      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val inMsg = grab(in)
          println("InMessage:::::: "+inMsg.value)
          val groupNamePattern: Regex = "\\(\\?<(\\w*)>".r
          var properties = scala.collection.mutable.Map[String, String]()
          fieldsToExtract.foreach { field =>

            val rule = s"""(?<$field>(?<=\\\"$field\\\"\\s{0,1}:\\s{0,1}\\\")[^\"]*)"""
            /* For every received message, the given regex rule is applied and a Seq with the new tags is created */
            val patter = rule.r(groupNamePattern.findAllMatchIn(rule).map(_.group(1)).toSeq: _*)

            try {
              properties += field -> patter.findFirstMatchIn(inMsg.value).map(m => m.groupNames.map(name => m.group(name))).get(0)
            } catch {
              //TODO: some useful exception handling
              case nse: NoSuchElementException => println("Did not find pattern")
            }

          }

          println(JSONObject(properties.toMap).toString())
          /* Create Json from map for filter chain and push it*/
          push(out, CommitableFilterMessage(properties.toMap, inMsg.committableOffset))
        }
      })

      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          pull(in)
        }
      })
    }


}
*/
