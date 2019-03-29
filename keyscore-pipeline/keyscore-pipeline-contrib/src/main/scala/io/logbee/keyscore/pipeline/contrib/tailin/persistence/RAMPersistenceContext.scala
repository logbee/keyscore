package io.logbee.keyscore.pipeline.contrib.tailin.persistence

import scala.reflect.ClassTag
import scala.reflect.runtime.universe.TypeTag
import org.json4s.DefaultFormats
import org.json4s.JsonAST.JValue
import org.json4s.native.JsonMethods.parse
import org.json4s.native.Serialization.write

class RAMPersistenceContext extends PersistenceContext {
  
  implicit val formats = DefaultFormats
  
  private var json: JValue = null
  
  
  def store(key: String, value: Any) = {
    json = json.merge(parse(write(key -> value)))
  }
  
  
  def load[T](key: String)(implicit tag: TypeTag[T]): Option[T] = {
    
    val value = json \ key
    
    
    
    
    //Convert the TypeTag to a ClassTag, so that both are in scope.
    //Necessary for automatic conversion to the deprecated Manifest,
    //which is still in use by json4s as of 2018-11-05.
    //Should be able to remove the following line once json4s gets updated.
    implicit val classTag = ClassTag[T](tag.mirror.runtimeClass(tag.tpe))
    
    
    
    
    value.extractOpt[T]
  }
  
  
  def remove(key: String): Unit = {
    
    json = json.removeField {
      case (string: String, _) => string == key
      case _ => false
    }
  }
}
