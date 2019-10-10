package io.logbee.keyscore.pipeline.contrib.tailin.persistence


trait PersistenceContext[K, V] {

  /**
   * @param key The key to store the given value under, with which it can later be retrieved through PersistenceContext.load().
   * @param value The value to store.
   */
  def store(key: K, value: V): Unit

  /**
   * @param key The key to search for in the persisted data.
   * @return None, if the key was not found. Some(T), if the key was found.
   */
  def load(key: K): Option[V]

  /**
   * Removes the entry with the given key from the persisted data.
   * 
   * @param key The key to remove from the persisted data.
   */
  def remove(key: K): Unit
}
