package io.logbee.keyscore.contrib.tailin.persistence

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

trait PersistenceContext {
  
  /**
   * @param key The key to store the given value under, with which it can later be retrieved through PersistenceContext.load().
   * @param value The value to store.
   */
  def store(key: String, value: Any): Unit
  
  
  /**
   * @param key The key to search for in the persisted data.
   * @param tag Necessary to provide runtime information about the type. Pass "typeTag[T]" to this (with T replaced by the correct type). 
   * @return None, if the key was not found. Some(T), if the key was found.
   */
  def load[T](key: String)(implicit tag: TypeTag[T]): Option[T]
  
  
  
  /**
   * Removes the entry with the given key from the persisted data.
   * 
   * @param key The key to remove from the persisted data.
   */
  def remove(key: String): Unit
}
