package io.logbee.keyscore.frontier.app

object AppInfo {
  def apply(): AppInfo = {
    try {
      val appPackage = classOf[AppInfo].getPackage
      AppInfo(appPackage.getImplementationTitle, appPackage.getImplementationVersion, appPackage.getImplementationVendor)
    }

    AppInfo("<unkown>", "<unkown>", "<unkown>")
  }
}

case class AppInfo(name: String, verion: String, vendor: String)