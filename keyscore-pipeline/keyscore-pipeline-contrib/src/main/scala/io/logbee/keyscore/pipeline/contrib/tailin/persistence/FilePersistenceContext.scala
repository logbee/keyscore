package io.logbee.keyscore.pipeline.contrib.tailin.persistence

import java.io.{File, FileWriter}
import java.nio.file.Paths

import com.typesafe.config.Config
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.FilePersistenceContext.{Configuration, PersistenceFormat}
import io.logbee.keyscore.pipeline.contrib.tailin.read.FileReadRecord
import org.json4s.DefaultFormats
import org.json4s.native.Serialization._
import org.slf4j.LoggerFactory

import scala.io.Source

class FilePersistenceContext private(configuration: Configuration) extends PersistenceContext[String, FileReadRecord] {
  private lazy val log = LoggerFactory.getLogger(classOf[FilePersistenceContext])
  private implicit val formats = DefaultFormats


  private val ramPersistenceContext = new RamPersistenceContext[String, PersistenceFormat]

  //load all previously persisted records into RAM
  if (configuration.enabled) {
    configuration.persistenceDir.listFiles.foreach { file =>

      try {
        val loaded = read[PersistenceFormat](Source.fromFile(file).mkString)
        ramPersistenceContext.store(uniqueId(loaded.path), PersistenceFormat(file.getAbsolutePath, loaded.fileReadRecord))
      }
      catch {
        case exception: Throwable =>
          val isEmpty = file.length == 0;

          val delete_message =
            if (isEmpty)
              " (Deleting file, because it is empty.)"
            else
              ""

          log.error(s"Failed to read persistence from file '${file.getAbsolutePath}'.$delete_message", exception)

          if (isEmpty) {
            file.delete()
          }
      }
    }
  }

  override def store(absolutePath: String, fileReadRecord: FileReadRecord): Unit = {

    val uniqueIdFromPath = uniqueId(absolutePath)

    ramPersistenceContext.store(uniqueIdFromPath, PersistenceFormat(absolutePath, fileReadRecord))

    if (configuration.enabled) {
      val persistenceFile = configuration.persistenceDir.toPath.resolve(uniqueIdFromPath + ".json").toFile
      val jsonFormat = PersistenceFormat(absolutePath, fileReadRecord)
      var output: FileWriter = null

      persistenceFile.createNewFile()

      try {
        output = new FileWriter(persistenceFile, false)
        write(jsonFormat, output).flush
      }
      catch {
        case ex: Exception =>
          log.warn(s"Failed to write persistence entry to file (persisting only in RAM for now): $jsonFormat", ex)
      }
      finally {
        if (output != null) output.close()
      }
    }
  }

  override def load(key: String): Option[FileReadRecord] = {
    ramPersistenceContext.load(uniqueId(key)).map(_.fileReadRecord)
  }

  override def remove(key: String): Unit = {
    ramPersistenceContext.remove(uniqueId(key))

    if (configuration.enabled) {
      val persistenceFile = configuration.persistenceDir.toPath.resolve(uniqueId(key)).toFile
      persistenceFile.delete()
    }
  }

  import io.logbee.keyscore.model.util.Hashing._
  import io.logbee.keyscore.model.util.Hex.toHexable

  private def uniqueId(value: String): String = value.md5.toHex
}

object FilePersistenceContext {
  private lazy val log = LoggerFactory.getLogger(FilePersistenceContext.getClass)
  
  object Configuration {
    def apply(config: Config, userEnabled: Boolean, persistenceDirName: String): Configuration = {
      Configuration(
        enabled = config.getBoolean("enabled") && userEnabled,
        persistenceDir = Paths.get(config.getString("persistence-dir")).resolve(persistenceDirName).toFile,
      )
    }
  }
  case class Configuration private (enabled: Boolean, persistenceDir: File)
  
  
  def apply[K, V](configuration: Configuration): FilePersistenceContext = {
    
    if (configuration.enabled) {
      val created = configuration.persistenceDir.mkdirs()
      log.debug("{} persistence dir at {}", if (created) "Created" else "Found", configuration.persistenceDir.getAbsolutePath)
    }
    
    new FilePersistenceContext(configuration)
  }

  case class PersistenceFormat(path: String, fileReadRecord: FileReadRecord)
}
