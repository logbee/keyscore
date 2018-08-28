package io.logbee.keyscore.model.data

trait DatasetCompanion {

  def apply(records: List[Record]): Dataset = Dataset(MetaData(), records)

  def apply(records: Record*): Dataset = Dataset(MetaData(), records.toList)

  def apply(metaData: MetaData, records: Record*): Dataset = Dataset(metaData, records.toList)
}
