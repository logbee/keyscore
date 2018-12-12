package io.logbee.keyscore.pipeline.contrib.tailin

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import akka.stream.SourceShape
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.descriptor.ExpressionType.RegEx
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.{LogicParameters, SourceLogic}
import io.logbee.keyscore.pipeline.contrib.CommonCategories
import io.logbee.keyscore.pipeline.contrib.CommonCategories.CATEGORY_LOCALIZATION
import io.logbee.keyscore.pipeline.contrib.tailin.file.{DirWatcher, DirWatcherConfiguration, ReadMode}
import io.logbee.keyscore.pipeline.contrib.tailin.file.RotationReaderProvider
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.FilePersistenceContext
import io.logbee.keyscore.pipeline.contrib.tailin.file.ReadMode._
import scala.concurrent.duration._
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.InvalidPathException


object TailinSourceLogic extends Described {

  val filePattern = TextParameterDescriptor(
    ref = "tailin.file.pattern",
    info = ParameterInfo(
      displayName = TextRef("filePattern"),
      description = TextRef("filePatternDescription")
    ),
    validator = StringValidator(
      expression = """^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\-]*[a-zA-Z0-9])\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\-]*[A-Za-z0-9])$""",
      expressionType = RegEx
    ),
    defaultValue = "",
    mandatory = false
  )
  
  val recursionDepth = NumberParameterDescriptor(
    ref = "tailin.recursion.depth",
    info = ParameterInfo(
      displayName = TextRef("recursionDepth"),
      description = TextRef("recursionDepthDescription")
    ),
    defaultValue = 0L,
    range = NumberRange(step = 1, start = 0, end = 65535),
    mandatory = false
  )
  
  val readMode = ChoiceParameterDescriptor(
    ref = "tailin.readmode",
    info = ParameterInfo(
      displayName = TextRef("readMode"),
      description = TextRef("readModeDescription")
    ),
    min = 1,
    max = 1,
    choices = Seq(
      Choice(
        name = ReadMode.LINE.toString,
        displayName = TextRef("readMode.line.displayName"),
        description = TextRef("readMode.line.description")
      ),
      Choice(
        name = ReadMode.FILE.toString,
        displayName = TextRef("readMode.file.displayName"),
        description = TextRef("readMode.file.description")
      ),
    ),
  )
  
  val encoding = ChoiceParameterDescriptor(
    ref = "tailin.encoding",
    info = ParameterInfo(
      displayName = TextRef("encoding"),
      description = TextRef("encodingDescription"),
    ),
    min = 1,
    max = 1,
    choices = Seq(
      Choice(
        name = StandardCharsets.UTF_8.toString,
        displayName = TextRef("encoding.utf_8.displayName"),
        description = TextRef("encoding.utf_8.description"),
      ),
      Choice(
        name = StandardCharsets.UTF_16.toString,
        displayName = TextRef("encoding.utf_16.displayName"),
        description = TextRef("encoding.utf_16.description"),
      ),
      Choice(
        name = Charset.forName("windows-1252").toString,
        displayName = TextRef("encoding.windows-1252.displayName"),
        description = TextRef("encoding.windows-1252.description"),
      ),
      Choice(
        name = StandardCharsets.ISO_8859_1.toString,
        displayName = TextRef("encoding.iso_8859_1.displayName"),
        description = TextRef("encoding.iso_8859_1.description"),
      ),
    ),
  )
  
  val rotationSuffix = TextParameterDescriptor(
    ref = "tailin.rotation.suffix",
    info = ParameterInfo(
      displayName = TextRef("rotationSuffix"),
      description = TextRef("rotationSuffixDescription")
    ),
    validator = StringValidator(
      expression = """^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\-]*[a-zA-Z0-9])\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\-]*[A-Za-z0-9])$""",
      expressionType = RegEx
    ),
    defaultValue = "",
    mandatory = false
  )
  

  override def describe = Descriptor(
    ref = "5a754cd3-e11d-4dfb-a484-a9f83cf3d795",
    describes = SourceDescriptor(
      name = classOf[TailinSourceLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(CommonCategories.SOURCE, Category("File")),
      parameters = Seq(
        filePattern,
        recursionDepth,
        readMode,
        encoding,
        rotationSuffix,
      ),
      icon = Icon.fromClass(classOf[TailinSourceLogic])
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.tailin.TailinSourceLogic",
      Locale.ENGLISH, Locale.GERMAN
    ) ++ CATEGORY_LOCALIZATION
  )
}


class TailinSourceLogic(parameters: LogicParameters, shape: SourceShape[Dataset]) extends SourceLogic(parameters, shape) {

