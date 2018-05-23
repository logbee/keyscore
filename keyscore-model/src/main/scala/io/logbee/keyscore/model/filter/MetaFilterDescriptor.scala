package io.logbee.keyscore.model.filter

import java.util.Locale.ENGLISH
import java.util.{Locale, UUID}

object MetaFilterDescriptor {
  def apply(id: UUID, name: String, descriptors: Map[Locale, FilterDescriptorFragment]) = new MetaFilterDescriptor(id, name, descriptors)
}

case class MetaFilterDescriptor(id: UUID, name: String, descriptors: Map[Locale, FilterDescriptorFragment]) {

  def describe(language: Locale = ENGLISH): FilterDescriptor = {
    var selectedFragment: FilterDescriptorFragment = null
    descriptors.get(language) match {
      case Some(fragment) =>
        selectedFragment = fragment
      case None => descriptors.get(ENGLISH) match {
        case Some(fragment) =>
          selectedFragment = fragment
        case None =>
          selectedFragment = descriptors.values.toList.head
      }
    }
    FilterDescriptor(id, name, selectedFragment.displayName, selectedFragment.description, selectedFragment.previousConnection, selectedFragment.nextConnection, selectedFragment.parameters, selectedFragment.category)
  }


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
