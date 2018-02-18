package io.logbee.keyscore.frontier.filters

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Sink}
import akka.stream.testkit.scaladsl.TestSource
import io.logbee.keyscore.model.TextField

import scala.io.StdIn
import scala.util.Success

object GrokFilterExample extends App {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = materializer.executionContext

  private val configuration = GrokFilterConfiguration(
    isPaused = Some(true),
    fieldNames = Some(List("message")),
    pattern = Some("=(?<state>\\w*)")
  )

  val (sourceProbe, switchFut) = TestSource.probe[CommittableRecord]
    .viaMat(GrokFilter(configuration))(Keep.both)
    .map(number => s"> $number")
    .toMat(Sink.foreach(println))(Keep.left)
    .run()

  switchFut.mapTo[GrokFilterHandle].onComplete {
    case Success(switch) =>
      println("ANY KEY [1]")
      StdIn.readLine()

      // These three records wont get processed until the next key is pressed, because the filter is paused.
      sourceProbe.sendNext(CommittableRecord(TextField("message", "Hello World")))
      sourceProbe.sendNext(CommittableRecord(TextField("message", "This is a Test where A=42")))
      sourceProbe.sendNext(CommittableRecord(TextField("message", "This is a Test where A=73")))

      println("ANY KEY [2]")
      StdIn.readLine()

      println("Open Valve")
      switch.configure(GrokFilterConfiguration(isPaused = Some(false))).onComplete {
        case Success(success) =>
          sourceProbe.sendNext(CommittableRecord())
          sourceProbe.sendNext(CommittableRecord())
          // Five Records have to be printed to the console - the two above too. Now the filter gets paused again.
          switch.configure(GrokFilterConfiguration(isPaused = Some(true), pattern = Some(":\\s?(?<state>\\w*)"))).onComplete {
            case Success(success) =>

              // Not printed; filter still paused.
              sourceProbe.sendNext(CommittableRecord(TextField("message", "Hello World foo: fubar")))
              sourceProbe.sendNext(CommittableRecord(TextField("message", "Hello World muh: kuh")))

              // Unpause the filter and to print the last two messages.
              println("ANY KEY [3]")
              StdIn.readLine()
              switch.configure(GrokFilterConfiguration(isPaused = Some(false)))

              println("ANY KEY [4]")
              StdIn.readLine()
              system.terminate()
          }
      }
  }
}
