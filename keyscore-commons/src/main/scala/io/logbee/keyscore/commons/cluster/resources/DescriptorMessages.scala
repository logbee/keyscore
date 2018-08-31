package io.logbee.keyscore.commons.cluster.resources

import io.logbee.keyscore.model.descriptor.{Descriptor, DescriptorRef}

object DescriptorMessages {

  case class StoreDescriptorRequest(descriptor: Descriptor)

  case class DeleteDescriptorRequest(ref: DescriptorRef)

  case class GetDescriptorRequest(ref: DescriptorRef)

  case class GetDescriptorResponse(descriptor: Option[Descriptor])
}
