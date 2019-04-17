package io.logbee.keyscore.pipeline.api.util

trait DataIndex[T] {

  /**
    Adding a single element into a specific group according to the passed function.

    @param element a List of datasets records or anything else.
    @param func
   */
  def insert(element: T, func: (T => Set[Attribute[_]])*): Unit

  /**
    * Retrieving a group which matches the given characteristics
    *
    * @param characteristic
    * @return
    */
  def select(characteristic: Characteristic,  sortedBy: (T, T) => Int): ResultSet[T]

}

sealed trait Order
object Ascending extends Order
object Descending extends Order