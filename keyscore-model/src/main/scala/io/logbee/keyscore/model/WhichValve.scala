package io.logbee.keyscore.model

object WhichValve {
  implicit def whichValve(which: String): WhichValve = {
    which match {
      case "before" => Before
      case "after" => After
      case _ => throw new IllegalArgumentException()
    }
  }
}

sealed trait WhichValve

case object Before extends WhichValve
case object After extends WhichValve

