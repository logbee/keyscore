package io.logbee.keyscore.model

object MetaData {
  def apply(): MetaData = new MetaData(Map.empty)
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
