package io.logbee.keyscore.commons.util

import io.logbee.keyscore.commons.util.Using.using

import scala.io.Source
import scala.util.Random


class RandomNameGenerator(source: String, seed: Long = new Random().nextLong()) {

  private val rng = new Random(seed)
  private val names: List[String] = using(getClass.getResourceAsStream(source))(stream => {
                                      Source.fromInputStream(stream).getLines().toList
                                    })

  def nextName(): String = {
    names(rng.nextInt(names.size))
  }
}
