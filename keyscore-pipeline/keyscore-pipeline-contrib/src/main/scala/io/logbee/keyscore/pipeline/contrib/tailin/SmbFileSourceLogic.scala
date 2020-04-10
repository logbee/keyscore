package io.logbee.keyscore.pipeline.contrib.tailin

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
import io.logbee.keyscore.model.configuration.{Configuration, ParameterSet}
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.pipeline.api.LogicParameters
import io.logbee.keyscore.pipeline.commons.CommonCategories
import io.logbee.keyscore.pipeline.commons.CommonCategories.CATEGORY_LOCALIZATION
import io.logbee.keyscore.pipeline.contrib.tailin.FileSourceLogicBase.Poll
import io.logbee.keyscore.pipeline.contrib.tailin.file.DirNotOpenableException
import io.logbee.keyscore.pipeline.contrib.tailin.file.smb.{SmbDir, SmbFile}
import io.logbee.keyscore.pipeline.contrib.tailin.watch.{BaseDirWatcher, FileMatchPattern, WatchDirNotFoundException}

import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success}

object SmbFileSourceLogic extends Described {
  import io.logbee.keyscore.model.util.ToOption.T2OptionT
  
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

  val enableAuth = BooleanParameterDescriptor(
    ref = "source.smb.enableAuth",
    info = ParameterInfo(
      displayName = TextRef("enableAuth.displayName"),
      description = TextRef("enableAuth.description")
    ),
    defaultValue = false
  )

  val groupCondition = BooleanParameterCondition(
    parameter = enableAuth.ref
  )

  val authGroup = ParameterGroupDescriptor(
    ref = "source.smb.authGroup",
    info = ParameterInfo(
      displayName = TextRef("authGroup.displayName"),
      description = TextRef("authGroup.description")
    ),
    condition = groupCondition,
    parameters = Seq(loginName,password)
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
        enableAuth,
        authGroup,
        FileSourceLogicBase.filePattern,
        FileSourceLogicBase.readMode,
        FileSourceLogicBase.firstLinePattern,
        FileSourceLogicBase.lastLinePattern,
        FileSourceLogicBase.fieldName,
        FileSourceLogicBase.encoding,
        FileSourceLogicBase.rotationPattern,
        FileSourceLogicBase.postReadFileAction,
        FileSourceLogicBase.renamePostReadFileAction_string,
        FileSourceLogicBase.renamePostReadFileAction_append,
        FileSourceLogicBase.persistenceEnabled,
      ),
      icon = Icon.fromClass(classOf[SmbFileSourceLogic]),
      maturity = Maturity.Development
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.tailin.SmbFileSourceLogic",
      Locale.ENGLISH, Locale.GERMAN
    ) ++ FileSourceLogicBase.LOCALIZATION ++ CATEGORY_LOCALIZATION
  )

  object Configuration {
    import scala.language.implicitConversions
    private implicit def convertDuration(duration: Duration): FiniteDuration = scala.concurrent.duration.Duration.fromNanos(duration.toNanos)
    
    def apply(config: Config): Configuration = {
      val sub = config.getConfig("keyscore.smb-source")
      new Configuration(
        filePersistenceConfig = sub.getConfig("file-persistence-context"),
        pollInterval = sub.getDuration("poll-interval"),
        pollTimeout = sub.getDuration("poll-error-timeout"),
        connectRetryInterval = sub.getDuration("connect-retry-interval"),
        baseDirNotFoundRetryInterval = sub.getDuration("base-dir-not-found-retry-interval"),
        readBufferSize = sub.getMemorySize("read-buffer-size").toBytes.toInt,
      )
    }
  }
  case class Configuration(filePersistenceConfig: Config, pollInterval: FiniteDuration, pollTimeout: Duration, connectRetryInterval: FiniteDuration, baseDirNotFoundRetryInterval: FiniteDuration, readBufferSize: Int)
}

class SmbFileSourceLogic(parameters: LogicParameters, shape: SourceShape[Dataset]) extends FileSourceLogicBase[SmbDir, SmbFile](parameters, shape) {
  
