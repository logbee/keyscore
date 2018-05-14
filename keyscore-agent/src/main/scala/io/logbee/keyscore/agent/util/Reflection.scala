package io.logbee.keyscore.agent.util

import java.lang.reflect.InvocationTargetException

import akka.actor.ActorSystem
import io.logbee.keyscore.model.filter.FilterConfiguration

import scala.reflect.runtime.{universe => ru}

object Reflection {

  def createFilterByClassname(objectName: String, config: FilterConfiguration, system: Option[ActorSystem] = None): Any = {
    val createMethodName = "create"

    val m = ru.runtimeMirror(getClass.getClassLoader)
    val module = m.staticModule(objectName)
    val im = m.reflectModule(module)
    val method = im.symbol.info.decl(ru.TermName(createMethodName)).asMethod

    val objMirror = m.reflect(im.instance)
    try {
      if (system.isEmpty) objMirror.reflectMethod(method)(config) else objMirror.reflectMethod(method)(config, system.get)
    } catch {
      case e: InvocationTargetException => throw e.getCause;
    }

  }


}