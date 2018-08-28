package io.logbee.keyscore.model.data

trait MetaDataCompanion {
  def apply(labels: Label*): MetaData = new MetaData(labels.toSet)
}
