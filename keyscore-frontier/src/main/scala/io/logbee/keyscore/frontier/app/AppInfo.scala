package io.logbee.keyscore.frontier.app

object AppInfo {
  def apply(): AppInfo = {
    val appPackage = classOf[AppInfo].getPackage
    AppInfo(appPackage.getImplementationTitle, appPackage.getImplementationVersion, appPackage.getImplementationVendor)
  }
}

case class AppInfo(name: String, verion: String, vendor: String)