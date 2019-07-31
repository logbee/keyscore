package io.logbee.keyscore.pipeline.commons.util

import io.logbee.keyscore.model.data._

import scala.collection.mutable


object DefaultDataIndex {

  val UNDEFINED = Attribute(Name[String]("io.logbee.keyscore.pipeline.contrib.filter.batch.Attribute.UNDEFINED"), Some("<undefined>"))

  val FIELD_PRESENT = "field.present"
  val FIELD_ABSENT = "field.absent"

  def apply[T]() = new DefaultDataIndex[T]()

  def withAttributes(attribute: Attribute[_], attributes: Attribute[_]*)(dataset: Dataset): Set[Attribute[_]] = {
    (List(attributes:_*) :+ attribute).toSet
  }

  object Record {

    def byField(name: String)(record: Record): Set[Attribute[_]] = {
      record.fields.foreach {
        case Field(`name`, TextValue(value)) => return Set(Attribute(Name(name), Some(value)))
        case _ =>
      }

      Set.empty
    }

    def byPresentField(name: String, presentName: String = FIELD_PRESENT, absentName: String = FIELD_ABSENT)(record: Record): Set[Attribute[_]] = {

      if (record.fields.exists(field => name.equals(field.name))) {
        Set(Attribute(Name(name), Some(presentName)))
      }
      else {
        Set(Attribute(Name(name), Some(absentName)))
      }
    }

    def withField(name: String)(a: Record, b: Record): Int = {

      val fieldA = a.fields.find(field => field.name == name)
      val fieldB = b.fields.find(field => field.name == name)

      if(fieldA.isDefined && fieldB.isDefined) {
        (fieldA.get.value, fieldB.get.value) match {
          case (TextValue(valueA), TextValue(valueB)) => valueA.compareTo(valueB)
          case (BooleanValue(valueA), BooleanValue(valueB)) => valueA.compareTo(valueB)
          case (DecimalValue(valueA), DecimalValue(valueB)) => valueA.compareTo(valueB)
          case (NumberValue(valueA), NumberValue(valueB)) => valueA.compareTo(valueB)
          case (TimestampValue(secondsA, nanosA), TimestampValue(secondsB, nanosB)) =>
            val result = secondsA.compareTo(secondsB)
            if (result == 0) {
              nanosA.compareTo(nanosB)
            }
            else {
              result
            }
          case (DurationValue(secondsA, nanosA), DurationValue(secondsB, nanosB)) =>
            val result = secondsA.compareTo(secondsB)
            if (result == 0) {
              nanosA.compareTo(nanosB)
            }
            else {
              result
            }

          case _ => 0
        }
      }
      else 0
    }
  }
}

class DefaultDataIndex[T] extends DataIndex[T] {

  private val index = mutable.HashMap.empty[Name[_], mutable.HashMap[Option[_], mutable.HashSet[T]]]

  override def insert(element: T, func: (T => Set[Attribute[_]])*): Unit = {
    val attrs = func.flatMap(func => func(element))
    attrs.foreach(attr => {
      index.getOrElseUpdate(attr.name, mutable.HashMap.empty)
        .getOrElseUpdate(attr.value, mutable.HashSet.empty) += element
      index.getOrElseUpdate(attr.name, mutable.HashMap.empty)
        .getOrElseUpdate(None, mutable.HashSet.empty) += element
    })
  }

  override def select(characteristic: Characteristic, sortedBy: (T, T) => Int = (_, _) => 0): ResultSet[T] = {

    val elements = characteristic.intersectionSet.flatMap(characteristic => {
      index.get(characteristic.name).flatMap(_.get(characteristic.value))
    })

    val intersection = elements.foldLeft(elements.flatten) {
      case (result, set) => result.intersect(set)
    }.toList

    ResultSetImpl(intersection)
  }

  private case class ResultSetImpl(elements: List[T]) extends ResultSet[T]  {

    override def sort(func: (T, T) => Int, order: Order): ResultSet[T] = {
      order match {
        case Ascending => ResultSetImpl(elements.sortWith((a: T, b: T) => func(a, b) > 0))
        case _ => ResultSetImpl(elements.sortWith((a: T, b: T) => func(a, b) < 0))
      }
    }
  }
}
