package io.logbee.keyscore.frontier.util

import java.lang.reflect.InvocationTargetException

import akka.actor.ActorSystem
import io.logbee.keyscore.model.configuration.Configuration

import scala.reflect.runtime.{universe => ru}


object Reflection {

  def createFilterByClassname(objectName: String, config: Configuration,system:Option[ActorSystem]=None): Any = {
    val createMethodName ="create"

    val m = ru.runtimeMirror(getClass.getClassLoader)
    val module = m.staticModule(objectName)
    val im = m.reflectModule(module)
    val method = im.symbol.info.decl(ru.TermName(createMethodName)).asMethod

    val objMirror = m.reflect(im.instance)
    try {
      if(system.isEmpty) objMirror.reflectMethod(method)(config) else objMirror.reflectMethod(method)(config,system.get)
    } catch {
      case e: InvocationTargetException => throw e.getCause;
    }

  }


}
