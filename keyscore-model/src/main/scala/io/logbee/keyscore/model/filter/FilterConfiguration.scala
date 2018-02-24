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
}


case class TextParameter(name: String, value: String) extends Parameter[String]

case class BooleanParameter(name: String, value: Boolean) extends Parameter[Boolean]

case class IntParameter(name: String, value: Int) extends Parameter[Int]

case class FloatParameter(name: String, value: Float) extends Parameter[Float]

case class TextMapParameter(name: String, value: Map[String,String]) extends Parameter[Map[String,String]]

case class TextListParameter(name: String, value: List[String]) extends Parameter[List[String]]
