package io.logbee.keyscore.pipeline.contrib.tailin.persistence
import scala.collection.mutable

class RamPersistenceContext[K, V] extends PersistenceContext[K, V] {
  private val storage = mutable.HashMap.empty[K, V]
  
  override def store(key: K, value: V): Unit = {
    storage.put(key, value)
  }

  override def load(key: K): Option[V] = {
    storage.get(key)
  }

  override def remove(key: K): Unit = {
    storage.remove(key)
  }
}
