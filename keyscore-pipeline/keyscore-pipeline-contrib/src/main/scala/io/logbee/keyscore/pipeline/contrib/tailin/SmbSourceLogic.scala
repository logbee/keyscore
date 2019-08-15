package io.logbee.keyscore.pipeline.contrib.tailin

import java.io.File
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
import io.logbee.keyscore.pipeline.contrib.tailin.file.FileHandle
import io.logbee.keyscore.pipeline.contrib.tailin.file.smb.SmbDir
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.{FilePersistenceContext, RAMPersistenceContext, ReadPersistence, ReadSchedule}
import io.logbee.keyscore.pipeline.contrib.tailin.read._
import io.logbee.keyscore.pipeline.contrib.tailin.watch.{BaseDirWatcher, FileMatchPattern, WatchDirNotFoundException, WatcherProvider}

import scala.concurrent.duration.FiniteDuration

object SmbSourceLogic extends Described {
  
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
  
  
  val password = TextParameterDescriptor(
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
      name = classOf[SmbSourceLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(CommonCategories.SOURCE, Category("File"), Category("SMB")),
      parameters = Seq(
        hostName,
        shareName,
        domainName,
        loginName,
        password,
        TailinSourceLogic.filePattern,
        TailinSourceLogic.readMode,
        TailinSourceLogic.fieldName,
        TailinSourceLogic.encoding,
        TailinSourceLogic.rotationPattern,
        TailinSourceLogic.onComplete,
        TailinSourceLogic.renameOnComplete_string,
        TailinSourceLogic.renameOnComplete_append,
      ),
      icon = Icon.fromClass(classOf[SmbSourceLogic])
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.tailin.SmbSourceLogic",
      Locale.ENGLISH, Locale.GERMAN
    ) ++ CATEGORY_LOCALIZATION
  )
  
  
  object Configuration {
    import scala.language.implicitConversions
    private implicit def convertDuration(duration: Duration): FiniteDuration = scala.concurrent.duration.Duration.fromNanos(duration.toNanos)
    
    def apply(config: Config): Configuration = {
      val sub = config.getConfig("keyscore.smb-source")
      new Configuration(
        persistenceFile = sub.getString("persistence-file"),
        pollInterval = sub.getDuration("poll-interval"),
        connectRetryInterval = sub.getDuration("connect-retry-interval"),
        baseDirNotFoundRetryInterval = sub.getDuration("base-dir-not-found-retry-interval"),
        readBufferSize = sub.getMemorySize("read-buffer-size").toBytes.toInt,
      )
    }
  }
  case class Configuration(persistenceFile: String, pollInterval: FiniteDuration, connectRetryInterval: FiniteDuration, baseDirNotFoundRetryInterval: FiniteDuration, readBufferSize: Int)
}

class SmbSourceLogic(parameters: LogicParameters, shape: SourceShape[Dataset]) extends SourceLogic(parameters, shape) {
  
  private val config = SmbSourceLogic.Configuration(system.settings.config)
  
  private var hostName = SmbSourceLogic.hostName.defaultValue
  private var shareName = SmbSourceLogic.shareName.defaultValue
  private var domainName = SmbSourceLogic.domainName.defaultValue
  private var loginName = SmbSourceLogic.loginName.defaultValue
  private var password = SmbSourceLogic.password.defaultValue
  
  //like local TailinSourceLogic
  private var filePattern = TailinSourceLogic.filePattern.defaultValue
  private var readMode = ReadMode.LINE.toString
  private var fieldName = TailinSourceLogic.fieldName.defaultValue
  private var encoding = StandardCharsets.UTF_8.toString
  private var rotationPattern = TailinSourceLogic.rotationPattern.defaultValue
  private var onComplete = TailinSourceLogic.OnComplete.None.toString
  private var renameOnComplete_string = TailinSourceLogic.renameOnComplete_string.defaultValue
  private var renameOnComplete_append = TailinSourceLogic.RenameAppend.After.toString
  
  
  private val persistenceFile = config.persistenceFile
  
  
  
  
  var connection: Connection = null
  var share: DiskShare = null
  
  
  var dirWatcher: BaseDirWatcher = _
  
  var sendBuffer: SendBuffer = null
  var readPersistence: ReadPersistence = null
  
  var baseDirString: String = null
  var smbFilePattern: FileMatchPattern = null
  
