package io.logbee.keyscore.commons.cluster.resources

import io.logbee.keyscore.model.descriptor.{Descriptor, DescriptorRef}

object DescriptorMessages {

  case class StoreDescriptorRequest(descriptor: Descriptor)

  case object StoreDescriptorResponse

  case class DeleteDescriptorRequest(ref: DescriptorRef)

  case object DeleteDescriptorResponse

  case object DeleteAllDescriptorsRequest

  case object DeleteAllDescriptorsResponse

  case class GetDescriptorRequest(ref: DescriptorRef)

  case class GetDescriptorResponse(descriptor: Option[Descriptor])

  case object GetAllDescriptorsRequest

  case class GetAllDescriptorsResponse(descriptors: Map[DescriptorRef, Descriptor])

  case class UpdateDescriptorRequest(descriptor: Descriptor)

  case object UpdateDescriptorSuccessResponse

  case object UpdateDescriptorFailureResponse
}