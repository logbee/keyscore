package io.logbee.keyscore.pipeline.api.util

trait ResultSet[T] {

  def elements: List[T]

  def sort(func: (T, T) => Int, order: Order = Ascending): ResultSet[T]
}
