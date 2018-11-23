package io.logbee.keyscore.commons.util

import io.logbee.keyscore.model.util.Using.using

import scala.io.Source

object BannerPrinter {

  def printBanner(name: String = "/banner.txt"): Unit = {
    val banner: String = using(getClass.getResourceAsStream(name))(stream => {
      Source.fromInputStream(stream).mkString("")
    })
    println(banner)
  }
}
