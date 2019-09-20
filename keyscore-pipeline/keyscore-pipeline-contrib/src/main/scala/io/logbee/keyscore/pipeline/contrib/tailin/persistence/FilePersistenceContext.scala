package io.logbee.keyscore.pipeline.contrib.tailin.persistence

import java.io.{File, FileWriter}
import java.nio.file.Paths

import com.typesafe.config.Config
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.FilePersistenceContext.Configuration
import org.json4s.native.JsonMethods.parse
import org.json4s.native.Serialization.write
import org.json4s.{DefaultFormats, JValue, jvalue2extractable, jvalue2monadic, string2JsonInput}
import org.slf4j.LoggerFactory

import scala.io.Source
import scala.reflect.ClassTag
import scala.reflect.runtime.universe.TypeTag

class FilePersistenceContext private (configuration: Configuration) extends PersistenceContext {
  
  implicit val formats = DefaultFormats
  
  private var json: JValue = null
  
  private def ensureJsonIsLoaded(): Unit = {
    if (json == null && configuration.enabled) {
      json = parse(Source.fromFile(configuration.persistenceFile).mkString)
    }
  }

  def store(key: String, value: Any): Unit = {
    
    ensureJsonIsLoaded()
    
    json = json.merge(parse(write(key -> value)))
    
    writeJsonToFile(json, configuration.persistenceFile)
  }
  
  def load[T](key: String)(implicit tag: TypeTag[T]): Option[T] = {
    
    ensureJsonIsLoaded()
    
    val value = json \ key

    //Convert the TypeTag to a ClassTag, so that both are in scope.
    //Necessary for automatic conversion to the deprecated Manifest,
    //which is still in use by json4s as of 2018-11-05.
    //Should be able to remove the following line once json4s gets updated.
    implicit val classTag = ClassTag[T](tag.mirror.runtimeClass(tag.tpe))

    value.extractOpt[T]
  }
  

  def findKeysWithPrefix(keyPrefix: String): List[String] = {
    ensureJsonIsLoaded()

    val values = json.filterField {
      case (key, _) => key.startsWith(keyPrefix)
      case _ => false
    }

    values.map(_._1)
  }


  def remove(key: String): Unit = {
    
    ensureJsonIsLoaded()
    
    json = json.removeField {
      case (string: String, _) => string == key
      case _ => false
    }
    
    writeJsonToFile(json, configuration.persistenceFile)
  }

  private def writeJsonToFile(json: JValue, file: File): Unit = {
    
    if (configuration.enabled) {
      if (json == null) {
        throw new IllegalStateException("Couldn't write JSON to file, because it was not loaded.")
      }

      var output: FileWriter = null
      try {
        output = new FileWriter(file, false)
        write(json, output)

        output.flush()
      }
      finally {
        if (output != null) {
          output.close()
        }
      }
    }
  }
}

object FilePersistenceContext {
  private lazy val log = LoggerFactory.getLogger(FilePersistenceContext.getClass)
  
  object Configuration {
    def apply(config: Config, userEnabled: Boolean, persistenceFileName: String): Configuration = {
      Configuration(
        enabled = config.getBoolean("enabled") && userEnabled,
        persistenceFile = Paths.get(config.getString("persistence-dir")).resolve(persistenceFileName).toFile,
      )
    }
  }
  case class Configuration private (enabled: Boolean, persistenceFile: File)
  
  
  def apply(configuration: Configuration): FilePersistenceContext = {
    
    if (configuration.enabled) {
      configuration.persistenceFile.getParentFile.mkdirs()
      val created = configuration.persistenceFile.createNewFile()
      log.debug("{} persistence file at {}", if (created) "Created" else "Found", configuration.persistenceFile.getAbsolutePath)
    }
    
    new FilePersistenceContext(configuration)
  }
}
