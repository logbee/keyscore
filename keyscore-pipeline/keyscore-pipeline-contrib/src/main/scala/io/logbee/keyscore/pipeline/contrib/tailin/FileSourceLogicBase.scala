package io.logbee.keyscore.pipeline.contrib.tailin

import java.nio.charset.{Charset, StandardCharsets}
import java.time.Duration

import akka.stream.SourceShape
import com.typesafe.config.Config
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.pipeline.api.{LogicParameters, SourceLogic}
import io.logbee.keyscore.pipeline.contrib.tailin.FileSourceLogicBase.{Poll, RenameAppend}
import io.logbee.keyscore.pipeline.contrib.tailin.file.{DirHandle, FileHandle}
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.{FilePersistenceContext, RamPersistenceContext, ReadPersistence, ReadSchedule}
import io.logbee.keyscore.pipeline.contrib.tailin.read._
import io.logbee.keyscore.pipeline.contrib.tailin.watch.WatcherProvider
import org.slf4j.LoggerFactory

import scala.concurrent.duration.FiniteDuration

object FileSourceLogicBase {
  import io.logbee.keyscore.model.util.ToOption.T2OptionT

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
        name = ReadMode.Line.toString,
        displayName = TextRef("readMode.line.displayName"),
        description = TextRef("readMode.line.description")
      ),
      Choice(
        name = ReadMode.MultiLine.toString,
        displayName = TextRef("readMode.multiLine.displayName"),
        description = TextRef("readMode.multiLine.description")
      ),
      Choice(
        name = ReadMode.MultiLineWithEnd.toString,
        displayName = TextRef("readMode.multiLineWithEnd.displayName"),
        description = TextRef("readMode.multiLineWithEnd.description")
      ),
      Choice(
        name = ReadMode.File.toString,
        displayName = TextRef("readMode.file.displayName"),
        description = TextRef("readMode.file.description")
      ),
    ),
  )
  
  val firstLinePattern = ExpressionParameterDescriptor(
    ref = "tailin.readMode.multiLine.firstLinePattern",
    info = ParameterInfo(
      displayName = TextRef("readMode.multiLine.firstLinePattern.displayName"),
      description = TextRef("readMode.multiLine.firstLinePattern.description")
    ),
    choices = Seq(
      Choice(
        name = "tailin.readMode.multiLine.firstLinePattern.regex",
        displayName = TextRef("readMode.multiLine.regex.displayName"),
        description = TextRef("readMode.multiLine.regex.description")
      )
    ),
    mandatory = false
  )
  
  val lastLinePattern = ExpressionParameterDescriptor(
    ref = "tailin.readMode.multiLine.lastLinePattern",
    info = ParameterInfo(
      displayName = TextRef("readMode.multiLine.lastLinePattern.displayName"),
      description = TextRef("readMode.multiLine.lastLinePattern.description")
    ),
    choices = Seq(
      Choice(
        name = "tailin.readMode.multiLine.lastLinePattern.regex",
        displayName = TextRef("readMode.multiLine.regex.displayName"),
        description = TextRef("readMode.multiLine.regex.description")
      )
    ),
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

  val postReadFileAction = ChoiceParameterDescriptor(
    ref = "tailin.postReadFileAction",
    info = ParameterInfo(
      displayName = TextRef("postReadFileAction.displayName"),
      description = TextRef("postReadFileAction.description"),
    ),
    min = 1,
    max = 1,
    choices = Seq(
      Choice(
        name = PostReadFileAction.None.toString,
        displayName = TextRef("postReadFileAction.none.displayName"),
        description = TextRef("postReadFileAction.none.description"),
      ),
      Choice(
        name = PostReadFileAction.Delete.toString,
        displayName = TextRef("postReadFileAction.delete.displayName"),
        description = TextRef("postReadFileAction.delete.description"),
      ),
      Choice(
        name = PostReadFileAction.Rename.toString,
        displayName = TextRef("postReadFileAction.rename.displayName"),
        description = TextRef("postReadFileAction.rename.description"),
      ),
    ),
  )

  val renamePostReadFileAction_string = TextParameterDescriptor(
    ref = "tailin.postReadFileAction.rename.string",
    info = ParameterInfo(
      displayName = TextRef("postReadFileAction.rename.string.displayName"),
      description = TextRef("postReadFileAction.rename.string.description")
    ),
    validator = StringValidator(
      expression = """^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\-]*[a-zA-Z0-9])\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\-]*[A-Za-z0-9])$""",
    ),
    defaultValue = "",
    mandatory = false
  )
  
  
  sealed trait RenameAppend
  object RenameAppend {
    private lazy val log = LoggerFactory.getLogger(classOf[PostReadFileAction])
  
    case object None extends RenameAppend
    case object Before extends RenameAppend
    case object After extends RenameAppend
  
    def fromString(value: String): RenameAppend = value.toLowerCase match {
      case "" | "none" => None
      case "before" => Before
      case "after" => After
      case _ => throw new IllegalArgumentException(s"Unknown RenameAppend: '$value'. Possible values are: [$None, $Before, $After].")
    }
  }
  
  val renamePostReadFileAction_append = ChoiceParameterDescriptor(
    ref = "tailin.postReadFileAction.rename.append",
    info = ParameterInfo(
      displayName = TextRef("postReadFileAction.rename.append.displayName"),
      description = TextRef("postReadFileAction.rename.append.description")
    ),
    min = 1,
    max = 1,
    choices = Seq(
      Choice(
        name = RenameAppend.Before.toString,
        displayName = TextRef("postReadFileAction.rename.append.before.displayName"),
        description = TextRef("postReadFileAction.rename.append.before.description")
      ),
      Choice(
        name = RenameAppend.After.toString,
        displayName = TextRef("postReadFileAction.rename.append.after.displayName"),
        description = TextRef("postReadFileAction.rename.append.after.description")
      ),
    ),
  )
  
  val persistenceEnabled = BooleanParameterDescriptor(
    ref = "tailin.persistenceEnabled",
    info = ParameterInfo(
      displayName = TextRef("persistence.enabled.displayName"),
      description = TextRef("persistence.enabled.description")
    ),
    defaultValue = true,
    mandatory = false
  )

  val LOCALIZATION: Localization = Localization.fromResourceBundle(
    bundleName = classOf[FileSourceLogicBase[_, _]].getName,
    Locale.ENGLISH, Locale.GERMAN
  )
  
  case object Poll
}

