package io.logbee.keyscore.pipeline.contrib.tailin

import java.io.File
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Paths

import scala.concurrent.duration.DurationInt
import akka.stream.SourceShape
import com.typesafe.config.Config
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.data.Field
import io.logbee.keyscore.model.data.Icon
import io.logbee.keyscore.model.data.Label
import io.logbee.keyscore.model.data.MetaData
import io.logbee.keyscore.model.data.NumberValue
import io.logbee.keyscore.model.data.Record
import io.logbee.keyscore.model.data.TextValue
import io.logbee.keyscore.model.descriptor.{BooleanParameterDescriptor, Category, Choice, ChoiceParameterDescriptor, Descriptor, FieldNameHint, FieldNameParameterDescriptor, ParameterInfo, SourceDescriptor, StringValidator, TextParameterDescriptor}
import io.logbee.keyscore.model.localization.Locale
import io.logbee.keyscore.model.localization.Localization
import io.logbee.keyscore.model.localization.TextRef
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.LogicParameters
import io.logbee.keyscore.pipeline.api.SourceLogic
import io.logbee.keyscore.pipeline.commons.CommonCategories
import io.logbee.keyscore.pipeline.commons.CommonCategories.CATEGORY_LOCALIZATION
import io.logbee.keyscore.pipeline.contrib.tailin.file.local.LocalDir
import io.logbee.keyscore.pipeline.contrib.tailin.file.FileHandle
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.FilePersistenceContext
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.RAMPersistenceContext
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadPersistence
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadSchedule
import io.logbee.keyscore.pipeline.contrib.tailin.read.FileReadRecord
import io.logbee.keyscore.pipeline.contrib.tailin.read.FileReaderManager
import io.logbee.keyscore.pipeline.contrib.tailin.read.FileReaderProvider
import io.logbee.keyscore.pipeline.contrib.tailin.read.ReadMode
import io.logbee.keyscore.pipeline.contrib.tailin.read.SendBuffer
import io.logbee.keyscore.pipeline.contrib.tailin.watch.BaseDirWatcher
import io.logbee.keyscore.pipeline.contrib.tailin.watch.FileMatchPattern
import io.logbee.keyscore.pipeline.contrib.tailin.watch.WatcherProvider


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
  
  
  
  object OnComplete extends Enumeration {
    type OnComplete = Value
    val None, Delete, Rename = Value
  }
  
  val onComplete = ChoiceParameterDescriptor(
    ref = "tailin.onComplete",
    info = ParameterInfo(
      displayName = TextRef("onComplete.displayName"),
      description = TextRef("onComplete.description"),
    ),
    min = 1,
    max = 1,
    choices = Seq(
      Choice(
        name = OnComplete.None.toString,
        displayName = TextRef("onComplete.none.displayName"),
        description = TextRef("onComplete.none.description"),
      ),
      Choice(
        name = OnComplete.Delete.toString,
        displayName = TextRef("onComplete.delete.displayName"),
        description = TextRef("onComplete.delete.description"),
      ),
      Choice(
        name = OnComplete.Rename.toString,
        displayName = TextRef("onComplete.rename.displayName"),
        description = TextRef("onComplete.rename.description"),
      ),
    ),
  )
  
  
  val renameOnComplete_string = TextParameterDescriptor(
    ref = "tailin.onComplete.rename.string",
    info = ParameterInfo(
      displayName = TextRef("onComplete.rename.string.displayName"),
      description = TextRef("onComplete.rename.string.description")
    ),
    validator = StringValidator(
      expression = """^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\-]*[a-zA-Z0-9])\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\-]*[A-Za-z0-9])$""",
    ),
    defaultValue = "",
    mandatory = false
  )
  
  
  object RenameAppend extends Enumeration {
    type RenameAppend = Value
    val Before, After = Value
  }
  
  val renameOnComplete_append = ChoiceParameterDescriptor(
    ref = "tailin.onComplete.rename.append",
    info = ParameterInfo(
      displayName = TextRef("onComplete.rename.append.displayName"),
      description = TextRef("onComplete.rename.append.description")
    ),
    min = 1,
    max = 1,
    choices = Seq(
      Choice(
        name = RenameAppend.Before.toString,
        displayName = TextRef("onComplete.rename.append.before.displayName"),
        description = TextRef("onComplete.rename.append.before.description")
      ),
      Choice(
        name = RenameAppend.After.toString,
        displayName = TextRef("onComplete.rename.append.after.displayName"),
        description = TextRef("onComplete.rename.append.after.description")
      ),
    ),
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
        fieldName,
        encoding,
        rotationPattern,
        onComplete,
        renameOnComplete_string,
        renameOnComplete_append,
      ),
      icon = Icon.fromClass(classOf[TailinSourceLogic])
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.tailin.TailinSourceLogic",
      Locale.ENGLISH, Locale.GERMAN
    ) ++ CATEGORY_LOCALIZATION
  )
  
  
  object Configuration {
    def apply(config: Config): Configuration = {
      val sub = config.getConfig("keyscore.local-file-source")
      new Configuration(
        persistenceFile = sub.getString("persistence-file"),
        readBufferSize = sub.getMemorySize("read-buffer-size").toBytes.toInt,
      )
    }
  }
  case class Configuration(persistenceFile: String, readBufferSize: Int)
}


class TailinSourceLogic(parameters: LogicParameters, shape: SourceShape[Dataset]) extends SourceLogic(parameters, shape) {
  
  private val config = TailinSourceLogic.Configuration(system.settings.config)
  
