package io.logbee.keyscore.pipeline.contrib.tailin

import java.nio.charset.{Charset, StandardCharsets}
import java.time.Duration

import akka.stream.SourceShape
import com.hierynomus.mserref.NtStatus
import com.hierynomus.mssmb2.SMBApiException
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.connection.Connection
import com.hierynomus.smbj.session.Session
import com.hierynomus.smbj.share.DiskShare
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
import io.logbee.keyscore.pipeline.contrib.tailin.SmbFileSourceLogic.Poll
import io.logbee.keyscore.pipeline.contrib.tailin.file.smb.{SmbDir, SmbFile}
import io.logbee.keyscore.pipeline.contrib.tailin.file.{DirNotOpenableException, FileHandle}
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.{FilePersistenceContext, RamPersistenceContext, ReadPersistence, ReadSchedule}
import io.logbee.keyscore.pipeline.contrib.tailin.read.FileReadRecord
import io.logbee.keyscore.pipeline.contrib.tailin.read._
import io.logbee.keyscore.pipeline.contrib.tailin.watch.{BaseDirWatcher, FileMatchPattern, WatchDirNotFoundException, WatcherProvider}

import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success}

object SmbFileSourceLogic extends Described {
  
  val hostName = TextParameterDescriptor(
    ref = "source.smb.hostName",
    info = ParameterInfo(
      displayName = TextRef("hostName"),
      description = TextRef("hostNameDescription")
    ),
    validator = StringValidator(
      expression = """^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\-]*[a-zA-Z0-9])\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\-]*[A-Za-z0-9])$""",
    ),
    defaultValue = "",
    mandatory = true,
  )

  val shareName = TextParameterDescriptor(
    ref = "source.smb.shareName",
    info = ParameterInfo(
      displayName = TextRef("shareName"),
      description = TextRef("shareNameDescription")
    ),
    validator = StringValidator(
      expression = """^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\-]*[a-zA-Z0-9])\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\-]*[A-Za-z0-9])$""",
    ),
    defaultValue = "",
    mandatory = true,
  )

  val domainName = TextParameterDescriptor(
    ref = "source.smb.domainName",
    info = ParameterInfo(
      displayName = TextRef("domainName"),
      description = TextRef("domainNameDescription")
    ),
    validator = StringValidator(
      expression = """^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\-]*[a-zA-Z0-9])\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\-]*[A-Za-z0-9])$""",
    ),
    defaultValue = "",
    mandatory = false,
  )

  val loginName = TextParameterDescriptor(
    ref = "source.smb.loginName",
    info = ParameterInfo(
      displayName = TextRef("loginName"),
      description = TextRef("loginNameDescription")
    ),
    validator = StringValidator(
      expression = """^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\-]*[a-zA-Z0-9])\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\-]*[A-Za-z0-9])$""",
    ),
    defaultValue = "",
    mandatory = false,
  )

  val password = PasswordParameterDescriptor(
    ref = "source.smb.password",
    info = ParameterInfo(
      displayName = TextRef("password"),
      description = TextRef("passwordDescription")
    ),
    validator = StringValidator(
      expression = """^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\-]*[a-zA-Z0-9])\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\-]*[A-Za-z0-9])$""",
    ),
    defaultValue = "",
    mandatory = false,
  )

