package io.logbee.keyscore.model

trait MetaDataCompanion {
  def apply(labels: Label*): MetaData = new MetaData(labels.toSet)
}
