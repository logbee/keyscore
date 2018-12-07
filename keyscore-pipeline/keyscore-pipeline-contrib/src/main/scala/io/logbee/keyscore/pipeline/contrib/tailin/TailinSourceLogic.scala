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
import io.logbee.keyscore.pipeline.contrib.tailin.util.FileUtility
import io.logbee.keyscore.pipeline.contrib.tailin.file.ReadMode._
import scala.concurrent.duration._


object TailinSourceLogic extends Described {

  val directoryPath = TextParameterDescriptor(
    ref = "tailin.directory.path",
    info = ParameterInfo(
      displayName = TextRef("directory"),
      description = TextRef("directoryDescription")
    ),
    validator = StringValidator(
      expression = """^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\-]*[a-zA-Z0-9])\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\-]*[A-Za-z0-9])$""",
      expressionType = RegEx
    ),
    defaultValue = "test",
    mandatory = true
  )

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
        name = ReadMode.LINE.toString(),
        displayName = TextRef("readMode.line.displayName"),
        description = TextRef("readMode.line.description")
      ),
      Choice(
        name = ReadMode.FILE.toString(),
        displayName = TextRef("readMode.file.displayName"),
        description = TextRef("readMode.file.description")
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
        directoryPath,
        filePattern,
        recursionDepth,
        readMode,
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

  private var directoryPath = TailinSourceLogic.directoryPath.defaultValue
  private var filePattern = TailinSourceLogic.filePattern.defaultValue
  private var recursionDepth = TailinSourceLogic.recursionDepth.defaultValue
  private var readMode = ReadMode.LINE.toString
  private var rotationSuffix = TailinSourceLogic.rotationSuffix.defaultValue
  
  var dirWatcher: DirWatcher = _

  val sendBuffer = new SendBuffer()

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: Configuration): Unit = {
    directoryPath = configuration.getValueOrDefault(TailinSourceLogic.directoryPath, directoryPath)
    filePattern = configuration.getValueOrDefault(TailinSourceLogic.filePattern, filePattern)
    recursionDepth = configuration.getValueOrDefault(TailinSourceLogic.recursionDepth, recursionDepth)
    readMode = configuration.getValueOrDefault(TailinSourceLogic.readMode, readMode)
    rotationSuffix = configuration.getValueOrDefault(TailinSourceLogic.rotationSuffix, rotationSuffix)
    
    
    
    val watchDir = Paths.get(directoryPath)
    if (watchDir.toFile.isDirectory == false) {
      log.warning("The path that was configured to watch doesn't exist or is not a directory.")
      return
    }

    val persistenceFile: File = new File(".keyscoreFileTailinPersistence")

    persistenceFile.createNewFile()
    FileUtility.waitForFileToExist(persistenceFile)

    val dirWatcherConfiguration = DirWatcherConfiguration(watchDir, filePattern, recursionDepth)
    val persistenceContext = new FilePersistenceContext(persistenceFile)
    val bufferSize = 1024

    val callback: String => Unit = {
      data: String =>
        sendBuffer.addToBuffer(data)
    }

    val rotationReaderProvider = new RotationReaderProvider(rotationSuffix, persistenceContext, bufferSize, callback, StandardCharsets.UTF_8, ReadMode.withName(readMode))
    dirWatcher = rotationReaderProvider.createDirWatcher(dirWatcherConfiguration)
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
    dirWatcher.tearDown()
    log.info("Tailin source is stopping.")
  }
}

