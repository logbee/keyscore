package io.logbee.keyscore.model.descriptor

import java.util.UUID

import scalapb.TypeMapper

trait DescriptorRefCompanion {

  implicit def stringToDescriptorRef(uuid: String): DescriptorRef = DescriptorRef(uuid)

  implicit def uuidToDescriptorRef(uuid: UUID): DescriptorRef = DescriptorRef(uuid.toString)

  implicit val typeMapper = TypeMapper[String, DescriptorRef](DescriptorRef.apply)(_.uuid)
}
