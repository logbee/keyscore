package io.logbee.keyscore.model

case class Label[T](name: String) {
  def from(metaData: MetaData): Option[T] = {
    metaData.label[T](this)
  }
}
