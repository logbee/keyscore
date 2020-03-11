package io.logbee.keyscore.commons.ehcache

import java.nio.file.Files
import java.time.Duration
import java.util.UUID

import com.google.protobuf.timestamp.Timestamp
import com.typesafe.config.Config
import io.logbee.keyscore.commons.ehcache.MetricsCache.Configuration
import io.logbee.keyscore.model.metrics.{MetricConversion, MetricsCollection}
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

class MetricsCache(val configuration: Configuration) extends Cache {

  private val idToValues: mutable.HashMap[UUID, (Long, Long)] = mutable.HashMap.empty[UUID, (Long, Long)]

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
    idToValues.get(id) match {
      case Some(tuple) =>
        idToValues += (id -> (first, tuple._2))
      case None =>
    }
  }

  private def getAllFrom(id: UUID, entry: Long, earliest: Timestamp, latest: Timestamp, limit: Long, seq: Seq[MetricsCollection] = Seq.empty[MetricsCollection]): Seq[MetricsCollection] = {
    cache.get(calculateKey(id, entry)) match {
      case mc: MetricsCollection =>
        if (mc.metrics.nonEmpty) {

          val actualLatest = MetricConversion.getLatest(mc)
          val actualTimestamp = Timestamp(actualLatest.seconds, actualLatest.nanos)

          if (seq.size < limit) {
            if(newerThanLatest(actualTimestamp, latest)){
              getAllFrom(id, entry - 1, earliest, latest, limit, seq)
            } else {
              if(olderThanEarliest(actualTimestamp, earliest)){
                seq
              } else {
                getAllFrom(id, entry - 1, earliest, latest, limit, seq :+ mc)
              }
            }
          } else seq
        } else seq
      case _ =>
        updateTuple(id, entry + 1)
        seq
    }
  }

  def put(id: UUID, collection: MetricsCollection): Unit = {
    idToValues.get(id) match {
      case Some(tuple) =>
        idToValues += (id -> (tuple._1, tuple._2 + 1L))
        val key = calculateKey(id, tuple._2 + 1L)
        cache.put(key, collection)
      case None =>
        idToValues += (id -> (0L, 0L))
        val key = calculateKey(id, 0L)
        cache.put(key, collection)
    }
  }

  def getAll(id: UUID, earliest: Timestamp, latest: Timestamp, limit: Long): Seq[MetricsCollection] = {
    idToValues.get(id) match {
      case Some(tuple) =>
        getAllFrom(id, tuple._2, earliest = earliest, latest = latest, limit)
      case None => Seq.empty
    }
  }

  def getNewest(id: UUID): Option[MetricsCollection] = {
    idToValues.get(id) match {
      case Some(tuple) => cache.get(calculateKey(id, tuple._2)) match {
        case mc: MetricsCollection => Some(mc)
        case _ => None
      }
      case None => None
    }
  }

  def getOldest(id: UUID): Option[MetricsCollection] = {
    idToValues.get(id) match {
      case Some(tuple) => cache.get(calculateKey(id, tuple._1)) match {
        case mc: MetricsCollection => Some(mc)
        case _ => None
      }
      case None => None
    }
  }

}