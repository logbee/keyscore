package io.logbee.keyscore.model.descriptor

import scala.language.implicitConversions

object ToParameterRef {

  implicit def toRef(descriptor: ParameterDescriptor): ParameterRef = descriptor match {
    case d: BooleanParameterDescriptor => d.ref
    case d: TextParameterDescriptor => d.ref
    case d: ExpressionParameterDescriptor => d.ref
    case d: NumberParameterDescriptor => d.ref
    case d: DecimalParameterDescriptor => d.ref
    case d: FieldNameParameterDescriptor => d.ref
    case d: FieldParameterDescriptor => d.ref
    case d: TextListParameterDescriptor => d.ref
    case d: FieldNameListParameterDescriptor => d.ref
    case d: FieldListParameterDescriptor => d.ref
    case d: ChoiceParameterDescriptor => d.ref
    case d: ParameterGroupDescriptor => d.ref
    case _ => throw new IllegalArgumentException
  }
}
