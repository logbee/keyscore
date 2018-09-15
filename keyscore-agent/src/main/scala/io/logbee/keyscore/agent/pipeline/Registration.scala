package io.logbee.keyscore.agent.pipeline

import io.logbee.keyscore.model.descriptor.Descriptor

case class Registration(descriptor: Descriptor, logicClass: Class[_])
