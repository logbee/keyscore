package io.logbee.keyscore.agent.runtimes.jvm

import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.descriptor.Descriptor

class LoadFilterDescriptorException(message: String, cause: Throwable = null) extends RuntimeException(message, cause) {}

/**
  * The '''FilterLoader''' returns a `Descriptor` for a given Class.
  *
  * @todo Renaming?
  */
class FilterLoader {

  import scala.reflect.runtime.{universe => ru}

  private val runtimeMirror = ru.runtimeMirror(getClass.getClassLoader)

  def loadDescriptor(clazz: Class[_]): Descriptor = {

    try {

      val moduleSymbol = runtimeMirror.reflectModule(runtimeMirror.staticModule(clazz.getName))

      moduleSymbol.instance match {
        case describable: Described =>
          describable.describe
        case _ =>
          throw new LoadFilterDescriptorException(s"Class '${clazz.getName}' does not implement '${classOf[Described].getName}'.")
      }
    }
    catch {
      case e: Throwable => throw new LoadFilterDescriptorException(s"Failed to load Descriptor of class '${clazz.getName}'.", e)
    }
  }
}
