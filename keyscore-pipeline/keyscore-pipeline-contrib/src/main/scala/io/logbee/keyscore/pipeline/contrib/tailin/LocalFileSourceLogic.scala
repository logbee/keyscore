package io.logbee.keyscore.pipeline.contrib.tailin

import java.nio.charset.{Charset, StandardCharsets}
import java.nio.file.Paths

import akka.stream.SourceShape
import com.typesafe.config.Config
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.{LogicParameters, SourceLogic}
import io.logbee.keyscore.pipeline.commons.CommonCategories
import io.logbee.keyscore.pipeline.commons.CommonCategories.CATEGORY_LOCALIZATION
import io.logbee.keyscore.pipeline.contrib.tailin.LocalFileSourceLogic.Poll
import io.logbee.keyscore.pipeline.contrib.tailin.file.FileHandle
import io.logbee.keyscore.pipeline.contrib.tailin.file.local.{LocalDir, LocalFile}
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.{FilePersistenceContext, RAMPersistenceContext, ReadPersistence, ReadSchedule}
import io.logbee.keyscore.pipeline.contrib.tailin.read.FileReader.FileReadRecord
import io.logbee.keyscore.pipeline.contrib.tailin.read._
import io.logbee.keyscore.pipeline.contrib.tailin.watch.{BaseDirWatcher, FileMatchPattern, WatcherProvider}

import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}


