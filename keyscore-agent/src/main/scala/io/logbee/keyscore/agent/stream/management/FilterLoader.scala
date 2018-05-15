package io.logbee.keyscore.agent.stream.management

import java.util.Locale

import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.sink.FilterDescriptor

import scala.collection.mutable

class LoadFilterDescriptorException extends RuntimeException {}

class FilterLoader {

  import scala.reflect.runtime.{universe => ru}

  private val runtimeMirror = ru.runtimeMirror(getClass.getClassLoader)

  def loadDescriptors(clazz: Class[_]): mutable.Map[Locale,FilterDescriptor] = {
    val moduleSymbol = runtimeMirror.reflectModule(runtimeMirror.staticModule(clazz.getName))

    moduleSymbol.instance match {
      case describable: Described =>
        describable.descriptors
      case _ =>
        throw new LoadFilterDescriptorException
    }
  }
}
