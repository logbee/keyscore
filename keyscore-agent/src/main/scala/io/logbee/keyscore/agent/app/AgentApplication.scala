package io.logbee.keyscore.agent.app

import java.io.File
import java.util

import akka.util.Timeout
import com.google.common.io.PatternFilenameFilter
import com.typesafe.config.ConfigFactory
import org.apache.felix.framework.Felix
import org.apache.felix.framework.util.FelixConstants
import org.osgi.framework.{BundleActivator, Constants}

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * The '''AgentApplication''' is the Main Class in the keyscore-agent package. <br><br>
  * The AgentApplication loads the `Configuration` for the package and creates an [[io.logbee.keyscore.agent.Agent]].
  */
object AgentApplication extends App {

  implicit val timeout: Timeout = 5 seconds
  val config = ConfigFactory.load()

  val systemPackagesList = config.getStringList("keyscore.agent.system-packages").asScala
  val bundleLocations = config.getStringList("keyscore.agent.bundle-locations").asScala

  val frameworkConfig = new util.HashMap[String, Object]()
  val agentAppActivator = new AgentAppActivator()
  val activators = new util.ArrayList[BundleActivator]()

  frameworkConfig.put(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, activators)
  frameworkConfig.put(Constants.FRAMEWORK_STORAGE, ".felix")
  frameworkConfig.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT)
  frameworkConfig.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, systemPackagesList.mkString(","))

  val bundleURLs = bundleLocations
    .map(new File(_))
    .filter(_.exists())
    .flatMap(file => file.listFiles(new PatternFilenameFilter(".*\\.jar$")))
    .map(_.toURI.toURL.toString)

  val framework = new Felix(frameworkConfig)

  framework.start()

  val ctx = framework.getBundleContext
  bundleURLs.map(location => {
    ctx.installBundle(location)
  }).foreach( bundle => {
    bundle.start()
  })

  Seq("assemblyref:file:./assembly.json", "assemblyref:file:./contrib-assembly.json").foreach( url => {
    ctx.installBundle(url).start()
  })
}

class AgentApplication
