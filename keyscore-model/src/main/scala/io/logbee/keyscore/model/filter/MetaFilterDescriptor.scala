package io.logbee.keyscore.model.filter

import java.util.{Locale, UUID}

import io.logbee.keyscore.model.sink.FilterDescriptor

class MetaFilterDescriptor(val id: UUID, val name: String, val descriptors: Map[Locale, FilterDescriptorFragment]) {

  def describe(language: Locale): FilterDescriptor = ???

  def canEqual(other: Any): Boolean = other.isInstanceOf[MetaFilterDescriptor]

  override def equals(other: Any): Boolean = other match {
    case that: MetaFilterDescriptor =>
      (that canEqual this) &&
        id == that.id
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(id)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}
