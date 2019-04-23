package io.logbee.keyscore.commons.ehcache

import java.io.File
import java.time.Duration
import java.util.UUID

import io.logbee.keyscore.model.metrics.MetricsCollection
import org.ehcache.config.builders.CacheManagerBuilder.newCacheManagerBuilder
import org.ehcache.config.builders._
import org.ehcache.config.units.MemoryUnit
import org.ehcache.{Cache, PersistentCacheManager}

import scala.collection.mutable

object MetricsCacheManager {
  def apply(heapEntries: Long = 10L, diskSize: Long = 10L, expiration: Duration = Duration.ofSeconds(60)): MetricsCacheManager = new MetricsCacheManager(heapEntries, diskSize, expiration)
}

class MetricsCacheManager(val heapEntries: Long, val diskSize: Long, val expiration: Duration) {
  private val MetricsCache = "metrics_cache"

  private val idToMetrics: mutable.HashMap[UUID, (Long, Long)] = mutable.HashMap.empty[UUID, (Long, Long)]

  private val resourcePool = ResourcePoolsBuilder
    .heap(heapEntries)
    .disk(diskSize, MemoryUnit.MB)

  private val config: CacheConfigurationBuilder[String, MetricsCollection] = CacheConfigurationBuilder.newCacheConfigurationBuilder(classOf[String], classOf[MetricsCollection], resourcePool).withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(expiration))

  private val persistence: CacheManagerConfiguration[PersistentCacheManager] = CacheManagerBuilder.persistence(new File("./metrics"))
  private val cacheManager: PersistentCacheManager = newCacheManagerBuilder().`with`(persistence).withCache(MetricsCache, config).build(true)
  private val cache: Cache[String, MetricsCollection] = cacheManager.getCache(MetricsCache, classOf[String], classOf[MetricsCollection])


  def get: PersistentCacheManager = {
    cacheManager
  }

  def close(): Unit = {
    cacheManager.close()
  }

  def destroy(): Unit = {
    cacheManager.destroy()
  }

  def clearCache(): Unit = {
    cache.clear()
  }

  private def updateTuple(id: UUID, first: Long): Unit = {
    idToMetrics.get(id) match {
      case Some(tuple) =>
        idToMetrics += (id -> (first, tuple._2))
      case None =>
    }
  }

  private def getCounter(s: String): Long = {
    s.substring(s.lastIndexOf("|") + 1).toLong
  }

  private def calculateKey(id: UUID, counter: Long): String = {
    id.toString + "|" + counter
  }

  private def getAllFrom(id: UUID, entry: Long, seq: Seq[MetricsCollection] = Seq.empty[MetricsCollection]): Seq[MetricsCollection] = {
    cache.get(calculateKey(id, entry)) match {
      case mc: MetricsCollection =>
        getAllFrom(id, entry - 1, mc +: seq)
      case _ =>
        updateTuple(id, entry + 1)
        seq
    }
  }

  def putEntry(id: UUID, collection: MetricsCollection): Unit = {
    idToMetrics.get(id) match {
      case Some(tuple) =>
        idToMetrics += (id -> (tuple._1, tuple._2 + 1L))
        val key = calculateKey(id, tuple._2 +1L)
        cache.put(key, collection)
      case None =>
        idToMetrics += (id -> (0L, 0L))
        val key = calculateKey(id, 0L)
        cache.put(key, collection)
    }
  }

  def getAllEntries(id: UUID): Option[Seq[MetricsCollection]] = {
    idToMetrics.get(id) match {
      case Some(tuple) =>
        Some(getAllFrom(id, tuple._2))
      case None => None
    }
  }

  def getNewestEntry(id: UUID): Option[MetricsCollection] = {
    idToMetrics.get(id) match {
      case Some(tuple) => cache.get(calculateKey(id, tuple._2)) match {
        case mc: MetricsCollection => Some(mc)
        case _ => None
      }
      case None => None
    }
  }

  def getOldestEntry(id: UUID): Option[MetricsCollection] = {
    idToMetrics.get(id) match {
      case Some(tuple) => cache.get(calculateKey(id, tuple._1)) match {
        case mc: MetricsCollection => Some(mc)
        case _ => None
      }
      case None => None
    }
  }

}
