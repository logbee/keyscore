package io.logbee.keyscore.agent.stream.management

import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.filter.FilterDescriptor

class LoadFilterDescriptorException extends RuntimeException {}

class FilterLoader {

  import scala.reflect.runtime.{universe => ru}

  private val runtimeMirror = ru.runtimeMirror(getClass.getClassLoader)

  def loadDescriptor(clazz: Class[_]): FilterDescriptor = {
    val moduleSymbol = runtimeMirror.reflectModule(runtimeMirror.staticModule(clazz.getName))

    moduleSymbol.instance match {
      case describable: Described =>
        describable.descriptor
      case _ =>
        throw new LoadFilterDescriptorException
    }
  }
}
