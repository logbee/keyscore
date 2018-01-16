package io.logbee.keyscore.model.filter

object FilterDescriptor {
  def apply(name: String, parameters: List[ParameterDescriptor]): FilterDescriptor = new FilterDescriptor(name, name, "", parameters)

  def apply(name: String, description: String, parameters: List[ParameterDescriptor]): FilterDescriptor = new FilterDescriptor(name, name, description, parameters)

}

case class FilterDescriptor(
                             name: String,
                             displayName: String,
                             description: String,
                             parameters: List[ParameterDescriptor] = List.empty
                           )

trait ParameterDescriptor {
  val name: String
  val displayName: String
  val kind: String
  val mandatory: Boolean
}

object BooleanParameterDescriptor extends {
  def apply(name: String): BooleanParameterDescriptor = new BooleanParameterDescriptor(name, name, true)

}

case class BooleanParameterDescriptor(name: String, displayName: String, mandatory: Boolean) extends ParameterDescriptor {
  override val kind: String = "boolean"
}

object TextParameterDescriptor {
  def apply(name: String): TextParameterDescriptor = new TextParameterDescriptor(name, name, true, ".*")

  def apply(name: String, validator: String): TextParameterDescriptor = new TextParameterDescriptor(name, name, true, validator)

}

case class TextParameterDescriptor(name: String, displayName: String, mandatory: Boolean, validator: String) extends ParameterDescriptor {
  override val kind: String = "text"
}


object ListParameterDescriptor {
  def apply(name: String, element: ParameterDescriptor): ListParameterDescriptor = new ListParameterDescriptor(name, name, true, element, 0, Int.MaxValue)

  def apply(name: String, element: ParameterDescriptor, min: Int): ListParameterDescriptor = new ListParameterDescriptor(name, name, true, element, min, Int.MaxValue)

  def apply(name: String, element: ParameterDescriptor, min: Int, max: Int): ListParameterDescriptor = new ListParameterDescriptor(name, name, true, element, min, max)

}

case class ListParameterDescriptor(name: String, displayName: String, mandatory: Boolean, element: ParameterDescriptor, min: Int, max: Int) extends ParameterDescriptor {
  override val kind: String = "list"
}


object MapParameterDescriptor {
  def apply(name: String, key: ParameterDescriptor, value: ParameterDescriptor, min: Int): MapParameterDescriptor = new MapParameterDescriptor(name, name, true, key, value, min, Int.MaxValue)

  def apply(name: String, key: ParameterDescriptor, value: ParameterDescriptor, min: Int, max: Int): MapParameterDescriptor = new MapParameterDescriptor(name, name, true, key, value, min, max)

}

case class MapParameterDescriptor(name: String, displayName: String, mandatory: Boolean, key: ParameterDescriptor, value: ParameterDescriptor, min: Int, max: Int) extends ParameterDescriptor {
  override val kind = "map"
}



