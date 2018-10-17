package io.logbee.keyscore.model.configuration

import io.logbee.keyscore.model.data.Field
import io.logbee.keyscore.model.descriptor._

import scala.collection.mutable


trait QueryableParameterSet {
  this: ParameterSet =>

  private def parameterMapping: Map[String, Any] = parameters.foldLeft(mutable.Map.empty[String, Any]) {
    case (result, parameter: BooleanParameter) => result + (parameter.ref.id -> parameter.value)
    case (result, parameter: TextParameter) => result + (parameter.ref.id -> parameter.value)
    case (result, parameter: ExpressionParameter) => result + (parameter.ref.id -> parameter.value)
    case (result, parameter: NumberParameter) => result + (parameter.ref.id -> parameter.value)
    case (result, parameter: DecimalParameter) => result + (parameter.ref.id -> parameter.value)
    case (result, parameter: FieldNameParameter) => result + (parameter.ref.id -> parameter.value)
    case (result, parameter: FieldParameter) => result + (parameter.ref.id -> parameter.value)
    case (result, parameter: TextListParameter) => result + (parameter.ref.id -> parameter.value)
    case (result, parameter: FieldNameListParameter) => result + (parameter.ref.id -> parameter.value)
    case (result, parameter: FieldListParameter) => result + (parameter.ref.id -> parameter.value)
    case (result, parameter: ChoiceParameter) => result + (parameter.ref.id -> parameter.value)
    case (result, parameter: FieldDirectiveSequenceParameter) => result + (parameter.ref.id -> parameter.sequences)
    case (result, parameter: DirectiveConfiguration) => result + (parameter.ref.uuid -> parameter.parameters)
    case (result, _) => result
  }.toMap

  def findBooleanValue(ref: ParameterRef): Option[Boolean] = parameterMapping.get(ref.id) match {
    case Some(value: Boolean) => Some(value)
    case _ => None
  }

  def findTextValue(ref: ParameterRef): Option[String] = parameterMapping.get(ref.id) match {
    case Some(value: String) => Some(value)
    case _ => None
  }

  def findValue(descriptor: BooleanParameterDescriptor): Option[Boolean] = parameterMapping.get(descriptor.ref.id) match {
    case Some(value) if value.isInstanceOf[Boolean] => Option(value.asInstanceOf[Boolean])
    case _ => None
  }

  def findValue(descriptor: NumberParameterDescriptor): Option[Long] = parameterMapping.get(descriptor.ref.id) match {
    case Some(value) if value.isInstanceOf[Long] => Option(value.asInstanceOf[Long])
    case _ => None
  }

  def findValue(descriptor: DecimalParameterDescriptor): Option[Double] = parameterMapping.get(descriptor.ref.id) match {
    case Some(value) if value.isInstanceOf[Double] => Option(value.asInstanceOf[Double])
    case _ => None
  }

  def findValue(descriptor: TextParameterDescriptor): Option[String] = parameterMapping.get(descriptor.ref.id) match {
    case Some(value) if value.isInstanceOf[String] => Option(value.asInstanceOf[String])
    case _ => None
  }

  def findValue(descriptor: ExpressionParameterDescriptor): Option[String] = parameterMapping.get(descriptor.ref.id) match {
    case Some(value) if value.isInstanceOf[String] => Option(value.asInstanceOf[String])
    case _ => None
  }

  def findValue(descriptor: FieldNameParameterDescriptor): Option[String] = parameterMapping.get(descriptor.ref.id) match {
    case Some(value) if value.isInstanceOf[String] => Option(value.asInstanceOf[String])
    case _ => None
  }

  def findValue(descriptor: FieldParameterDescriptor): Option[Field] = parameterMapping.get(descriptor.ref.id) match {
    case Some(Some(field: Field)) => Some(field)
    case _ => None
  }

  def findValue(descriptor: TextListParameterDescriptor): Option[Seq[String]] = parameterMapping.get(descriptor.ref.id) match {
    case Some(value) if value.isInstanceOf[Seq[String]] => Option(value.asInstanceOf[Seq[String]])
    case _ => None
  }

  def findValue(descriptor: FieldNameListParameterDescriptor): Option[Seq[String]] = parameterMapping.get(descriptor.ref.id) match {
    case Some(value) if value.isInstanceOf[Seq[String]] => Option(value.asInstanceOf[Seq[String]])
    case _ => None
  }

  def findValue(descriptor: FieldListParameterDescriptor): Option[Seq[Field]] = parameterMapping.get(descriptor.ref.id) match {
    case Some(value) if value.isInstanceOf[Seq[Field]] => Option(value.asInstanceOf[Seq[Field]])
    case _ => None
  }

  def findValue(descriptor: ChoiceParameterDescriptor): Option[String] = parameterMapping.get(descriptor.ref.id) match {
    case Some(value) if value.isInstanceOf[String] => Option(value.asInstanceOf[String])
    case _ => None
  }


  def findValue(descriptor: FieldDirectiveSequenceParameterDescriptor): Option[Seq[FieldDirectiveSequenceConfiguration]] = {
    case Some(value) if value.isInstanceOf[Seq[FieldDirectiveSequenceConfiguration]] => Option(value.asInstanceOf[Seq[FieldDirectiveSequenceConfiguration]])
    case _ => None
  }

  def getValueOrDefault(descriptor: BooleanParameterDescriptor, default: Boolean): Boolean = findValue(descriptor).getOrElse(default)

  def getValueOrDefault(descriptor: NumberParameterDescriptor, default: Long): Long = findValue(descriptor).getOrElse(default)

  def getValueOrDefault(descriptor: DecimalParameterDescriptor, default: Double): Double = findValue(descriptor).getOrElse(default)

  def getValueOrDefault(descriptor: TextParameterDescriptor, default: String): String = findValue(descriptor).getOrElse(default)

  def getValueOrDefault(descriptor: ExpressionParameterDescriptor, default: String): String = findValue(descriptor).getOrElse(default)

  def getValueOrDefault(descriptor: FieldNameParameterDescriptor, default: String): String = findValue(descriptor).getOrElse(default)

  def getValueOrDefault(descriptor: FieldParameterDescriptor, default: Field): Field = findValue(descriptor).getOrElse(default)

  def getValueOrDefault(descriptor: TextListParameterDescriptor, default: Seq[String]): Seq[String] = findValue(descriptor).getOrElse(default)

  def getValueOrDefault(descriptor: FieldNameListParameterDescriptor, default: Seq[String]): Seq[String] = findValue(descriptor).getOrElse(default)

  def getValueOrDefault(descriptor: FieldListParameterDescriptor, default: Seq[Field]): Seq[Field] = findValue(descriptor).getOrElse(default)

  def getValueOrDefault(descriptor: ChoiceParameterDescriptor, default: String): String = findValue(descriptor).getOrElse(default)

  def getValueOrDefault(descriptor: FieldDirectiveSequenceParameterDescriptor, default: Seq[FieldDirectiveSequenceConfiguration]): Seq[FieldDirectiveSequenceConfiguration] = {
    findValue(descriptor).getOrElse(default)
  }
}
