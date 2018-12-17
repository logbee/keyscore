package io.logbee.keyscore.pipeline.contrib.math

object MathUtil {

  def approximatelyEqual(x: Double, y: Double, precision: Double = 0.000001) = {
    if ((x - y).abs < precision) true else false
  }
}
