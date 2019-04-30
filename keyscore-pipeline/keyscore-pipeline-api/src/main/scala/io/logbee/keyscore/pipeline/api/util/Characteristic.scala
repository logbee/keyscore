package io.logbee.keyscore.pipeline.api.util

case class Name[+V](name: String)

case class Attribute[+V](name: Name[V], value: Option[V] = None)

object Characteristic {

  def whereAll(attribute: Attribute[_], attributes: Attribute[_]*): Characteristic =
    Characteristic((attributes :+ attribute).toSet, Set.empty, Set.empty)

  def whereOne(attribute: Attribute[_], attributes: Attribute[_]*): Characteristic =
    Characteristic(Set.empty, (attributes :+ attribute).toSet, Set.empty)

  def whereNone(attribute: Attribute[_], attributes: Attribute[_]*): Characteristic =
    Characteristic(Set.empty, Set.empty, (attributes :+ attribute).toSet)

}

case class Characteristic(intersectionSet: Set[Attribute[_]], unificationSet: Set[Attribute[_]], differenceSet: Set[Attribute[_]]) {

  def whereAll(attribute: Attribute[_], attributes: Attribute[_]*): Characteristic = Characteristic(
    intersectionSet ++ attributes + attribute, unificationSet, differenceSet
  )

  def whereOne(attribute: Attribute[_], attributes: Attribute[_]*): Characteristic = Characteristic(
    intersectionSet, unificationSet ++ attributes + attribute, differenceSet
  )

  def whereNone(attribute: Attribute[_], attributes: Attribute[_]*): Characteristic = Characteristic(
    intersectionSet, unificationSet, differenceSet ++ attributes + attribute
  )
}
