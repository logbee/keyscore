package io.logbee.keyscore.pipeline.contrib.tailin.persistence

import java.io.{File, FileWriter}
import java.nio.file.Paths

import com.typesafe.config.Config
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.FilePersistenceContext.{Configuration, PersistenceFormat}
import io.logbee.keyscore.pipeline.contrib.tailin.read.FileReader.FileReadRecord
import org.json4s.DefaultFormats
import org.json4s.native.Serialization._
import org.slf4j.LoggerFactory

import scala.io.Source

class FilePersistenceContext private(configuration: Configuration) extends PersistenceContext[String, FileReadRecord] {
  private lazy val log = LoggerFactory.getLogger(classOf[FilePersistenceContext])
  private implicit val formats = DefaultFormats


  private val ramPersistenceContext = new RamPersistenceContext[Int, PersistenceFormat]

  //load all previously persisted records into RAM
  if (configuration.enabled) {
    configuration.persistenceDir.listFiles.foreach { file =>

      val loaded = read[PersistenceFormat](Source.fromFile(file).mkString)

      ramPersistenceContext.store(loaded.path.hashCode, PersistenceFormat(file.getAbsolutePath, loaded.fileReadRecord))
    }
  }

  override def store(absolutePath: String, fileReadRecord: FileReadRecord): Unit = {

    val hashedKey = absolutePath.hashCode

    ramPersistenceContext.store(hashedKey, PersistenceFormat(absolutePath, fileReadRecord))

    if (configuration.enabled) {
      val persistenceFile = configuration.persistenceDir.toPath.resolve(hashedKey.toString + ".json").toFile
      persistenceFile.createNewFile()

      val jsonFormat = PersistenceFormat(absolutePath, fileReadRecord)

      var output: FileWriter = null
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
    ramPersistenceContext.load(key.hashCode).map(_.fileReadRecord)
  }

  override def remove(key: String): Unit = {
    ramPersistenceContext.remove(key.hashCode)

    if (configuration.enabled) {
      val persistenceFile = configuration.persistenceDir.toPath.resolve(key.hashCode.toString).toFile
      persistenceFile.delete()
    }
  }
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
