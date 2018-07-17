package io.logbee.keyscore.model.filter

import java.util.UUID

object FilterDescriptor {

  def apply(id: UUID, name: String): FilterDescriptor = new FilterDescriptor(id, name, "", "", FilterConnection(isPermitted = true), FilterConnection(isPermitted = true), List.empty)

  def apply(id: UUID, name: String, parameters: List[ParameterDescriptor]): FilterDescriptor = new FilterDescriptor(id, name, name, "", FilterConnection(isPermitted = true), FilterConnection(isPermitted = true), parameters)

  def apply(id: UUID, name: String, description: String, parameters: List[ParameterDescriptor]): FilterDescriptor = new FilterDescriptor(id, name, name, description, FilterConnection(isPermitted = true), FilterConnection(isPermitted = true), parameters)

  def apply(id: UUID, name: String, description: String, previousConnection: FilterConnection, nextConnection: FilterConnection, parameters: List[ParameterDescriptor]): FilterDescriptor = new FilterDescriptor(id, name, name, description, previousConnection, nextConnection, parameters)
}

case class FilterDescriptorFragment(
                                     displayName: String,
                                     description: String,
                                     previousConnection: FilterConnection,
                                     nextConnection: FilterConnection,
                                     parameters: List[ParameterDescriptor] = List.empty,
                                     category: String = "Filter",
                                   )

case class FilterDescriptor(
                             id: UUID,
                             name: String,
                             displayName: String,
                             description: String,
                             previousConnection: FilterConnection,
                             nextConnection: FilterConnection,
                             parameters: List[ParameterDescriptor] = List.empty,
                             category: String = "Filter",
                           )

case class FilterConnection(isPermitted: Boolean, connectionType: List[String] = List.empty)

trait ParameterDescriptor {
  val name: String
  val displayName: String
  val description: String
  val mandatory: Boolean
}


object BooleanParameterDescriptor {
  def apply(name: String): BooleanParameterDescriptor = new BooleanParameterDescriptor(name, name, "", true)

  def apply(name: String, displayName: String, description: String): BooleanParameterDescriptor = new BooleanParameterDescriptor(name, displayName, description, true)

  def apply(name: String, displayName: String, description: String, mandatory: Boolean): BooleanParameterDescriptor = new BooleanParameterDescriptor(name, displayName, description, mandatory)
}

case class BooleanParameterDescriptor(name: String, displayName: String, description: String, mandatory: Boolean) extends ParameterDescriptor

object TextParameterDescriptor {
  def apply(name: String): TextParameterDescriptor = new TextParameterDescriptor(name, name, "", true, ".*")

  def apply(name: String, validator: String): TextParameterDescriptor = new TextParameterDescriptor(name, name, "", true, validator)

  def apply(name: String, displayName: String, description: String): TextParameterDescriptor = new TextParameterDescriptor(name, displayName, description, true, ".*")

  def apply(name: String, displayName: String, description: String, mandatory: Boolean, validator: String): TextParameterDescriptor = new TextParameterDescriptor(name, displayName, description, mandatory, validator)

}

case class TextParameterDescriptor(name: String, displayName: String, description: String, mandatory: Boolean, validator: String) extends ParameterDescriptor


object IntParameterDescriptor {
  def apply(name: String): IntParameterDescriptor = new IntParameterDescriptor(name, name, "", true)

  def apply(name: String, displayName: String, description: String): IntParameterDescriptor = new IntParameterDescriptor(name, displayName, description, true)

  def apply(name: String, displayName: String, description: String, mandatory: Boolean): IntParameterDescriptor = new IntParameterDescriptor(name, displayName, description, mandatory)
}

case class IntParameterDescriptor(name: String, displayName: String, description: String, mandatory: Boolean) extends ParameterDescriptor


object ListParameterDescriptor {
  def apply(name: String, element: ParameterDescriptor): ListParameterDescriptor = new ListParameterDescriptor(name, name, "", true, element, 1, Int.MaxValue)

  def apply(name: String, displayName: String, description: String, element: ParameterDescriptor): ListParameterDescriptor = new ListParameterDescriptor(name, name, "", true, element, 1, Int.MaxValue)

  def apply(name: String, displayName: String, description: String, element: ParameterDescriptor, mandatory: Boolean): ListParameterDescriptor = new ListParameterDescriptor(name, displayName, description, mandatory, element, 0, Int.MaxValue)


  def apply(name: String, element: ParameterDescriptor, min: Int): ListParameterDescriptor = new ListParameterDescriptor(name, name, "", true, element, min, Int.MaxValue)

  def apply(name: String, element: ParameterDescriptor, min: Int, max: Int): ListParameterDescriptor = new ListParameterDescriptor(name, name, "", true, element, min, max)

  def apply(name: String, displayName: String, description: String, element: ParameterDescriptor, min: Int, max: Int): ListParameterDescriptor = new ListParameterDescriptor(name, displayName, description, true, element, min, max)
}

case class ListParameterDescriptor(name: String, displayName: String, description: String, mandatory: Boolean, element: ParameterDescriptor, min: Int, max: Int) extends ParameterDescriptor


object MapParameterDescriptor {

  def apply(name: String, key: ParameterDescriptor, value: ParameterDescriptor, min: Int): MapParameterDescriptor = new MapParameterDescriptor(name, name, "", true, key, value, min, Int.MaxValue)

  def apply(name: String, key: ParameterDescriptor, value: ParameterDescriptor, min: Int, max: Int): MapParameterDescriptor = new MapParameterDescriptor(name, name, "", true, key, value, min, max)

  def apply(name: String, displayName: String, description: String, key: ParameterDescriptor, value: ParameterDescriptor): MapParameterDescriptor = new MapParameterDescriptor(name, displayName, description, true, key, value, 1, Int.MaxValue)

}

case class MapParameterDescriptor(name: String, displayName: String, description: String, mandatory: Boolean, mapKey: ParameterDescriptor, mapValue: ParameterDescriptor, min: Int, max: Int) extends ParameterDescriptor
