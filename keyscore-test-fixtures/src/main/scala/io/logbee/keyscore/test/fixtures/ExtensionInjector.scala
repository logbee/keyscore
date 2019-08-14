package io.logbee.keyscore.test.fixtures

import java.util.concurrent.ConcurrentHashMap

import akka.actor.{ActorSystem, ExtensionId}

object ExtensionInjector {

  private val ru = scala.reflect.runtime.universe

  def injectExtension(id: ExtensionId[_], instance: AnyRef, system: ActorSystem): Unit = {
    val runtimeMirror = ru.runtimeMirror(system.getClass.getClassLoader)
    val instanceMirror = runtimeMirror.reflect(system)
    val fieldSymbol = instanceMirror.symbol.info.member(ru.TermName("extensions")).asTerm
    val fieldMirror = instanceMirror.reflectField(fieldSymbol)
    fieldMirror.get.asInstanceOf[ConcurrentHashMap[ExtensionId[_], AnyRef]].put(id, instance)
  }
}
