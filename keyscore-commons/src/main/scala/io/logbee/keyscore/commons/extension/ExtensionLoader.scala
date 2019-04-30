package io.logbee.keyscore.commons.extension

import akka.Done
import akka.actor.{Actor, ActorLogging}
import com.typesafe.config.Config
import io.logbee.keyscore.commons.extension.ExtensionLoader.{LoadExtensions, RegisterExtension}
import io.logbee.keyscore.commons.extension.ExtensionType.fromString

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}


object ExtensionLoader {
  case class LoadExtensions(config: Config, path: String)
  case class RegisterExtension(extensionType: ExtensionType, extensionClass: Class[_])
}

class ExtensionLoader extends Actor with ActorLogging {

  private val eventBus = context.system.eventStream

  override def preStart(): Unit = {
    log.info("StartUp complete.")
  }

  override def receive: Receive = {

    case LoadExtensions(config, path) =>
      config.getConfigList(path).asScala.foreach(config => {
        val extensionTypeName = config.getString("type")
        val extensionClassName = config.getString("class")
        Try(getClass.getClassLoader.loadClass(extensionClassName)) match {
          case Success(extensionClass) =>
            fromString(extensionTypeName) match {
              case Some(extensionType) =>
                val registerExtension = RegisterExtension(extensionType, extensionClass)
                eventBus.publish(registerExtension)
                log.info(s"Published notification about the $extensionType: $extensionClassName")
              case None =>
                log.error(s"Unknown extension type: $extensionTypeName")
            }
          case Failure(exception) =>
            log.error(exception, s"Could not load extension class: $extensionClassName")
        }
      })

      sender ! Done
  }
}
