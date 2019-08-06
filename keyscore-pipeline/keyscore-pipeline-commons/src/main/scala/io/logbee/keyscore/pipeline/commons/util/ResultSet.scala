package io.logbee.keyscore.pipeline.commons.util

trait ResultSet[T] {

  def elements: List[T]

  def sort(func: (T, T) => Int, order: Order = Ascending): ResultSet[T]
}
