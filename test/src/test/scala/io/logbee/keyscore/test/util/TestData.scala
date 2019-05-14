package io.logbee.keyscore.test.util

import io.logbee.keyscore.commons.metrics.MetricsQuery

object TestData {

  val format = "dd.MM.yyy_HH:mm:ss:nnnnnnnnn"
  val standardTimestamp = MetricsQuery(limit = 100, earliest = "01.01.2000_00:00:00:000000000", latest = "31.12.9999_23:59:59:999999999", format = format)

}
