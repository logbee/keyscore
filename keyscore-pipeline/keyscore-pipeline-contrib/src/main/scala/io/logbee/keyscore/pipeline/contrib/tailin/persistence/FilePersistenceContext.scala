package io.logbee.keyscore.pipeline.contrib.tailin.persistence

import java.io.File
import java.io.FileWriter

import scala.io.Source
import scala.reflect.ClassTag
import scala.reflect.runtime.universe.TypeTag

import org.json4s.DefaultFormats
import org.json4s.JValue
import org.json4s.jvalue2extractable
import org.json4s.jvalue2monadic
import org.json4s.native.JsonMethods.parse
import org.json4s.native.Serialization.write
import org.json4s.string2JsonInput


class FilePersistenceContext(persistenceFile: File) extends PersistenceContext {
  
  implicit val formats = DefaultFormats
  
  private var json: JValue = null
  
  private def ensureJsonIsLoaded(): Unit = {
    if (json == null) {
      json = parse(Source.fromFile(persistenceFile).mkString)
    }
  }

  
  def store(key: String, value: Any): Unit = {
    
    ensureJsonIsLoaded()
    
    json = json.merge(parse(write(key -> value)))
    
    writeJsonToFile(json, persistenceFile)
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
  
  
  def remove(key: String): Unit = {
    
    ensureJsonIsLoaded()
    
    json = json.removeField {
      case (string: String, _) => string == key
      case _ => false
    }
    
    writeJsonToFile(json, persistenceFile)
  }
  
  
  
  
  private def writeJsonToFile(json: JValue, file: File): Unit = {
    
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

