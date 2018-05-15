package io.logbee.keyscore.model

import java.util.Locale

import io.logbee.keyscore.model.sink.FilterDescriptor

import scala.collection.mutable

trait Described {
  def descriptors: mutable.Map[Locale,FilterDescriptor]
}
