package io.logbee.keyscore.model

import scala.language.implicitConversions
import scala.reflect.ClassTag

trait EntityCompanion {
  implicit def SeqOfComponents2MapOfComponent(components: Seq[Component]): Map[String, Component] =
    Map(components.map(component => (component.getClass.getName, component)):_*)
}

trait BaseEntity {
  this: Entity =>

  def get[T <: Component](implicit classTag: ClassTag[T]): T = {
    val className = classTag.runtimeClass.getName
    find[T] match {
      case Some(component) => component.asInstanceOf[T]
      case None => throw new NoSuchElementException(s"$className")
    }
  }

  def find[T <: Component](implicit classTag: ClassTag[T]): Option[T] = {
    val className = classTag.runtimeClass.getName
    components.find(kv => kv._1.equals(className)) match {
      case Some((_, component)) => Some(component.asInstanceOf[T])
      case None => None
    }
  }

  def matches(aspect: Aspect): Boolean = {
    aspect.intersectionSet subsetOf components.keySet
  }
}
