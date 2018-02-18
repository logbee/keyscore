package io.logbee.keyscore.frontier.sources

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.TestSink
import io.logbee.keyscore.frontier.filters.CommittableRecord
import org.scalatest.WordSpec

class HttpSourceSpec extends WordSpec {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  "A running HttpSource" should {

    "accept play load" in {

      val probe = Source.fromGraph(new HttpSource(HttpSourceConfiguration())).toMat(TestSink.probe[CommittableRecord])(Keep.right).run()

//      for (i <- 1 to 10) {
//        println(probe.requestNext(60 seconds))
//      }
    }
  }
}
