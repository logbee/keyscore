package io.logbee.keyscore.commons.util

import java.text.SimpleDateFormat
import java.util.TimeZone

import ch.qos.logback.classic.spi.{ILoggingEvent, IThrowableProxy, StackTraceElementProxy}
import ch.qos.logback.core.LayoutBase
import io.logbee.keyscore.commons.util.KeyscoreLoggingLayout._

import scala.annotation.tailrec

object KeyscoreLoggingLayout {
  val MAPPED_DIAGNOSTIC_CONTEXT_AKKA_TIMESTAMP = "akkaTimestamp"
  val MAPPED_DIAGNOSTIC_CONTEXT_AKKA_SOURCE = "akkaSource"

  object StackTraceElement {
    def unapply(arg: StackTraceElementProxy): Option[(String, String, String, Option[String])] = {
      if (arg != null) {
        Some((
          arg.getStackTraceElement.getClassName,
          arg.getStackTraceElement.getMethodName,
          arg.getStackTraceElement.getFileName,
          if (arg.getStackTraceElement.getLineNumber > 0) Some(arg.getStackTraceElement.getLineNumber.toString) else None,
        ))
      }
      else {
        None
      }
    }
  }
}

class KeyscoreLoggingLayout extends LayoutBase[ILoggingEvent] {

  private val dateFormat = { 
    val format = new SimpleDateFormat("yyyy-MM-dd")
    format.setTimeZone(TimeZone.getTimeZone("UTC"))
    format
  }

  private val timeFormat = {
    val format = new SimpleDateFormat("HH:mm:ss.SSS")
    format.setTimeZone(TimeZone.getTimeZone("UTC"))
    format
  }

  override def doLayout(event: ILoggingEvent): String = {

    val timestamp = Option(event.getMDCPropertyMap.get(MAPPED_DIAGNOSTIC_CONTEXT_AKKA_TIMESTAMP))
      .map(timestamp => s"${dateFormat.format(event.getTimeStamp)} ${timestamp}")
      .getOrElse(s"${dateFormat.format(event.getTimeStamp)} ${timeFormat.format(event.getTimeStamp)}UTC")

    val source = Option(event.getMDCPropertyMap.get(MAPPED_DIAGNOSTIC_CONTEXT_AKKA_SOURCE))
      .map(source => s"$source ")
      .getOrElse("")
    
    val throwable = Option(event.getThrowableProxy)
      .map(throwable => {
        @tailrec
        def formatThrowable(throwable: IThrowableProxy, result: String): String = {
          val newResult = result + throwable.getStackTraceElementProxyArray.foldLeft(s"${throwable.getClassName}: ${throwable.getMessage}") {
            case (result, StackTraceElement(className, methodName, fileName, lineNumber)) =>
              val resolvedLineNumber = lineNumber.map(line => s":$line").getOrElse("")
              s"$result\n\t\tat $className.$methodName($fileName$resolvedLineNumber)"
          }
          if (throwable.getCause != null) formatThrowable(throwable.getCause, s"$newResult\ncaused by: ") else newResult
        }
        formatThrowable(throwable, "\n")
      })
      .getOrElse("")

    s"""[$timestamp] [${event.getLevel}] $source${event.getLoggerName} - ${event.getFormattedMessage}$throwable\n"""
  }
}
