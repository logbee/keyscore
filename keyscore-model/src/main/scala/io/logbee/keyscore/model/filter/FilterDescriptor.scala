package io.logbee.keyscore.model.filter

import org.graalvm.compiler.debug.DebugContext.Description

object FilterDescriptor {
  def apply(name: String, parameters: List[ParameterDescriptor]): FilterDescriptor = new FilterDescriptor(name, name, "", FilterConnection(true),FilterConnection(true),parameters)

  def apply(name: String, description: String, parameters: List[ParameterDescriptor]): FilterDescriptor = new FilterDescriptor(name, name, description,FilterConnection(true),FilterConnection(true),parameters)

  def apply(name: String, description: String, previousConnection: FilterConnection, nextConnection: FilterConnection, parameters: List[ParameterDescriptor]): FilterDescriptor = new FilterDescriptor(name, name, description, previousConnection, nextConnection, parameters)

}

case class FilterDescriptor(
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
  val kind: String
  val mandatory: Boolean
}


object BooleanParameterDescriptor {
  def apply(name: String): BooleanParameterDescriptor = new BooleanParameterDescriptor(name, name, "",true, "boolean")

  def apply(name: String,displayName:String,description:String): BooleanParameterDescriptor = new BooleanParameterDescriptor(name, displayName, description,true, "boolean")

  def apply(name: String,displayName:String,description:String, mandatory:Boolean): BooleanParameterDescriptor = new BooleanParameterDescriptor(name, displayName, description,mandatory, "boolean")
}

case class BooleanParameterDescriptor(name: String, displayName: String, description: String,mandatory: Boolean, kind: String) extends ParameterDescriptor

object TextParameterDescriptor {
  def apply(name: String): TextParameterDescriptor = new TextParameterDescriptor(name, name, "",true, ".*", "text")

  def apply(name: String, validator: String): TextParameterDescriptor = new TextParameterDescriptor(name, name, "",true, validator, "text")

  def apply(name:String,displayName:String,description:String):TextParameterDescriptor = new TextParameterDescriptor(name,displayName,description,true,".*","text")

  def apply(name:String,displayName:String,description:String,mandatory:Boolean,validator:String):TextParameterDescriptor = new TextParameterDescriptor(name,displayName,description,mandatory,validator,"text")

}

case class TextParameterDescriptor(name: String, displayName: String, description: String, mandatory: Boolean, validator: String, kind: String) extends ParameterDescriptor


object IntParameterDescriptor {
  def apply(name: String): IntParameterDescriptor = new IntParameterDescriptor(name, name, "",true, "int")

  def apply(name:String,displayName:String,description:String):IntParameterDescriptor = new IntParameterDescriptor(name,displayName,description,true,"int")

  def apply(name:String,displayName:String,description:String,mandatory:Boolean):IntParameterDescriptor = new IntParameterDescriptor(name,displayName,description,mandatory,"int")
}

case class IntParameterDescriptor(name: String, displayName: String,description: String, mandatory: Boolean, kind: String) extends ParameterDescriptor


object ListParameterDescriptor {
  def apply(name: String, element: ParameterDescriptor): ListParameterDescriptor = new ListParameterDescriptor(name, name, "",true, element, 1, Int.MaxValue, "list")

  def apply(name: String, displayName:String,description:String,element: ParameterDescriptor): ListParameterDescriptor = new ListParameterDescriptor(name, name, "",true, element, 1, Int.MaxValue, "list")

  def apply(name: String, displayName:String,description:String,element: ParameterDescriptor,mandatory:Boolean): ListParameterDescriptor = new ListParameterDescriptor(name, displayName, description,mandatory, element, 0, Int.MaxValue, "list")


  def apply(name: String, element: ParameterDescriptor, min: Int): ListParameterDescriptor = new ListParameterDescriptor(name, name, "",true, element, min, Int.MaxValue, "list")

  def apply(name: String, element: ParameterDescriptor, min: Int, max: Int): ListParameterDescriptor = new ListParameterDescriptor(name, name, "",true, element, min, max, "list")

  def apply(name: String, displayName:String,description:String, element: ParameterDescriptor, min: Int, max: Int): ListParameterDescriptor = new ListParameterDescriptor(name, displayName, description,true, element, min, max, "list")
}

case class ListParameterDescriptor(name: String, displayName: String, description:String, mandatory: Boolean, element: ParameterDescriptor, min: Int, max: Int, kind: String) extends ParameterDescriptor


object MapParameterDescriptor {

  def apply(name: String, key: ParameterDescriptor, value: ParameterDescriptor, min: Int): MapParameterDescriptor = new MapParameterDescriptor(name, name, "",true, key, value, min, Int.MaxValue, "map")

  def apply(name: String, key: ParameterDescriptor, value: ParameterDescriptor, min: Int, max: Int): MapParameterDescriptor = new MapParameterDescriptor(name, name, "",true, key, value, min, max, "map")

  def apply(name:String, displayName:String,description:String,key:ParameterDescriptor,value:ParameterDescriptor):MapParameterDescriptor=new MapParameterDescriptor(name,displayName,description,true,key,value,1,Int.MaxValue,"map")

}

case class MapParameterDescriptor(name: String, displayName: String, description: String,mandatory: Boolean, mapKey: ParameterDescriptor, mapValue: ParameterDescriptor, min: Int, max: Int, kind: String) extends ParameterDescriptor