  override def describe = Descriptor(
    ref = "3bbf4f3e-6131-4b20-944d-0686d6f6f539",
    describes = SourceDescriptor(
      name = classOf[SmbFileSourceLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(CommonCategories.SOURCE, Category("File"), Category("SMB")),
      parameters = Seq(
        hostName,
        shareName,
        domainName,
        loginName,
        password,
        LocalFileSourceLogic.filePattern,
        LocalFileSourceLogic.readMode,
        LocalFileSourceLogic.firstLinePattern,
        LocalFileSourceLogic.fieldName,
        LocalFileSourceLogic.encoding,
        LocalFileSourceLogic.rotationPattern,
        LocalFileSourceLogic.postReadFileAction,
        LocalFileSourceLogic.renamePostReadFileAction_string,
        LocalFileSourceLogic.renamePostReadFileAction_append,
        LocalFileSourceLogic.persistenceEnabled,
      ),
      icon = Icon.fromClass(classOf[SmbFileSourceLogic]),
      maturity = Maturity.Development
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.tailin.SmbFileSourceLogic",
      Locale.ENGLISH, Locale.GERMAN
    ) ++ CATEGORY_LOCALIZATION
  )

  object Configuration {
    import scala.language.implicitConversions
    private implicit def convertDuration(duration: Duration): FiniteDuration = scala.concurrent.duration.Duration.fromNanos(duration.toNanos)
    
    def apply(config: Config): Configuration = {
      val sub = config.getConfig("keyscore.smb-source")
      new Configuration(
        filePersistenceConfig = sub.getConfig("file-persistence-context"),
        pollInterval = sub.getDuration("poll-interval"),
        connectRetryInterval = sub.getDuration("connect-retry-interval"),
        baseDirNotFoundRetryInterval = sub.getDuration("base-dir-not-found-retry-interval"),
        readBufferSize = sub.getMemorySize("read-buffer-size").toBytes.toInt,
      )
    }
  }
  case class Configuration(filePersistenceConfig: Config, pollInterval: FiniteDuration, connectRetryInterval: FiniteDuration, baseDirNotFoundRetryInterval: FiniteDuration, readBufferSize: Int)

  private case object Poll
}

class SmbFileSourceLogic(parameters: LogicParameters, shape: SourceShape[Dataset]) extends SourceLogic(parameters, shape) {
  
  private val config = SmbFileSourceLogic.Configuration(system.settings.config)
  
  private var hostName = SmbFileSourceLogic.hostName.defaultValue
  private var shareName = SmbFileSourceLogic.shareName.defaultValue
  private var domainName = SmbFileSourceLogic.domainName.defaultValue
  private var loginName = SmbFileSourceLogic.loginName.defaultValue
  private var password = SmbFileSourceLogic.password.defaultValue
  
  //like LocalFileSourceLogic
  private var filePattern = LocalFileSourceLogic.filePattern.defaultValue
  private var readMode = ReadMode.Line.toString
  private var firstLinePattern = LocalFileSourceLogic.firstLinePattern.defaultValue
  private var fieldName = LocalFileSourceLogic.fieldName.defaultValue
  private var encoding = StandardCharsets.UTF_8.toString
  private var rotationPattern = LocalFileSourceLogic.rotationPattern.defaultValue
  private var postFileReadAction = PostReadFileAction.None.toString
  private var renamePostReadFileAction_string = LocalFileSourceLogic.renamePostReadFileAction_string.defaultValue
  private var renamePostReadFileAction_append = LocalFileSourceLogic.RenameAppend.After.toString
  private var persistenceEnabled = LocalFileSourceLogic.persistenceEnabled.defaultValue

  var connection: Connection = _
  var share: DiskShare = _

  var dirWatcher: BaseDirWatcher = _
  
  var sendBuffer: SendBuffer = _
  var readPersistence: ReadPersistence = _
  
  var baseDirString: String = _
  var smbFilePattern: FileMatchPattern[SmbDir, SmbFile] = _
  
  var readSchedulerProvider: WatcherProvider[SmbDir, SmbFile] = _
  var baseDir: SmbDir = _

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
  }
  
