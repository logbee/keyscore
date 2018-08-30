package io.logbee.keyscore.model.descriptor

object ToParameterRef {

  implicit def toRef(descriptor: ParameterDescriptor): ParameterRef = descriptor match {
    case text: TextParameterDescriptor => text.ref
  }
}