abstract class FileSourceLogicBase[D <: DirHandle[D, F], F <: FileHandle](parameters: LogicParameters, shape: SourceShape[Dataset]) extends SourceLogic(parameters, shape) {
  
  protected var filePattern: String = FileSourceLogicBase.filePattern.defaultValue
  private var readMode = ReadMode.Line.toString
  private var firstLinePattern = FileSourceLogicBase.firstLinePattern.defaultValue
  private var lastLinePattern = FileSourceLogicBase.lastLinePattern.defaultValue
  private var fieldName = FileSourceLogicBase.fieldName.defaultValue
  private var encoding = StandardCharsets.UTF_8.toString
  private var rotationPattern = FileSourceLogicBase.rotationPattern.defaultValue
  private var persistenceEnabled = FileSourceLogicBase.persistenceEnabled.defaultValue
  private var readPersistence: ReadPersistence = _
  
  protected var sendBuffer: SendBuffer = _
  protected var watcherProvider: WatcherProvider[D, F] = _
  
  
  protected val pollInterval: FiniteDuration
  
  protected val readBufferSize: Int
  
  protected val filePersistenceConfig: Config
  protected val persistenceFileIdentifier: String
  
  protected val processChangesErrorTimeout: Duration
  
  
  
  override def initialize(configuration: Configuration): Unit = configure(configuration)
  
  override def configure(configuration: Configuration): Unit = {
    filePattern = configuration.getValueOrDefault(FileSourceLogicBase.filePattern, filePattern)
    readMode = configuration.getValueOrDefault(FileSourceLogicBase.readMode, readMode)
    firstLinePattern = configuration.getValueOrDefault(FileSourceLogicBase.firstLinePattern, firstLinePattern)
    lastLinePattern = configuration.getValueOrDefault(FileSourceLogicBase.lastLinePattern, lastLinePattern)
    fieldName = configuration.getValueOrDefault(FileSourceLogicBase.fieldName, fieldName)
    encoding = configuration.getValueOrDefault(FileSourceLogicBase.encoding, encoding)
    rotationPattern = configuration.getValueOrDefault(FileSourceLogicBase.rotationPattern, rotationPattern)
    persistenceEnabled = configuration.getValueOrDefault(FileSourceLogicBase.persistenceEnabled, persistenceEnabled)
    
    val postReadFileAction = configuration.getValueOrDefault(FileSourceLogicBase.postReadFileAction, PostReadFileAction.None.toString)
    val renamePostReadFileAction_string = configuration.getValueOrDefault(FileSourceLogicBase.renamePostReadFileAction_string, FileSourceLogicBase.renamePostReadFileAction_string.defaultValue)
    val renamePostReadFileAction_append = configuration.getValueOrDefault(FileSourceLogicBase.renamePostReadFileAction_append, FileSourceLogicBase.RenameAppend.After.toString)
    
    val postReadFileActionFunc = PostReadFileAction.createFunc(
      PostReadFileAction.fromString(postReadFileAction),
      RenameAppend.fromString(renamePostReadFileAction_append),
      renamePostReadFileAction_string
    )
    
    readPersistence = new ReadPersistence(
      completedPersistence = new RamPersistenceContext(),
      committedPersistence = FilePersistenceContext(
        FilePersistenceContext.Configuration(
          filePersistenceConfig,
          persistenceEnabled,
          s"${persistenceFileIdentifier}-${parameters.uuid}"
        )
      )
    )
    
    val fileReaderProvider = new FileReaderProvider(readBufferSize, Charset.forName(encoding), ReadMode.fromStringParams(readMode, firstLinePattern, lastLinePattern), postReadFileActionFunc)
    val readSchedule = new ReadSchedule()
    val fileReaderManager = new FileReaderManager(fileReaderProvider, readSchedule, readPersistence, rotationPattern)
    sendBuffer = new SendBuffer(fileReaderManager, readPersistence)
    
    watcherProvider = new WatcherProvider[D, F](readSchedule, rotationPattern, readPersistence, processChangesErrorTimeout)
  }
  
  
  protected def doPush(): Unit = {
    val fileReadDataOpt = sendBuffer.getNextElement
    
    fileReadDataOpt match {

      case None =>
        scheduleOnce(Poll, pollInterval)

      case Some(fileReadData) =>
        val outData = Dataset(
          records = List(Record(
            fields = List(
              Field(fieldName, TextValue(fileReadData.readData)),
              Field("file.path", TextValue(fileReadData.baseFile.absolutePath)),
              Field("file.modified-timestamp", TimestampValue(fileReadData.writeTimestamp / 1000, (fileReadData.writeTimestamp % 1000 * 1000000).asInstanceOf[Int])),
            )
          ))
        )
        push(out, outData)
        readPersistence.commitRead(fileReadData.baseFile, FileReadRecord(fileReadData.readEndPos, fileReadData.writeTimestamp, fileReadData.newerFilesWithSharedLastModified))
    }
  }
}
