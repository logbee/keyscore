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
      )
    )
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
        rotationSuffix,
        readMode,
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

  private var directoryPath = ""
  private var filePattern = ""
  private var rotationSuffix = ""
  private var readMode: ReadMode = ReadMode.FILE

  var dirWatcher: DirWatcher = _

  val sendBuffer = new SendBuffer()

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: Configuration): Unit = {
    directoryPath = configuration.getValueOrDefault(TailinSourceLogic.directoryPath, directoryPath)
    filePattern = configuration.getValueOrDefault(TailinSourceLogic.filePattern, filePattern)
    rotationSuffix = configuration.getValueOrDefault(TailinSourceLogic.rotationSuffix, rotationSuffix)
    readMode = ReadMode.withName(configuration.getValueOrDefault(TailinSourceLogic.readMode, readMode.toString))
    
    
    
    val watchDir = Paths.get(directoryPath)
    if (watchDir.toFile.isDirectory == false) {
      log.warning("The path that was configured to watch doesn't exist or is not a directory.")
      return
    }

    val persistenceFile: File = new File(".keyscoreFileTailinPersistence")

    persistenceFile.createNewFile()
    FileUtility.waitForFileToExist(persistenceFile)

    val dirWatcherConfiguration = DirWatcherConfiguration(watchDir, filePattern)
    val persistenceContext = new FilePersistenceContext(persistenceFile)
    val bufferSize = 1024

    val callback: String => Unit = {
      data: String =>
        sendBuffer.addToBuffer(data)
    }

    val rotationReaderProvider = new RotationReaderProvider(rotationSuffix, persistenceContext, bufferSize, callback, StandardCharsets.UTF_8, readMode)
    dirWatcher = rotationReaderProvider.createDirWatcher(dirWatcherConfiguration)
  }

  
  override def onPull(): Unit = {

    while(sendBuffer.isEmpty) {
      dirWatcher.processEvents()
      log.info("Triggered process events")
      Thread.sleep(1000)
    }

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
  
  
  override def postStop(): Unit = {
    dirWatcher.teardown()
    log.info("Tailin source is stopping.")
  }
}
