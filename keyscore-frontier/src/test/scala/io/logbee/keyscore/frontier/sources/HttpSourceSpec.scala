package io.logbee.keyscore.frontier.sources

import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import akka.stream.{ActorMaterializer, Graph, SourceShape}
import io.logbee.keyscore.frontier.filters.CommittableEvent
import org.scalatest.WordSpec

import scala.io.StdIn

class HttpSourceSpec extends WordSpec {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  "A running HttpSource" should {

    "accept play load" in {

      val graph: Graph[SourceShape[CommittableEvent], String] = new HttpSource()

      val source: Source[CommittableEvent, String] = Source.fromGraph(graph)

      source.runForeach(println)

      StdIn.readLine()
    }
  }
}
