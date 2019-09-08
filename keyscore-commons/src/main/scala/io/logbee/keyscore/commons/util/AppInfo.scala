package io.logbee.keyscore.commons.util

import java.util.jar.Attributes

import io.logbee.keyscore.model.util.Using.using

import scala.jdk.CollectionConverters._
import scala.reflect.ClassTag

object AppInfo {

  def fromMainClass[T](implicit classTag: ClassTag[T]): AppInfo = {

    val mainClass = classTag.runtimeClass
    var appInfo: AppInfo = AppInfo("<unknown>", "<unknown>", "<unknown>", "<unknown>", "<unknown>", "<unknown>")

    try {
      val manifest: Option[java.util.jar.Manifest] = mainClass.getClassLoader.getResources("META-INF/MANIFEST.MF").asScala
        .map(url => using(url.openStream())(stream => new java.util.jar.Manifest(stream)))
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

  def printAppInfo[T](implicit classTag: ClassTag[T]): Unit = {
    val appInfo = fromMainClass[T]
    println(s" Name:          ${appInfo.name}")
    println(s" Version:       ${appInfo.version}")
    println(s" Revision:      ${appInfo.revision}")
    println(s" Revision-Date: ${appInfo.revisionDate}")
    println(s" Build-Date:    ${appInfo.buildDate}")
    println(s" Vendor:        ${appInfo.vendor}")
    println()
  }
}

case class AppInfo(name: String, version: String, revision: String, revisionDate: String, buildDate: String, vendor: String)