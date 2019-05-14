package io.logbee.keyscore.commons.metrics

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId}

import com.google.protobuf.timestamp.Timestamp

trait BaseMetricsQuery {
  this: MetricsQuery =>

  /**
    * Returns the number of seconds from the Java epoch of 1970-01-01T00:00:00Z.
    * @return The '''earliest''' timestamp in UnixTime
    */
  def earliestSeconds: Long = {
    LocalDateTime.parse(earliest, getPattern).atZone(ZoneId.systemDefault()).toInstant.getEpochSecond
  }

  /**
    * Returns the number of nanoseconds, later along the time-line, from the start of the second.
    * @return The nanoseconds of the '''earliest''' timestamp
    */
  def earliestNanos: Int = {
    LocalDateTime.parse(earliest, getPattern).atZone(ZoneId.systemDefault()).toInstant.getNano
  }

  /**
    * Returns the earliest Timestamp
    * @return [[Timestamp]]
    */
  def earliestTimestamp: Timestamp = {
    Timestamp(earliestSeconds, earliestNanos)
  }

  /**
    * Returns the number of seconds from the Java epoch of 1970-01-01T00:00:00Z.
    * @return The '''latest''' timestamp in UnixTime
    */
  def latestSeconds: Long = {
    LocalDateTime.parse(latest, getPattern).atZone(ZoneId.systemDefault()).toInstant.getEpochSecond
  }

  /**
    * Returns the number of nanoseconds, later along the time-line, from the start of the second.
    * @return The nanoseconds of the '''latest''' timestamp
    */
  def latestNanos: Int = {
    LocalDateTime.parse(latest, getPattern).atZone(ZoneId.systemDefault()).toInstant.getNano
  }

  /**
    * Returns the latest Timestamp
    * @return [[Timestamp]]
    */
  def latestTimestamp: Timestamp = {
    Timestamp(latestSeconds, latestNanos)
  }

  def getPattern: DateTimeFormatter = {
    DateTimeFormatter.ofPattern(format)
  }

}
