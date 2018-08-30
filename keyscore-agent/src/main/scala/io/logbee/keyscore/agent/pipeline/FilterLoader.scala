package io.logbee.keyscore.agent.pipeline

import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.descriptor.Descriptor

class LoadFilterDescriptorException extends RuntimeException {}

class FilterLoader {

  import scala.reflect.runtime.{universe => ru}

  private val runtimeMirror = ru.runtimeMirror(getClass.getClassLoader)

  def loadDescriptors(clazz: Class[_]): Descriptor = {
    val moduleSymbol = runtimeMirror.reflectModule(runtimeMirror.staticModule(clazz.getName))

    moduleSymbol.instance match {
      case describable: Described =>
        describable.describe
      case _ =>
        throw new LoadFilterDescriptorException
    }
  }
}