  val bufferSize = config.readBufferSize
  
  
  var readSchedulerProvider: WatcherProvider = null
  var baseDir: SmbDir = null
  
  
  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
  }
  
  override def configure(configuration: Configuration): Unit = {
    hostName = configuration.getValueOrDefault(SmbSourceLogic.hostName, hostName)
		shareName = configuration.getValueOrDefault(SmbSourceLogic.shareName, shareName)
		domainName = configuration.getValueOrDefault(SmbSourceLogic.domainName, domainName)
		loginName = configuration.getValueOrDefault(SmbSourceLogic.loginName, loginName)
		password = configuration.getValueOrDefault(SmbSourceLogic.password, password)
    
		//like local TailinSourceLogic
    filePattern = configuration.getValueOrDefault(TailinSourceLogic.filePattern, filePattern)
    readMode = configuration.getValueOrDefault(TailinSourceLogic.readMode, readMode)
    fieldName = configuration.getValueOrDefault(TailinSourceLogic.fieldName, fieldName)
    encoding = configuration.getValueOrDefault(TailinSourceLogic.encoding, encoding)
    rotationPattern = configuration.getValueOrDefault(TailinSourceLogic.rotationPattern, rotationPattern)
    onComplete = configuration.getValueOrDefault(TailinSourceLogic.onComplete, onComplete)
    renameOnComplete_string = configuration.getValueOrDefault(TailinSourceLogic.renameOnComplete_string, renameOnComplete_string)
    renameOnComplete_append = configuration.getValueOrDefault(TailinSourceLogic.renameOnComplete_append, renameOnComplete_append)
    
    
    val _persistenceFile = new File(persistenceFile)
    _persistenceFile.createNewFile()
    
    for (i <- 1 to 50) {
      if (_persistenceFile.exists == false) {
        Thread.sleep(100)
      }
    }
    
    readPersistence = new ReadPersistence(completedPersistence = new RAMPersistenceContext(),
                                          committedPersistence = new FilePersistenceContext(_persistenceFile))
    
    
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
    
    
    
    var filePatternWithoutLeadingSlashes = filePattern
    while (filePatternWithoutLeadingSlashes.startsWith("/") || filePatternWithoutLeadingSlashes.startsWith("\\")) {
      filePatternWithoutLeadingSlashes = filePatternWithoutLeadingSlashes.substring(1)
    }
    
    smbFilePattern = new FileMatchPattern(s"\\\\$hostName\\$shareName\\$filePatternWithoutLeadingSlashes",
                                        s"\\\\$hostName\\$shareName\\$exclusionPattern")
    
    
    
    
    //start the first DirWatcher at the deepest level where no new sibling-directories can match the filePattern in the future
    FileMatchPattern.extractInvariableDir(filePatternWithoutLeadingSlashes) match {
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
    val fileReaderProvider = new FileReaderProvider(rotationPattern, bufferSize, Charset.forName(encoding), ReadMode.withName(readMode), fileCompleteActions)
    
    val fileReaderManager = new FileReaderManager(fileReaderProvider, readSchedule, readPersistence, rotationPattern)
    sendBuffer = new SendBuffer(fileReaderManager, readPersistence)
    
    readSchedulerProvider = new WatcherProvider(readSchedule, rotationPattern, readPersistence)
  }
  
  
  
  override def postStop(): Unit = {
    
    if (dirWatcher != null) {
      dirWatcher.tearDown()
    }
    
    if (share != null) {
      share.close()
    }
    
    if (connection != null) {
      connection.close()
    }
  }
  
  
  case class Poll()
  
  override def onTimer(timerKey: Any): Unit = {
    
    timerKey match {
      case Poll() => onPull()
    }
  }
  
  
  private def doPush(): Unit = {
    val fileReadDataOpt = sendBuffer.getNextElement
    
    fileReadDataOpt match {
      case None =>
        scheduleOnce(timerKey = Poll(), config.pollInterval)
      case Some(fileReadData) =>
      
      
      val outData = Dataset(
        records = List(Record(
          fields = List(
            Field(fieldName, TextValue(fileReadData.string)),
            Field("smb.path", TextValue(fileReadData.baseFile.absolutePath)),
            Field("smb.write_timestamp", NumberValue(fileReadData.writeTimestamp)),
          )
        ))
      )
      
      
      push(out, outData)
      readPersistence.commitRead(fileReadData.baseFile, FileReadRecord(fileReadData.readEndPos, fileReadData.writeTimestamp, fileReadData.newerFilesWithSharedLastModified))
    }
  }
  
  
  
  override def onPull(): Unit = {
    
    if (connection == null || connection.isConnected == false) {
      setupConnection()
      return
    }
    
    
    if (share == null || share.isConnected == false) {
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
      }
      catch {
        case ex: WatchDirNotFoundException =>
          setupSmbBaseDir()
          return
      }
      scheduleOnce(timerKey = Poll(), config.pollInterval)
    }
  }
  
  
  private def setupConnection(): Unit = {
    val client = new SMBClient()
    try {
      connection = client.connect(hostName)
    }
    catch {
      case ex: Throwable =>
        log.error("Could not connect: '{}'. Retrying in {}.", ex, config.connectRetryInterval)
        scheduleOnce(Poll(), config.connectRetryInterval)
        return
    }
    
    onPull()
  }
  
  
  private def setupShare(): Unit = {
    val authContext = new AuthenticationContext(loginName, password.toCharArray, domainName)
    
    var session: Session = null
    try {
      session = connection.authenticate(authContext)
    }
    catch {
      case ex: SMBApiException =>
        if (ex.getStatus == NtStatus.STATUS_LOGON_FAILURE) {
          log.error("Could not authenticate for user '{}'.", loginName)
          fail(out, ex)
          return
        }
    }
    
    try {
      share = session.connectShare(shareName).asInstanceOf[DiskShare]
    }
    catch {
      case ex: SMBApiException =>
        if (ex.getStatus == NtStatus.STATUS_BAD_NETWORK_NAME) {
          log.error("Could not find share with name '{}'", shareName)
          fail(out, ex)
          return
        }
    }
    
    onPull()
  }
  
  
  private def setupSmbBaseDir(): Unit = {
    
    try {
      baseDir = new SmbDir(baseDirString, share)
    }
    catch {
      case ex: SMBApiException =>
        if (ex.getStatus == NtStatus.STATUS_OBJECT_NAME_NOT_FOUND
         || ex.getStatus == NtStatus.STATUS_OBJECT_PATH_NOT_FOUND) { //occurs when not even the parent directory exists
          log.error(s"The determined base directory '{}' does not exist. Retrying in {}.", baseDirString, config.baseDirNotFoundRetryInterval)
          scheduleOnce(Poll(), config.baseDirNotFoundRetryInterval)
          return
        }
        else throw ex
    }
    
    
    onPull()
  }
  
  
  private def setupDirWatcher(): Unit = {
    
    dirWatcher = readSchedulerProvider.createDirWatcher(baseDir, smbFilePattern)
    
    onPull()
  }
}