  private var filePattern = TailinSourceLogic.filePattern.defaultValue
  private var readMode = ReadMode.LINE.toString
  private var fieldName = TailinSourceLogic.fieldName.defaultValue
  private var encoding = StandardCharsets.UTF_8.toString
  private var rotationPattern = TailinSourceLogic.rotationPattern.defaultValue
  private var onComplete = TailinSourceLogic.OnComplete.None.toString
  private var renameOnComplete_string = TailinSourceLogic.renameOnComplete_string.defaultValue
  private var renameOnComplete_append = TailinSourceLogic.RenameAppend.After.toString
  
  
  private val persistenceFile = config.persistenceFile
  
  
  var dirWatcher: BaseDirWatcher = _
  
  var sendBuffer: SendBuffer = null
  var readPersistence: ReadPersistence = null
  
  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
  }
  
  override def configure(configuration: Configuration): Unit = {
    filePattern = configuration.getValueOrDefault(TailinSourceLogic.filePattern, filePattern)
    readMode = configuration.getValueOrDefault(TailinSourceLogic.readMode, readMode)
    fieldName = configuration.getValueOrDefault(TailinSourceLogic.fieldName, fieldName)
    encoding = configuration.getValueOrDefault(TailinSourceLogic.encoding, encoding)
    rotationPattern = configuration.getValueOrDefault(TailinSourceLogic.rotationPattern, rotationPattern)
    onComplete = configuration.getValueOrDefault(TailinSourceLogic.onComplete, onComplete)
    renameOnComplete_string = configuration.getValueOrDefault(TailinSourceLogic.renameOnComplete_string, renameOnComplete_string)
    renameOnComplete_append = configuration.getValueOrDefault(TailinSourceLogic.renameOnComplete_append, renameOnComplete_append)
    
    
    val invariableString = FileMatchPattern.extractInvariableDir(filePattern) //start the first DirWatcher at the deepest level where no new sibling-directories can match the filePattern in the future
    if (invariableString.isEmpty
        || Paths.get(invariableString.get).toFile.isDirectory == false) {
        val message = "Could not parse the specified file pattern or could not find suitable parent directory to observe."
        log.error(message)
        fail(out, new IllegalArgumentException(message))
        return
    }
    
    val baseDir = Paths.get(invariableString.get)
    
    val _persistenceFile = new File(persistenceFile)
    _persistenceFile.createNewFile()
    
    for (i <- 1 to 50) {
      if (_persistenceFile.exists == false) {
        Thread.sleep(100)
      }
    }
    
    
    readPersistence = new ReadPersistence(completedPersistence = new RAMPersistenceContext(),
                                          committedPersistence = new FilePersistenceContext(_persistenceFile))
    
    val bufferSize = config.readBufferSize
    
    
    var exclusionPattern = ""
    
    import TailinSourceLogic.OnComplete
    import TailinSourceLogic.RenameAppend
    val fileCompleteActions: Seq[FileHandle => Unit] =
      OnComplete.withName(onComplete) match {
        case OnComplete.None => Seq.empty
        case OnComplete.Delete => Seq(_.delete())
        case OnComplete.Rename =>
          
          if (renameOnComplete_append.isEmpty) {
            val message = "When 'Rename' is selected as Post Read File Action, you need to specify whether the string should be appended before or after the file name."
            log.error(message)
            fail(out, new IllegalArgumentException(message))
            return
          }
          
          (RenameAppend.withName(renameOnComplete_append), renameOnComplete_string) match {
            case (_, "") | (_, null) => Seq.empty
            case (RenameAppend.Before, string) => {
              exclusionPattern = "**/" + string + "*"
              
              Seq(file => file.move(file.parent + string + file.name))
            }
            case (RenameAppend.After, string) => {
              exclusionPattern = "**/*" + string
              
              Seq(file => file.move(file.parent + file.name + string))
            }
            case (_, _) => Seq.empty
          }
    }
    
    val matchPattern = new FileMatchPattern(filePattern, exclusionPattern)
    
    val readSchedule = new ReadSchedule()
    val fileReaderProvider = new FileReaderProvider(rotationPattern, bufferSize, Charset.forName(encoding), ReadMode.withName(readMode), fileCompleteActions)
    
    val fileReaderManager = new FileReaderManager(fileReaderProvider, readSchedule, readPersistence, rotationPattern)
    sendBuffer = new SendBuffer(fileReaderManager, readPersistence)
    
    val readSchedulerProvider = new WatcherProvider(readSchedule, rotationPattern, readPersistence)
    dirWatcher = readSchedulerProvider.createDirWatcher(new LocalDir(baseDir), matchPattern)
  }
  
  
  
  override def onTimer(timerKey: Any): Unit = {
    dirWatcher.processChanges()
    
    if (!sendBuffer.isEmpty) {
      doPush()
    }
    else {
      scheduleOnce(timerKey = "poll", 1.second)
    }
  }
  
  
  private def doPush(): Unit = {
    
    val fileReadDataOpt = sendBuffer.getNextElement
    
    fileReadDataOpt match {
      case None =>
        scheduleOnce(timerKey = "poll", 1.second)
      case Some(fileReadData) =>
      
      
      val outData = Dataset(
        records = List(Record(
          fields = List(
            Field(fieldName, TextValue(fileReadData.string)),
            Field("local.file.path", TextValue(fileReadData.baseFile.absolutePath)),
            Field("local.file.writeTimestamp", NumberValue(fileReadData.writeTimestamp)),
          )
        ))
      )
      
      log.info(s"Created Datasets: $outData")
      
      push(out, outData)
      readPersistence.commitRead(fileReadData.baseFile, FileReadRecord(fileReadData.readEndPos, fileReadData.writeTimestamp, fileReadData.newerFilesWithSharedLastModified))
    }
  }
  
  
  override def onPull(): Unit = {
    
    if (!sendBuffer.isEmpty) {
      doPush()
    }
    else {
      dirWatcher.processChanges()
      
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
