package io.logbee.keyscore.agent.runtimes.jvm

import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.descriptor.Descriptor

class LoadFilterDescriptorException extends RuntimeException {}

/**
  * The '''FilterLoader''' returns a `Descriptor` for a given Class.
  *
  * @todo Renaming?
  */
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