  override def configure(configuration: Configuration): Unit = {
    hostName = configuration.getValueOrDefault(SmbFileSourceLogic.hostName, hostName)
		shareName = configuration.getValueOrDefault(SmbFileSourceLogic.shareName, shareName)
		domainName = configuration.getValueOrDefault(SmbFileSourceLogic.domainName, domainName)
		loginName = configuration.getValueOrDefault(SmbFileSourceLogic.loginName, loginName)
		password = configuration.getValueOrDefault(SmbFileSourceLogic.password, password)
    
		//like LocalFileSourceLogic
    filePattern = configuration.getValueOrDefault(LocalFileSourceLogic.filePattern, filePattern)
    readMode = configuration.getValueOrDefault(LocalFileSourceLogic.readMode, readMode)
    firstLinePattern = configuration.getValueOrDefault(LocalFileSourceLogic.firstLinePattern, firstLinePattern)
    fieldName = configuration.getValueOrDefault(LocalFileSourceLogic.fieldName, fieldName)
    encoding = configuration.getValueOrDefault(LocalFileSourceLogic.encoding, encoding)
    rotationPattern = configuration.getValueOrDefault(LocalFileSourceLogic.rotationPattern, rotationPattern)
    postFileReadAction = configuration.getValueOrDefault(LocalFileSourceLogic.postReadFileAction, postFileReadAction)
    renamePostReadFileAction_string = configuration.getValueOrDefault(LocalFileSourceLogic.renamePostReadFileAction_string, renamePostReadFileAction_string)
    renamePostReadFileAction_append = configuration.getValueOrDefault(LocalFileSourceLogic.renamePostReadFileAction_append, renamePostReadFileAction_append)
    persistenceEnabled = configuration.getValueOrDefault(LocalFileSourceLogic.persistenceEnabled, persistenceEnabled)

    readPersistence = new ReadPersistence(completedPersistence = new RamPersistenceContext(),
                                          committedPersistence = FilePersistenceContext(
                                            FilePersistenceContext.Configuration(
                                              config.filePersistenceConfig,
                                              persistenceEnabled,
                                              s"${classOf[SmbFileSourceLogic].getSimpleName}-${parameters.uuid}"
                                            )
                                          ))

    var exclusionPattern = ""

    import LocalFileSourceLogic.RenameAppend
    val postFileReadActions: Seq[FileHandle => Unit] =
      if (postFileReadAction.isEmpty)
        Seq.empty
      else {
        PostReadFileAction.fromString(postFileReadAction) match {
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

    var filePatternWithoutLeadingSlashes = filePattern.replace('/', '\\') //transform any forward slashes to backward slashes -> SMB/Windows paths cannot contain forward slashes, so this should be safe
    while (filePatternWithoutLeadingSlashes.startsWith("/") || filePatternWithoutLeadingSlashes.startsWith("\\")) {
      filePatternWithoutLeadingSlashes = filePatternWithoutLeadingSlashes.substring(1)
    }
    
        val absoluteFilePattern = s"\\\\$hostName\\$shareName\\$filePatternWithoutLeadingSlashes"
    val absoluteExclusionPattern = s"\\\\$hostName\\$shareName\\$exclusionPattern"
     
    smbFilePattern = new FileMatchPattern[SmbDir, SmbFile](absoluteFilePattern,
                                                           absoluteExclusionPattern)

    //start the first DirWatcher at the deepest level where no new sibling-directories can match the filePattern in the future
    FileMatchPattern.extractInvariableDir(absoluteFilePattern, "\\") match {
      case None =>
        val message = "Could not parse the specified file pattern or could not find suitable parent directory to observe."
        log.error(message)
        fail(out, new IllegalArgumentException(message))
        return
        
      case Some(baseDirString) =>
        this.baseDirString = baseDirString
        log.debug("Selecting '{}' as base directory to start the first DirWatcher in.", baseDirString)
    }
    
    val readSchedule = new ReadSchedule()
    val fileReaderProvider = new FileReaderProvider(rotationPattern, config.readBufferSize, Charset.forName(encoding), ReadMode.fromString(readMode), firstLinePattern, postFileReadActions)
    
    val fileReaderManager = new FileReaderManager(fileReaderProvider, readSchedule, readPersistence, rotationPattern)
    sendBuffer = new SendBuffer(fileReaderManager, readPersistence)
    
    readSchedulerProvider = new WatcherProvider(readSchedule, rotationPattern, readPersistence)
  }
  
  override def postStop(): Unit = {
    log.info(s"${classOf[SmbFileSourceLogic].getSimpleName} is stopping.")

    if (share != null) {
      share.close()
    }
    
    if (connection != null) {
      connection.close()
    }
  }

  override def onTimer(timerKey: Any): Unit = {
    
    timerKey match {
      case Poll => onPull()
    }
  }

  private def doPush(): Unit = {
    val fileReadDataOpt = sendBuffer.getNextElement
    
    fileReadDataOpt match {

      case None =>
        scheduleOnce(timerKey = Poll, config.pollInterval)

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

  override def onPull(): Unit = {

    if (connectionNotReady) {
      setupConnection()
      return
    }

    if (shareNotReady) {
      setupShare()
      return
    }

    if (baseDir == null) {
      setupSmbBaseDir()
      return
    }

    if (dirWatcher == null) {
      setupDirWatcher()
      return
    }

    if (!sendBuffer.isEmpty) {
      doPush()
    }
    else {
      try {
        dirWatcher.processChanges()
        scheduleOnce(timerKey = Poll, config.pollInterval)
      }
      catch {
        case _: WatchDirNotFoundException => setupSmbBaseDir()
      }
    }
  }

  private def setupConnection(): Unit = {
    val client = new SMBClient()
    try {
      connection = client.connect(hostName)
      onPull()
    }
    catch {
      case ex: Throwable =>
        log.error(ex, "Could not connect: '{}'. Retrying in {}.", config.connectRetryInterval)
        scheduleOnce(Poll, config.connectRetryInterval)
    }
  }
  
  private def setupShare(): Unit = {
    val authContext = new AuthenticationContext(loginName, password.toCharArray, domainName)
    
    var session: Session = null
    try {
      session = connection.authenticate(authContext)
      share = session.connectShare(shareName).asInstanceOf[DiskShare]
      onPull()
    }
    catch {
      case ex: SMBApiException =>
        if (ex.getStatus == NtStatus.STATUS_LOGON_FAILURE) {
          log.error(ex, "Could not authenticate for user '{}'.", loginName)
          fail(out, ex)
        }
        else if (ex.getStatus == NtStatus.STATUS_BAD_NETWORK_NAME) {
          log.error(ex, "Could not find share with name '{}'", shareName)
          fail(out, ex)
        }
    }
  }

  private def setupSmbBaseDir(): Unit = {
    
    try {
      baseDir = SmbDir(baseDirString, share)
      onPull()
    }
    catch {
      case ex: SMBApiException =>
        if (ex.getStatus == NtStatus.STATUS_OBJECT_NAME_NOT_FOUND
         || ex.getStatus == NtStatus.STATUS_OBJECT_PATH_NOT_FOUND) { //occurs when not even the parent directory exists
          log.error(ex, s"The determined base directory '$baseDirString' does not exist. Retrying in ${config.baseDirNotFoundRetryInterval}.")
        }
        else {
          log.error(ex, s"Error while trying to open the determined base directory '$baseDirString'. Retrying in ${config.baseDirNotFoundRetryInterval}.")
        }

        scheduleOnce(Poll, config.baseDirNotFoundRetryInterval)
    }
  }

  private def setupDirWatcher(): Unit = {
    try {
      dirWatcher = readSchedulerProvider.createDirWatcher(baseDir, smbFilePattern)
      onPull()
    }
    catch {
      case ex: DirNotOpenableException =>
        log.error(ex, s"The determined base directory '$baseDirString' does not exist. Retrying in ${config.baseDirNotFoundRetryInterval}.")
        scheduleOnce(Poll, config.baseDirNotFoundRetryInterval)
    }
  }

  private def connectionNotReady: Boolean = {
    connection == null || !connection.isConnected
  }

  private def shareNotReady: Boolean = {
    share == null || !share.isConnected
  }
}
