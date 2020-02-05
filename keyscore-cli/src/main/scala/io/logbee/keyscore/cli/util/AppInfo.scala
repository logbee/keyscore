package io.logbee.keyscore.cli.util

import java.util.jar.Attributes

import scala.jdk.CollectionConverters._
import scala.reflect.ClassTag
import scala.util.Using

object AppInfo {

  def fromMainClass[T](implicit classTag: ClassTag[T]): AppInfo = {

    val mainClass = classTag.runtimeClass
    var appInfo: AppInfo = AppInfo("<unknown>", "<unknown>", "<unknown>", "<unknown>", "<unknown>", "<unknown>")

    try {
      val manifest: Option[java.util.jar.Manifest] = mainClass.getClassLoader.getResources("META-INF/MANIFEST.MF")
        .asScala
        .map(url => Using(url.openStream())(stream => new java.util.jar.Manifest(stream)).get)
        .find(manifest => Option(manifest.getMainAttributes.getValue(Attributes.Name.MAIN_CLASS)).exists(name => name.contains(mainClass.getName)))

      manifest.foreach(manifest => {
        val attributes = manifest.getMainAttributes
        val implementationTitle = attributes.getValue(Attributes.Name.IMPLEMENTATION_TITLE)
        val implementationVendor = attributes.getValue(Attributes.Name.IMPLEMENTATION_VENDOR)
        val implementationVersion = attributes.getValue(Attributes.Name.IMPLEMENTATION_VERSION)
        val implementationRevision = attributes.getValue("Implementation-Revision")
        val implementationRevisionDate = attributes.getValue("Implementation-Revision-Date")
        val implementationBuildDate = attributes.getValue("Implementation-Build-Date")

        appInfo = AppInfo(
          name = if (implementationTitle == null) "<unknown>" else implementationTitle,
          version = if (implementationVersion == null) "<unknown>" else implementationVersion,
          revision = if (implementationRevision == null) "<unknown>" else implementationRevision,
          revisionDate = if (implementationRevisionDate == null) "<unknown>" else implementationRevisionDate,
          buildDate = if (implementationBuildDate == null) "<unknown>" else implementationBuildDate,
          vendor = if (implementationVendor == null) "<unknown>" else implementationVendor
        )
      })
    }
    catch {
      case e: Exception => throw new RuntimeException("Failed to obtain application information!", e)
    }

    appInfo
  }
}

case class AppInfo(name: String, version: String, revision: String, revisionDate: String, buildDate: String, vendor: String)