package io.logbee.keyscore.commons.ehcache

import java.util.UUID

import com.google.protobuf.timestamp.Timestamp

abstract class Cache {

  def newerThanLatest(actual: Timestamp, latest: Timestamp): Boolean = {
    if (actual.seconds > latest.seconds) true
    else if (actual.seconds == latest.seconds) {
      if (actual.nanos > latest.nanos) true
      else false
    }
    else false
  }

  def olderThanEarliest(actual: Timestamp, earliest: Timestamp): Boolean = {
    if(actual.seconds < earliest.seconds) true
    else if (actual.seconds == earliest.seconds) {
      if (actual.nanos < earliest.nanos) true
      else false
    }
    else false
  }

  def calculateKey(id: UUID, counter: Long): String = {
    id.toString + "|" + counter
  }


}
