package io.logbee.keyscore.agent.extension

import akka.Done
import akka.actor.{Actor, ActorLogging}
import com.typesafe.config.Config
import io.logbee.keyscore.agent.extension.ExtensionLoader.{LoadExtensions, RegisterExtension}
import io.logbee.keyscore.agent.extension.ExtensionType.fromString

import scala.collection.JavaConverters._
import scala.reflect.internal.util.ScalaClassLoader
import scala.util.Try


object ExtensionLoader {
  case class LoadExtensions(config: Config, path: String)
  case class RegisterExtension(extensionType: ExtensionType, extensionClass: Option[Class[_]], configuration: Option[Config] = None)
}

class ExtensionLoader extends Actor with ActorLogging {

  private val eventBus = context.system.eventStream

  override def preStart(): Unit = {
    log.info("StartUp complete.")
  }

  override def receive: Receive = {

    case LoadExtensions(config, path) =>
      config.getConfigList(path).asScala.foreach(config => {
        val extensionConfiguration = Try(config.getConfig("configuration")).toOption
        fromString(Try(config.getString("type")).toOption) match {
          case Some(`ExternalFilterExtension`) =>
            publish(RegisterExtension(ExternalFilterExtension, None, extensionConfiguration))
          case Some(extensionType) =>
            val extensionClassName = config.getString("class")
            ScalaClassLoader(getClass.getClassLoader).tryToLoadClass(extensionClassName) match {
              case extensionClass @ Some(_) =>
                publish(RegisterExtension(extensionType, extensionClass, extensionConfiguration))
              case _ =>
                log.error(s"Could not load extension class: $extensionClassName")
            }
          case _ =>
            log.error(s"Unknown/none extension type.")
        }
      })

      sender ! Done
  }

  private def publish(extension: RegisterExtension): Unit = {
    eventBus.publish(extension)
    log.info(s"Published: $extension")
  }
}
