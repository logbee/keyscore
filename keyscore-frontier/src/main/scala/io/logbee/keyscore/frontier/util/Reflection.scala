package io.logbee.keyscore.frontier.util

import io.logbee.keyscore.model.filter.FilterConfiguration

import scala.reflect.runtime.{universe => ru}


object Reflection {

  def createFilterByClassname(objectName: String, methodName: String, config: FilterConfiguration): Any = {
    val m = ru.runtimeMirror(getClass.getClassLoader)
    val module = m.staticModule(objectName)
    val im = m.reflectModule(module)
    val method = im.symbol.info.decl(ru.TermName(methodName)).asMethod

    val objMirror = m.reflect(im.instance)
    return objMirror.reflectMethod(method)(config)
  }


}
