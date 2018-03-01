package io.logbee.keyscore.model.filter

object FilterDescriptor {
  def apply(name: String, parameters: List[ParameterDescriptor]): FilterDescriptor = new FilterDescriptor(name, name, "", parameters)

  def apply(name: String, description: String, parameters: List[ParameterDescriptor]): FilterDescriptor = new FilterDescriptor(name, name, description, parameters)

}

case class FilterDescriptor(
                             name: String,
                             displayName: String,
                             description: String,
                             parameters: List[ParameterDescriptor] = List.empty,
                             category: String = "Filter"
                           )

trait ParameterDescriptor {
  val name: String
  val displayName: String
  val kind: String
  val mandatory: Boolean
}

object BooleanParameterDescriptor {
  def apply(name: String): BooleanParameterDescriptor = new BooleanParameterDescriptor(name, name, true, "boolean")

}

case class BooleanParameterDescriptor(name: String, displayName: String, mandatory: Boolean, kind: String) extends ParameterDescriptor

object TextParameterDescriptor {
  def apply(name: String): TextParameterDescriptor = new TextParameterDescriptor(name, name, true, ".*", "text")

  def apply(name: String, validator: String): TextParameterDescriptor = new TextParameterDescriptor(name, name, true, validator, "text")

}

case class TextParameterDescriptor(name: String, displayName: String, mandatory: Boolean, validator: String, kind: String) extends ParameterDescriptor


object IntParameterDescriptor {
  def apply(name: String): IntParameterDescriptor = new IntParameterDescriptor(name, name, true, "int")
}

case class IntParameterDescriptor(name: String, displayName: String, mandatory: Boolean, kind: String) extends ParameterDescriptor


object ListParameterDescriptor {
  def apply(name: String, element: ParameterDescriptor): ListParameterDescriptor = new ListParameterDescriptor(name, name, true, element, 0, Int.MaxValue, "list")

  def apply(name: String, element: ParameterDescriptor, min: Int): ListParameterDescriptor = new ListParameterDescriptor(name, name, true, element, min, Int.MaxValue, "list")

  def apply(name: String, element: ParameterDescriptor, min: Int, max: Int): ListParameterDescriptor = new ListParameterDescriptor(name, name, true, element, min, max, "list")

}

case class ListParameterDescriptor(name: String, displayName: String, mandatory: Boolean, element: ParameterDescriptor, min: Int, max: Int, kind: String) extends ParameterDescriptor


object MapParameterDescriptor {
  def apply(name: String, key: ParameterDescriptor, value: ParameterDescriptor, min: Int): MapParameterDescriptor = new MapParameterDescriptor(name, name, true, key, value, min, Int.MaxValue, "map")

  def apply(name: String, key: ParameterDescriptor, value: ParameterDescriptor, min: Int, max: Int): MapParameterDescriptor = new MapParameterDescriptor(name, name, true, key, value, min, max, "map")

}

case class MapParameterDescriptor(name: String, displayName: String, mandatory: Boolean, key: ParameterDescriptor, value: ParameterDescriptor, min: Int, max: Int, kind: String) extends ParameterDescriptor



