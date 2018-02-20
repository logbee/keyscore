package io.logbee.keyscore.model

class Dataset(private val records: List[Record]) extends Seq[Record] {

  override def apply(idx: Int): Record = records(idx)

  override def iterator: Iterator[Record] = records.iterator

  override def length: Int = records.length
}
