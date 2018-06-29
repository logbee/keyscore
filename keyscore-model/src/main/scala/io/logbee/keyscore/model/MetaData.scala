package io.logbee.keyscore.model

import io.logbee.keyscore.model.NativeModel.NativeMetaData

object MetaData {

  def apply(): MetaData = new MetaData(Map.empty)

  implicit def metadataToNative(metaData: MetaData): NativeMetaData = {
    val builder = NativeMetaData.newBuilder
    builder.build()
  }

  implicit def metadataFromNative(native: NativeMetaData): MetaData = {
    MetaData()
  }
}

case class MetaData(labels: Map[Label[_], Any]) {

  def label[T](label: Label[T]): Option[T] = {
    labels
      .find(_.isInstanceOf[T])
      .map(_.asInstanceOf[T])
  }

  def label[T](label: Label[T], value: T): MetaData = {
    MetaData(labels + (label -> value))
  }

  def hasLabel(label: Label[_]): Boolean = {
    labels.contains(label)
  }
}
