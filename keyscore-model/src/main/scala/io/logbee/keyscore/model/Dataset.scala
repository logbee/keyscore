package io.logbee.keyscore.model

object Dataset {

  def apply(records: List[Record]): Dataset = new Dataset(records)

  def apply(records: Record*): Dataset = new Dataset(records.toList)
}

class Dataset(val records: List[Record]) extends Seq[Record] {

  override def apply(idx: Int): Record = records(idx)

  override def iterator: Iterator[Record] = records.iterator

  override def length: Int = records.length
}
