import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths, StandardOpenOption}

import akka.stream.SourceShape
import io.logbee.keyscore.contrib.tailin.{DirWatcher, DirWatcherConfiguration, ReadMode, RotationReaderProvider}
import io.logbee.keyscore.contrib.tailin.persistence.FilePersistenceContext
import io.logbee.keyscore.contrib.tailin.send.SendBuffer
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
import io.logbee.keyscore.pipeline.contrib.kafka.KafkaSourceLogic
import io.logbee.keyscore.pipeline.contrib.tailin.util.FileUtility

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
    defaultValue = "test",
    mandatory = true
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
        filePattern
      ),
      icon = Icon.fromClass(classOf[KafkaSourceLogic])
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.kafka.KafkaSourceLogic",
      Locale.ENGLISH, Locale.GERMAN
    ) ++ CATEGORY_LOCALIZATION
  )
}

class TailinSourceLogic(parameters: LogicParameters, shape: SourceShape[Dataset]) extends SourceLogic(parameters, shape) {

  private var directoryPath = ""
  private var filePattern = ""



  var dirWatcher: DirWatcher = _

  val sendBuffer = new SendBuffer()



  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: Configuration): Unit = {
    directoryPath = configuration.getValueOrDefault(TailinSourceLogic.directoryPath, directoryPath)
    filePattern = configuration.getValueOrDefault(TailinSourceLogic.filePattern, filePattern)



    val watchDir = Paths.get(directoryPath)

    val rotationSuffix = ".[1-5]"

    val persistenceFile: File = new File(".keyscoreFileTailinPersistence")

    persistenceFile.createNewFile()
    FileUtility.waitForFileToExist(persistenceFile)


    val dirWatcherConfiguration = DirWatcherConfiguration(watchDir, watchDir, filePattern)  //TODO check if basedir is still needed
    val persistenceContext = new FilePersistenceContext(persistenceFile)
    val bufferSize = 1024


    val callback: String => Unit = {
      data: String =>
        sendBuffer.addToBuffer(data)
    }

    val readMode = ReadMode.LINE

    val rotationReaderProvider = new RotationReaderProvider(rotationSuffix, persistenceContext, bufferSize, callback, StandardCharsets.UTF_8, readMode)
    dirWatcher = rotationReaderProvider.createDirWatcher(dirWatcherConfiguration)
  }


  override def onPull(): Unit = {

    while(sendBuffer.isEmpty) {
      dirWatcher.processEvents()
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

    push(out, outData)
  }
}