  private val config = SmbFileSourceLogic.Configuration(system.settings.config)
  
  
  override val pollInterval: FiniteDuration = config.pollInterval
  override val readBufferSize: Int = config.readBufferSize
  override val filePersistenceConfig: Config = config.filePersistenceConfig
  override val processChangesErrorTimeout: Duration = config.pollTimeout
  override val persistenceFileIdentifier: String = classOf[SmbFileSourceLogic].getSimpleName
  
  private var hostName = SmbFileSourceLogic.hostName.defaultValue
  private var shareName = SmbFileSourceLogic.shareName.defaultValue
  private var domainName = SmbFileSourceLogic.domainName.defaultValue
  private var loginName = SmbFileSourceLogic.loginName.defaultValue
  private var password = SmbFileSourceLogic.password.defaultValue
  private var authenticationEnabled = SmbFileSourceLogic.enableAuth.defaultValue
  
  
  var connection: Connection = _
  var share: DiskShare = _
  
  var dirWatcher: BaseDirWatcher = _
  
  var baseDirString: String = _
  var smbFilePattern: FileMatchPattern[SmbDir, SmbFile] = _
  
  var baseDir: SmbDir = _
  
  
  override def configure(configuration: Configuration): Unit = {
    super.configure(configuration)
    
    hostName = configuration.getValueOrDefault(SmbFileSourceLogic.hostName, hostName)
    shareName = configuration.getValueOrDefault(SmbFileSourceLogic.shareName, shareName)
    domainName = configuration.getValueOrDefault(SmbFileSourceLogic.domainName, domainName)
    
    authenticationEnabled = configuration.getValueOrDefault(SmbFileSourceLogic.enableAuth, authenticationEnabled)
    
    configuration.findValue(SmbFileSourceLogic.authGroup) match {
      
      case Some(configuration: ParameterSet) if authenticationEnabled =>
        loginName = configuration.getValueOrDefault(SmbFileSourceLogic.loginName, loginName)
        password = configuration.getValueOrDefault(SmbFileSourceLogic.password, password)
      
      case _ =>
        loginName = ""
        password = ""
    }
    
    
    var filePatternWithoutLeadingSlashes = filePattern.replace('/', '\\') //transform any forward slashes to backward slashes -> SMB/Windows paths cannot contain forward slashes, so this should be safe
    while (filePatternWithoutLeadingSlashes.startsWith("/") || filePatternWithoutLeadingSlashes.startsWith("\\")) {
      filePatternWithoutLeadingSlashes = filePatternWithoutLeadingSlashes.substring(1)
    }
    
    
    val exclusionPattern = "" //Currently unused, may be exposed in the UI in future
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
  }
  
  override def onTimer(timerKey: Any): Unit = {
    
    timerKey match {
      case Poll => onPull()
    }
  }
  
  
  override def onPull(): Unit = {
    
    if (connection == null || !connection.isConnected) {
      setupConnection()
      return
    }
    
    if (share == null || !share.isConnected) {
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
      dirWatcher.processChanges() match {
        case Success(_) =>
        case Failure(_: WatchDirNotFoundException) => setupSmbBaseDir()
        case Failure(ex) => fail(out, ex)
      }
      
      scheduleOnce(timerKey = Poll, config.pollInterval)
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
        ||  ex.getStatus == NtStatus.STATUS_OBJECT_PATH_NOT_FOUND) { //occurs when not even the parent directory exists
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
      dirWatcher = watcherProvider.createDirWatcher(baseDir, smbFilePattern)
      onPull()
    }
    catch {
      case ex: DirNotOpenableException =>
        log.error(ex, s"The determined base directory '$baseDirString' does not exist. Retrying in ${config.baseDirNotFoundRetryInterval}.")
        scheduleOnce(Poll, config.baseDirNotFoundRetryInterval)
    }
  }
  
  
  override def postStop(): Unit = {
    super.postStop()
    
    if (share != null) {
      share.close()
    }
    
    if (connection != null) {
      connection.close()
    }
  }
}
