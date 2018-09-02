package io.logbee.keyscore.frontier.app

import java.util.jar.Attributes

import io.logbee.keyscore.model.util.Using.using
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

object AppInfo {
  private val log = LoggerFactory.getLogger(classOf[AppInfo])

  def apply(mainClass: Class[_]): AppInfo = {

    var appInfo: AppInfo = AppInfo("<unkown>", "<unkown>", "<unkown>")

    try {
      val manifest: Option[java.util.jar.Manifest] = mainClass.getClassLoader.getResources("META-INF/MANIFEST.MF").asScala
        .map(url => using(url.openStream())(stream => new java.util.jar.Manifest(stream)))
        .find(manifest => Option(manifest.getMainAttributes.getValue(Attributes.Name.MAIN_CLASS)).exists(name => name.contains(mainClass.getName)))

      manifest.foreach(manifest => {
        val attributes = manifest.getMainAttributes
        val implementationTitle = attributes.getValue(Attributes.Name.IMPLEMENTATION_TITLE)
        val implementationVendor = attributes.getValue(Attributes.Name.IMPLEMENTATION_VENDOR)
        val implementationVersion = attributes.getValue(Attributes.Name.IMPLEMENTATION_VERSION)

        appInfo = AppInfo(
          if (implementationTitle == null) "<unkown>" else implementationTitle,
          if (implementationVersion == null) "<unkown>" else implementationVersion,
          if (implementationVendor == null) "<unkown>" else implementationVendor
        )
      })
    }
    catch {
      case e: Exception => log.error("Failed to obtain app information!", e)
    }

    log.info("{}", appInfo)

    appInfo
  }
}

case class AppInfo(name: String, version: String, vendor: String)