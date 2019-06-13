package io.logbee.keyscore.commons.ehcache

import java.nio.file.Files
import java.time.Duration
import java.util.UUID

import com.google.protobuf.timestamp.Timestamp
import com.typesafe.config.Config
import io.logbee.keyscore.commons.ehcache.MetricsCache.Configuration
import io.logbee.keyscore.model.metrics.MetricsCollection
import org.ehcache.PersistentUserManagedCache
import org.ehcache.config.builders._
import org.ehcache.config.units.MemoryUnit
import org.ehcache.impl.config.persistence.{DefaultPersistenceConfiguration, UserManagedPersistenceContext}
import org.ehcache.impl.persistence.DefaultLocalPersistenceService

import scala.collection.mutable

object MetricsCache {

  def apply(configuration: Configuration): MetricsCache = new MetricsCache(configuration)

  object Configuration {
    val root = "keyscore.metrics.cache"

    def apply(config: Config): Configuration = {

      val resolvedConfig = config.getConfig(s"$root")

      new Configuration(
        heapEntries = resolvedConfig.getLong("heap-entries"),
        diskSize = resolvedConfig.getMemorySize("disk-size").toBytes,
        expiration = resolvedConfig.getDuration("expiration"),
      )
    }
  }

  case class Configuration(
                            heapEntries: Long,
                            diskSize: Long,
                            expiration: Duration
                          )

}

class MetricsCache(val configuration: Configuration) {

  private val idToMetrics: mutable.HashMap[UUID, (Long, Long)] = mutable.HashMap.empty[UUID, (Long, Long)]

  private val resourcePool = ResourcePoolsBuilder
    .heap(configuration.heapEntries)
    .disk(configuration.diskSize, MemoryUnit.B)

  private val configExpiry = ExpiryPolicyBuilder.timeToLiveExpiration(configuration.expiration)
  private val persistenceConfiguration = new DefaultPersistenceConfiguration(Files.createTempDirectory("keyscore.metrics-cache-").toFile)
  private val persistenceService = new DefaultLocalPersistenceService(persistenceConfiguration)
  private val persistenceContext = new UserManagedPersistenceContext[String, MetricsCollection]("keyscore.metrics-cache", persistenceService)

  private val cache: PersistentUserManagedCache[String, MetricsCollection] = UserManagedCacheBuilder.newUserManagedCacheBuilder(classOf[String], classOf[MetricsCollection])
    .`with`(persistenceContext)
    .withExpiry(configExpiry)
    .withResourcePools(resourcePool)
    .build(true)

  def close(): Unit = {
    cache.close()
    cache.destroy()
  }

  def clear(): Unit = {
    cache.clear()
  }

  private def updateTuple(id: UUID, first: Long): Unit = {
    idToMetrics.get(id) match {
      case Some(tuple) =>
        idToMetrics += (id -> (first, tuple._2))
      case None =>
    }
  }

  private def calculateKey(id: UUID, counter: Long): String = {
    id.toString + "|" + counter
  }

  private def getAllFrom(id: UUID, entry: Long, earliest: Timestamp, latest: Timestamp, limit: Long, seq: Seq[MetricsCollection] = Seq.empty[MetricsCollection]): Seq[MetricsCollection] = {
    cache.get(calculateKey(id, entry)) match {
      case mc: MetricsCollection =>
        if (mc.metrics.nonEmpty) {

          //TODO find a better solution as head ~> MetricConversion
          val timestamp = Timestamp(mc.metrics.head.asMessage.timestamp.seconds, mc.metrics.head.asMessage.timestamp.nanos)

          if (seq.size < limit) {
            if (isLatest(timestamp, latest)) {
              if (isEarliest(timestamp, earliest)) {
                getAllFrom(id, entry - 1, earliest, latest, limit, mc +: seq)
              } else seq
            } else {
              getAllFrom(id, entry - 1, earliest, latest, limit, seq)
            }
          } else seq
        } else seq
      case _ =>
        updateTuple(id, entry + 1)
        seq
    }
  }

  def put(id: UUID, collection: MetricsCollection): Unit = {
    idToMetrics.get(id) match {
      case Some(tuple) =>
        idToMetrics += (id -> (tuple._1, tuple._2 + 1L))
        val key = calculateKey(id, tuple._2 + 1L)
        cache.put(key, collection)
      case None =>
        idToMetrics += (id -> (0L, 0L))
        val key = calculateKey(id, 0L)
        cache.put(key, collection)
    }
  }

  def getAll(id: UUID, earliest: Timestamp, latest: Timestamp, limit: Long): Seq[MetricsCollection] = {
    idToMetrics.get(id) match {
      case Some(tuple) =>
        getAllFrom(id, tuple._2, earliest = earliest, latest = latest, limit)
      case None => Seq.empty
    }
  }

  def getNewest(id: UUID): Option[MetricsCollection] = {
    idToMetrics.get(id) match {
      case Some(tuple) => cache.get(calculateKey(id, tuple._2)) match {
        case mc: MetricsCollection => Some(mc)
        case _ => None
      }
      case None => None
    }
  }

  def getOldest(id: UUID): Option[MetricsCollection] = {
    idToMetrics.get(id) match {
      case Some(tuple) => cache.get(calculateKey(id, tuple._1)) match {
        case mc: MetricsCollection => Some(mc)
        case _ => None
      }
      case None => None
    }
  }

  private def isLatest(actual: Timestamp, latest: Timestamp): Boolean = {
    if (actual.seconds < latest.seconds) true
    else if (actual.seconds == latest.seconds) {
      if (actual.nanos <= latest.nanos) true
      else false
    }
    else false
  }

  private def isEarliest(actual: Timestamp, earliest: Timestamp): Boolean = {
    if(actual.seconds > earliest.seconds) true
    else if (actual.seconds == earliest.seconds) {
      if (actual.nanos >= earliest.nanos) true
      else false
    }
    else false
  }

}
