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
import io.logbee.keyscore.pipeline.contrib.tailin.file.DirWatcherPattern


object TailinSourceLogic extends Described {

  val filePattern = TextParameterDescriptor(
    ref = "tailin.file.pattern",
    info = ParameterInfo(
      displayName = TextRef("filePattern"),
      description = TextRef("filePatternDescription")
    ),
    validator = StringValidator(
      expression = """^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\-]*[a-zA-Z0-9])\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\-]*[A-Za-z0-9])$""",
    ),
    defaultValue = "",
    mandatory = true
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
  
  val rotationPattern = TextParameterDescriptor(
    ref = "tailin.rotation.pattern",
    info = ParameterInfo(
      displayName = TextRef("rotationPattern"),
      description = TextRef("rotationPatternDescription")
    ),
    validator = StringValidator(
      expression = """^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\-]*[a-zA-Z0-9])\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\-]*[A-Za-z0-9])$""",
    ),
    defaultValue = "",
    mandatory = false
  )
  
  
  val fieldName = FieldNameParameterDescriptor(
    ref = "tailin.fieldName",
    info = ParameterInfo(
      displayName = TextRef("fieldName.displayName"),
      description = TextRef("fieldName.description")
    ),
    defaultValue = "message",
    hint = FieldNameHint.PresentField,
    mandatory = true
  )
  
  
  
  //not exposed in UI
  val persistenceFile = TextParameterDescriptor(
    defaultValue = ".keyscoreFileTailinPersistence",
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
        readMode,
        encoding,
        rotationPattern,
        fieldName,
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
  private var readMode = ReadMode.LINE.toString
  private var encoding = StandardCharsets.UTF_8.toString
  private var rotationPattern = TailinSourceLogic.rotationPattern.defaultValue
  private var fieldName = TailinSourceLogic.fieldName.defaultValue
  
  //not exposed in UI
  private var persistenceFile = TailinSourceLogic.persistenceFile.defaultValue
  
  
  var dirWatcher: DirWatcher = _

  val sendBuffer = new SendBuffer()

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: Configuration): Unit = {
    filePattern = configuration.getValueOrDefault(TailinSourceLogic.filePattern, filePattern)
    readMode = configuration.getValueOrDefault(TailinSourceLogic.readMode, readMode)
    encoding = configuration.getValueOrDefault(TailinSourceLogic.encoding, encoding)
    rotationPattern = configuration.getValueOrDefault(TailinSourceLogic.rotationPattern, rotationPattern)
    fieldName = configuration.getValueOrDefault(TailinSourceLogic.fieldName, fieldName)
    persistenceFile = configuration.getValueOrDefault(TailinSourceLogic.persistenceFile, persistenceFile)
    
    
    
    var baseDir = DirWatcherPattern.extractInvariableDir(filePattern) //start the first DirWatcher at the deepest level where no new sibling-directories can match the filePattern in the future 
    if (baseDir == null) {
      log.warning("Could not parse the specified file pattern or could not find suitable parent directory to observe.")
      return
    }
    

    val _persistenceFile = new File(persistenceFile)
    _persistenceFile.createNewFile()
    
    for (i <- 1 to 50) {
      if (_persistenceFile.exists == false) {
        Thread.sleep(100)
      }
    }
    
    
    val persistenceContext = new FilePersistenceContext(_persistenceFile)
    val bufferSize = 1024

    val callback: String => Unit = {
      data: String =>
        sendBuffer.addToBuffer(data)
    }
    
    val rotationReaderProvider = new RotationReaderProvider(rotationPattern, persistenceContext, bufferSize, callback, Charset.forName(encoding), ReadMode.withName(readMode))
    val dirWatcherConfiguration = DirWatcherConfiguration(baseDir, DirWatcherPattern(filePattern))
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
          fieldName,
          TextValue(sendBuffer.getNextElement)
        ))
      )
    )

    log.info(s"Created Datasets: $outData")

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

