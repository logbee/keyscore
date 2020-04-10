package io.logbee.keyscore.commons.logging


import ch.qos.logback.classic.{Level, LoggerContext}
import ch.qos.logback.classic.spi.LoggingEvent
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatestplus.junit.JUnitRunner
import java.util

import io.logbee.keyscore.model.util.ToOption.T2OptionT
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

@RunWith(classOf[JUnitRunner])
class KeyscoreLoggingLayoutSpec extends AnyFreeSpec with Matchers with MockFactory {

  "A LoggingLayout should" - {
    val testSetups = Seq(
      (
        "a simple log event",
        event(
          timestamp = 1567116061562L,
        ),
        """\[2019-08-29 22:01:01\.562UTC\] \[DEBUG\] i\.l\.k\.HelloWorld - Hello World\n""".r
      ),
      (
        "a log event with akkaTimestamp and akkaSource",
        event(
          timestamp = 1567116061562L,
          message = "Initializing Agent with FilterManager",
          akkaTimestamp = "23:32:47.321UTC",
          akkaSource = "akka.tcp://keyscore@keyscore-agent:3551/user/agent"
        ),
        """\[2019-08-29 23:32:47\.321UTC\] \[DEBUG\] akka\.tcp://keyscore@keyscore-agent:3551/user/agent i\.l\.k\.HelloWorld - Initializing Agent with FilterManager\n""".r
      ),
      (
        "a log event with a throwable",
        event(
          timestamp = 1567116061562L,
          message = "Initializing Agent with FilterManager",
          throwable = throwable()
        ),
        """#(?s)\[2019-08-29 22:01:01\.562UTC\] \[DEBUG\] i\.l\.k\.HelloWorld - Initializing Agent with FilterManager.
           #java\.lang\.RuntimeException: Something went wrong!.
           #\s+at io\.logbee\.keyscore\.commons\.logging\.KeyscoreLoggingLayoutSpec\.throwable\(KeyscoreLoggingLayoutSpec\.scala:\d+\).
           #\s+at io\.logbee\.keyscore\.commons\.logging\.KeyscoreLoggingLayoutSpec\.\$anonfun\$new\$1\(KeyscoreLoggingLayoutSpec\.scala:\d+\).*
           #caused by: java\.lang\.IllegalArgumentException: You put in the wrong thing!.
           #\s+at io\.logbee\.keyscore\.commons\.logging\.KeyscoreLoggingLayoutSpec\.throwable\(KeyscoreLoggingLayoutSpec\.scala:\d+\).
           #\s+at io\.logbee\.keyscore\.commons\.logging\.KeyscoreLoggingLayoutSpec\.\$anonfun\$new\$1\(KeyscoreLoggingLayoutSpec\.scala:\d+\).*"""
          .stripMargin('#').replace("\n", "").r
      )
    )

    testSetups.foreach {
      case (description, event, expectation) =>
        s"print out $description" in {

          val testee = new KeyscoreLoggingLayout()
          val layout = testee.doLayout(event)

          layout should fullyMatch regex expectation
        }
      }
  }

  def event(timestamp: Option[Long] = None, akkaTimestamp: Option[String] = None, akkaSource: Option[String] = None, message: Option[String] = "Hello World", throwable: Option[Throwable] = None): LoggingEvent = {
    val testLogger = new LoggerContext().getLogger("i.l.k.HelloWorld")
    
    val event = new LoggingEvent("i.l.k.HelloWorld", testLogger, Level.DEBUG, message.get, throwable.orNull, Array.empty)
    
    timestamp.foreach(event.setTimeStamp)
    event.setMDCPropertyMap(new util.HashMap[String, String]())
    akkaTimestamp.foreach(event.getMDCPropertyMap.put(KeyscoreLoggingLayout.MAPPED_DIAGNOSTIC_CONTEXT_AKKA_TIMESTAMP, _))
    akkaSource.foreach(event.getMDCPropertyMap.put(KeyscoreLoggingLayout.MAPPED_DIAGNOSTIC_CONTEXT_AKKA_SOURCE, _))

    event
  }

  def throwable(): Throwable = {
    try {
      throw new IllegalArgumentException("You put in the wrong thing!")
    }
    catch {
      case e: Throwable => 
        try {
          throw new RuntimeException("Something went wrong!", e)
        }
        catch {
          case e: Throwable => e
        }
    }
  }
}
