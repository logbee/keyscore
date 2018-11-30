package io.logbee.keyscore.pipeline.contrib.tailin.persistence

import java.io.{File, FileWriter}

import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization.write

import scala.io.Source
import scala.reflect.ClassTag
import scala.reflect.runtime.universe._


class FilePersistenceContext(persistenceFile: File) extends PersistenceContext {
  
  implicit val formats = DefaultFormats
  
  private var json: JValue = null

  
  def store(key: String, value: Any) = {
    
    if (json == null) {
      json = parse(Source.fromFile(persistenceFile).mkString)
    }
    
    json = json.merge(parse(write(key -> value)))
    
    
    writeJsonToFile(json, persistenceFile)
  }
  
  
  def load[T](key: String)(implicit tag: TypeTag[T]): Option[T] = {
    
    if (json == null) {
      json = parse(Source.fromFile(persistenceFile).mkString)
    }
    
    val value = json \ key
    
    
    
    
    //Convert the TypeTag to a ClassTag, so that both are in scope.
    //Necessary for automatic conversion to the deprecated Manifest,
    //which is still in use by json4s as of 2018-11-05.
    //Should be able to remove the following line once json4s gets updated.
    implicit val classTag = ClassTag[T](tag.mirror.runtimeClass(tag.tpe))
    
    
    
    
    value.extractOpt[T]
  }
  
  
  def remove(key: String): Unit = {
    
    if (json == null) {
      json = parse(Source.fromFile(persistenceFile).mkString)
    }
    
    json = json.removeField {
      case (string: String, _) => string == key
      case _ => false
    }
    
    writeJsonToFile(json, persistenceFile)
  }
  
  
  
  
  private def writeJsonToFile(json: JValue, file: File) {
    
    var output: FileWriter = null
    try {
      output = new FileWriter(file, true)
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

