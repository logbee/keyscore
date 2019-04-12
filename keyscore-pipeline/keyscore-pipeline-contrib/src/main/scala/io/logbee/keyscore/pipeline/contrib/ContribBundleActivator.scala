package io.logbee.keyscore.pipeline.contrib

import org.osgi.framework.{BundleActivator, BundleContext}

class ContribBundleActivator extends BundleActivator {

  override def start(ctx: BundleContext): Unit = {
    println("ContribBundleActivator.start")
  }

  override def stop(context: BundleContext): Unit = {
    println("ContribBundleActivator.stop")
  }
}