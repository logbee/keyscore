package io.logbee.keyscore.model

object Dataset {

  def apply(metaData: MetaData, records: List[Record]): Dataset = new Dataset(metaData, records)

  def apply(records: List[Record]): Dataset = new Dataset(MetaData(), records)

  def apply(records: Record*): Dataset = new Dataset(MetaData(), records.toList)
}

class Dataset(val metaData: MetaData, val records: List[Record]) {

  def label[T](label: Label[T]): Option[T] = {
    metaData.label[T](label)
  }

  def label[T](label: Label[T], value: T): Dataset = {
    Dataset(metaData.label(label, value), records)
  }

  def labelOnce[T](label: Label[T], value: T): Dataset = {
    if (metaData.labels.contains(label)) {
      this
    }
    else {
      this.label(label, value)
    }
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[Dataset]

  override def equals(other: Any): Boolean = other match {
    case that: Dataset =>
      (that canEqual this) &&
        records == that.records
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(records)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }

  override def toString = s"Dataset($metaData, $records)"
}
