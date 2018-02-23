package io.logbee.keyscore.model.filter

import java.util.UUID


case class FilterConfiguration(
                                id: UUID,
                                kind: String,
                                parameters: List[Parameter[_]]
                              ) {

  def getParameterValue[T](parameterName: String): T = {
    return parameters.find(p => p.name.equals(parameterName)).get.value.asInstanceOf[T]
  }


}


trait Parameter[T] {
  val name: String
  val value: T
  val kind:String
}


case class TextParameter(name: String, value: String,kind:String) extends Parameter[String]

case class BooleanParameter(name: String, value: Boolean,kind:String) extends Parameter[Boolean]

case class IntParameter(name: String, value: Int,kind:String) extends Parameter[Int]

case class FloatParameter(name: String, value: Float,kind:String) extends Parameter[Float]

case class TextMapParameter(name: String, value: Map[String,String],kind:String) extends Parameter[Map[String,String]]

case class TextListParameter(name: String, value: List[String],kind:String) extends Parameter[List[String]]

