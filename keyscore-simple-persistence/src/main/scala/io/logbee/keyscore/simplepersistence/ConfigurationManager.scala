package io.logbee.keyscore.simplepersistence

import io.logbee.keyscore.model.json4s.KeyscoreFormats
import org.json4s.JsonAST.{JNothing, JValue}
import org.slf4j.LoggerFactory



object ConfigurationManager {
  def apply(): ConfigurationManager = new ConfigurationManager()
}

class ConfigurationManager {
  implicit val formats = KeyscoreFormats.formats
  import org.json4s.native.JsonMethods.parse
  import org.json4s.native.Serialization.{write, writePretty}



  val logger = LoggerFactory.getLogger(classOf[ConfigurationManager])
  private var configuration : JValue = JNothing

  implicit class JValueExtended(value: JValue) {
    def isEmpty() : Boolean = {
      return value.children.isEmpty
    }
    def has(childString: String): Boolean = {
      if ((value \ childString) != JNothing) {
        true
      } else {
        false
      }
    }
  }

  def configNeedsUpdate(configToCheck: String) : String = {

    val json = parse(configToCheck)

    logger.debug("parsed json " + writePretty(json))

    if(json.isEmpty()){
      logger.debug("Configuration needs update")
      logger.debug("Available json " + writePretty(configuration))
      return write(configuration)
    }

    logger.debug("Configuration does not need an update")

    configuration = json;
    return ""
    //val Diff(changed, added, deleted) = json.diff(configuration)
  }
}
