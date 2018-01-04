package io.logbee.keyscore.frontier.filter

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Sink}
import akka.stream.testkit.scaladsl.TestSource
import io.logbee.keyscore.model.{Event, TextField}

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

  val (sourceProbe, switchFut) = TestSource.probe[Event]
    .viaMat(GrokFilter(configuration))(Keep.both)
    .map(number => s"> $number")
    .toMat(Sink.foreach(println))(Keep.left)
    .run()

  switchFut.onComplete {
    case Success(switch) =>

      println("ANY KEY [1]")
      StdIn.readLine()

      sourceProbe.sendNext(Event(TextField("message", "Hello World")))
      sourceProbe.sendNext(Event(TextField("message", "This is a Test where A=42")))
      sourceProbe.sendNext(Event(TextField("message", "This is a Test where A=73")))

      println("ANY KEY [2]")
      StdIn.readLine()

      println("Open Valve")
      switch.configure(GrokFilterConfiguration(isPaused = Some(false))).onComplete {
        case Success(success) =>
          sourceProbe.sendNext(Event())
          sourceProbe.sendNext(Event())
          switch.configure(GrokFilterConfiguration(isPaused = Some(true), pattern = Some(":\\s?(?<state>\\w*)"))).onComplete {
            case Success(success) =>
              sourceProbe.sendNext(Event(TextField("message", "Hello World foo: fubar")))
              sourceProbe.sendNext(Event(TextField("message", "Hello World muh: kuh")))
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