object LocalFileSourceLogic extends Described {

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
        name = ReadMode.File.toString,
        displayName = TextRef("readMode.file.displayName"),
        description = TextRef("readMode.file.description")
      ),
    ),
  )
  
  val firstLinePattern = TextParameterDescriptor(
    ref = "tailin.readMode.multiLine.firstLinePattern",
    info = ParameterInfo(
      displayName = TextRef("readMode.multiLine.firstLinePattern.displayName"),
      description = TextRef("readMode.multiLine.firstLinePattern.description")
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
  
  object RenameAppend extends Enumeration {
    type RenameAppend = Value
    val Before, After = Value
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

  override def describe = Descriptor(
    ref = "5a754cd3-e11d-4dfb-a484-a9f83cf3d795",
    describes = SourceDescriptor(
      name = classOf[LocalFileSourceLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(CommonCategories.SOURCE, Category("File")),
      parameters = Seq(
        filePattern,
        readMode,
        firstLinePattern,
        fieldName,
        encoding,
        rotationPattern,
        postReadFileAction,
        renamePostReadFileAction_string,
        renamePostReadFileAction_append,
        persistenceEnabled,
      ),
      icon = Icon.fromClass(classOf[LocalFileSourceLogic]),
      maturity = Maturity.Development
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.tailin.LocalFileSourceLogic",
      Locale.ENGLISH, Locale.GERMAN
    ) ++ CATEGORY_LOCALIZATION
  )

  object Configuration {
    def apply(config: Config): Configuration = {
      val sub = config.getConfig("keyscore.local-file-source")
      new Configuration(
        filePersistenceConfig = sub.getConfig("file-persistence-context"),
        readBufferSize = sub.getMemorySize("read-buffer-size").toBytes.toInt,
      )
    }
  }
  case class Configuration(filePersistenceConfig: Config, readBufferSize: Int)

  private case object Poll
}

class LocalFileSourceLogic(parameters: LogicParameters, shape: SourceShape[Dataset]) extends SourceLogic(parameters, shape) {
  
  private val config = LocalFileSourceLogic.Configuration(system.settings.config)
  
  private var filePattern = LocalFileSourceLogic.filePattern.defaultValue
  private var readMode = ReadMode.Line.toString
  private var firstLinePattern = LocalFileSourceLogic.firstLinePattern.defaultValue
  private var fieldName = LocalFileSourceLogic.fieldName.defaultValue
  private var encoding = StandardCharsets.UTF_8.toString
  private var rotationPattern = LocalFileSourceLogic.rotationPattern.defaultValue
  private var postReadFileAction = PostReadFileAction.None.toString
  private var renamePostReadFileAction_string = LocalFileSourceLogic.renamePostReadFileAction_string.defaultValue
  private var renamePostReadFileAction_append = LocalFileSourceLogic.RenameAppend.After.toString
  private var persistenceEnabled = LocalFileSourceLogic.persistenceEnabled.defaultValue

  var dirWatcher: BaseDirWatcher = _

  var sendBuffer: SendBuffer = _
  var readPersistence: ReadPersistence = _

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: Configuration): Unit = {

    filePattern = configuration.getValueOrDefault(LocalFileSourceLogic.filePattern, filePattern)
    readMode = configuration.getValueOrDefault(LocalFileSourceLogic.readMode, readMode)
    firstLinePattern = configuration.getValueOrDefault(LocalFileSourceLogic.firstLinePattern, firstLinePattern)
    fieldName = configuration.getValueOrDefault(LocalFileSourceLogic.fieldName, fieldName)
    encoding = configuration.getValueOrDefault(LocalFileSourceLogic.encoding, encoding)
    rotationPattern = configuration.getValueOrDefault(LocalFileSourceLogic.rotationPattern, rotationPattern)
    postReadFileAction = configuration.getValueOrDefault(LocalFileSourceLogic.postReadFileAction, postReadFileAction)
    renamePostReadFileAction_string = configuration.getValueOrDefault(LocalFileSourceLogic.renamePostReadFileAction_string, renamePostReadFileAction_string)
    renamePostReadFileAction_append = configuration.getValueOrDefault(LocalFileSourceLogic.renamePostReadFileAction_append, renamePostReadFileAction_append)
    persistenceEnabled = configuration.getValueOrDefault(LocalFileSourceLogic.persistenceEnabled, persistenceEnabled)

    val invariableString = FileMatchPattern.extractInvariableDir(filePattern, java.io.File.separator) //start the first DirWatcher at the deepest level where no new sibling-directories can match the filePattern in the future
    if (invariableString.isEmpty
        || Paths.get(invariableString.get).toFile.isDirectory == false) {
        val message = "Could not parse the specified file pattern or could not find suitable parent directory to observe."
        log.error(message)
        fail(out, new IllegalArgumentException(message))
        return
    }

    val baseDir = Paths.get(invariableString.get)

    readPersistence = new ReadPersistence(completedPersistence = new RAMPersistenceContext(),
                                          committedPersistence = FilePersistenceContext(
                                            FilePersistenceContext.Configuration(
                                              config.filePersistenceConfig,
                                              persistenceEnabled,
                                              s"${classOf[LocalFileSourceLogic].getSimpleName}-${parameters.uuid}.json"
                                            )
                                          ))

    val bufferSize = config.readBufferSize

    var exclusionPattern = ""

    import LocalFileSourceLogic.RenameAppend
    val fileCompleteActions: Seq[FileHandle => Unit] =
      if (postReadFileAction.isEmpty)
        Seq.empty
      else {
        PostReadFileAction.fromString(postReadFileAction) match {
          case PostReadFileAction.None => Seq.empty

          case PostReadFileAction.Delete => Seq(file => file.delete() match {
            case Success(_) => log.debug(s"Deleted file '${file.absolutePath}'")
            case Failure(ex) => log.error(ex, "Could not delete file '{}': {}", file)
          })

          case PostReadFileAction.Rename =>

            if (renamePostReadFileAction_append.isEmpty) {
              val message = "When 'Rename' is selected as Post Read File Action, you need to specify whether the string should be appended before or after the file name."
              log.error(message)
              fail(out, new IllegalArgumentException(message))
              return
            }

            (RenameAppend.withName(renamePostReadFileAction_append), renamePostReadFileAction_string) match {
              case (_, "") | (_, null) => Seq.empty
              case (RenameAppend.Before, string) => {
                exclusionPattern = "**/" + string + "*"

                Seq(file => file.open {
                  case Success(openFile) => file.move(openFile.parent + string + openFile.name)
                  case Failure(ex) => log.error(ex, "Could not rename file '{}': {}", file)
                })
              }
              case (RenameAppend.After, string) => {
                exclusionPattern = "**/*" + string

                Seq(file => file.open {
                  case Success(openFile) => file.move(openFile.parent + openFile.name + string)
                  case Failure(ex) => log.error(ex, "Could not rename file '{}': {}", file)
                })
              }
              case (_, _) => Seq.empty
            }
        }
      }
    
    val matchPattern = new FileMatchPattern[LocalDir, LocalFile](filePattern, exclusionPattern)
    
    val fileReaderProvider = new FileReaderProvider(rotationPattern, bufferSize, Charset.forName(encoding), ReadMode.fromString(readMode), firstLinePattern, fileCompleteActions)
    
    val readSchedule = new ReadSchedule()
    val fileReaderManager = new FileReaderManager(fileReaderProvider, readSchedule, readPersistence, rotationPattern)
    sendBuffer = new SendBuffer(fileReaderManager, readPersistence)
    
    val readSchedulerProvider = new WatcherProvider[LocalDir, LocalFile](readSchedule, rotationPattern, readPersistence)
    dirWatcher = readSchedulerProvider.createDirWatcher(LocalDir(baseDir), matchPattern)
  }

  override def onTimer(timerKey: Any): Unit = {
    dirWatcher.processChanges()
    
    if (!sendBuffer.isEmpty) {
      doPush()
    }
    else {
      scheduleOnce(Poll, 1.second)
    }
  }

  private def doPush(): Unit = {
    
    val fileReadDataOpt = sendBuffer.getNextElement
    
    fileReadDataOpt match {

      case None =>
        scheduleOnce(Poll, 1.second)

      case Some(fileReadData) =>

        val outData = Dataset(
          records = List(Record(
            fields = List(
              Field(fieldName, TextValue(fileReadData.string)),
              Field("file.path", TextValue(fileReadData.baseFile.absolutePath)),
              Field("file.modified-timestamp", NumberValue(fileReadData.writeTimestamp)),
            )
          ))
        )
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
        scheduleOnce(Poll, 1.second)
      }
    }
  }

  override def postStop(): Unit = {
    log.info("Tailin source is stopping.")
  }
}