  private var filePattern = TailinSourceLogic.filePattern.defaultValue
  private var recursionDepth = TailinSourceLogic.recursionDepth.defaultValue
  private var readMode = ReadMode.LINE.toString
  private var encoding = StandardCharsets.UTF_8.toString
  private var rotationSuffix = TailinSourceLogic.rotationSuffix.defaultValue
  
  var dirWatcher: DirWatcher = _

  val sendBuffer = new SendBuffer()

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: Configuration): Unit = {
    filePattern = configuration.getValueOrDefault(TailinSourceLogic.filePattern, filePattern)
    recursionDepth = configuration.getValueOrDefault(TailinSourceLogic.recursionDepth, recursionDepth)
    readMode = configuration.getValueOrDefault(TailinSourceLogic.readMode, readMode)
    encoding = configuration.getValueOrDefault(TailinSourceLogic.encoding, encoding)
    rotationSuffix = configuration.getValueOrDefault(TailinSourceLogic.rotationSuffix, rotationSuffix)
    
    
    val watchDir = extractWatchDirFromFilePattern(filePattern)
    if (watchDir == null) {
      log.warning("The path that was configured to watch doesn't exist or is not a directory.")
      return
    }
    
    if (Paths.get(filePattern).toFile.isDirectory) {
      filePattern += "*" //if the user specifies a directory, assume that the want all files in the directory
    }

    val persistenceFile: File = new File(".keyscoreFileTailinPersistence")

    persistenceFile.createNewFile()
    
    for (i <- 1 to 50) {
      if (persistenceFile.exists == false) {
        Thread.sleep(100)
      }
    }
    
    
    val persistenceContext = new FilePersistenceContext(persistenceFile)
    val bufferSize = 1024

    val callback: String => Unit = {
      data: String =>
        sendBuffer.addToBuffer(data)
    }
    
    val rotationReaderProvider = new RotationReaderProvider(rotationSuffix, persistenceContext, bufferSize, callback, Charset.forName(encoding), ReadMode.withName(readMode))
    val dirWatcherConfiguration = DirWatcherConfiguration(watchDir, filePattern, recursionDepth)
    dirWatcher = rotationReaderProvider.createDirWatcher(dirWatcherConfiguration)
  }
  
  
  private def extractWatchDirFromFilePattern(filePattern: String): Path = {
    
    val asteriskIndex = filePattern.indexOf("*")
    
    var invariableString = filePattern
    if (asteriskIndex != -1) {
      invariableString = filePattern.substring(0, asteriskIndex)
    }
    
    
    val invariablePath = Paths.get(invariableString)
    
    if (invariablePath.toFile.isDirectory) {
      invariablePath
    }
    else { //remove the last part behind the last slash, if a slash exists
      val lastSlashIndex = invariableString.lastIndexOf(File.separator)
      
      if (lastSlashIndex == -1) {
        null
      }
      else {
        val invariablePathDir = Paths.get(invariableString.substring(0, lastSlashIndex))
        if (invariablePathDir.toFile.isDirectory) {
          invariablePathDir
        }
        else {
          null
        }
      }
    }
  }
  
  
  
  override def onTimer(timerKey: Any) {
    dirWatcher.processEvents()
    
    if (!sendBuffer.isEmpty) {
      doPush()
    }
    else {
      scheduleOnce(timerKey = "poll", 1.second)
    }
  }
  
  
  private def doPush() {
    val outData = Dataset(
      records = Record(
        fields = List(Field(
          "output",
          TextValue(sendBuffer.getNextElement)
        ))
      )
    )

    log.info(s"Created Datasets $outData")

    push(out, outData)
  }
  
  
  override def onPull(): Unit = {
    
    if (!sendBuffer.isEmpty) {
      doPush()
    }
    else {
      dirWatcher.processEvents()
      
      if (!sendBuffer.isEmpty) {
        doPush()
      }
      else {
        scheduleOnce(timerKey = "poll", 1.second)
      }
    }
  }
  
  
  override def postStop(): Unit = {
    if (dirWatcher != null) {
      dirWatcher.tearDown()
    }
    log.info("Tailin source is stopping.")
  }
}

