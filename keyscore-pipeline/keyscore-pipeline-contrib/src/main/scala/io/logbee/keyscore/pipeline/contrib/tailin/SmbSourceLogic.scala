package io.logbee.keyscore.pipeline.contrib.tailin

import java.io.File
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.EnumSet

import scala.concurrent.duration.DurationInt

import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.connection.Connection
import com.hierynomus.smbj.share.DiskShare

import akka.stream.SourceShape
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
import io.logbee.keyscore.model.descriptor.Category
import io.logbee.keyscore.model.descriptor.Descriptor
import io.logbee.keyscore.model.descriptor.ParameterInfo
import io.logbee.keyscore.model.descriptor.SourceDescriptor
import io.logbee.keyscore.model.descriptor.StringValidator
import io.logbee.keyscore.model.descriptor.TextParameterDescriptor
import io.logbee.keyscore.model.localization.Locale
import io.logbee.keyscore.model.localization.Localization
import io.logbee.keyscore.model.localization.TextRef
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.LogicParameters
import io.logbee.keyscore.pipeline.api.SourceLogic
import io.logbee.keyscore.pipeline.contrib.CommonCategories
import io.logbee.keyscore.pipeline.contrib.CommonCategories.CATEGORY_LOCALIZATION
import io.logbee.keyscore.pipeline.contrib.tailin.file.SmbDir
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
        TailinSourceLogic.encoding,
        TailinSourceLogic.rotationPattern,
        TailinSourceLogic.fieldName,
      ),
      icon = Icon.fromClass(classOf[SmbSourceLogic])
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.tailin.SmbSourceLogic",
      Locale.ENGLISH, Locale.GERMAN
    ) ++ CATEGORY_LOCALIZATION
  )
}

class SmbSourceLogic(parameters: LogicParameters, shape: SourceShape[Dataset]) extends SourceLogic(parameters, shape) {
  
  private var hostName = SmbSourceLogic.hostName.defaultValue
  private var shareName = SmbSourceLogic.shareName.defaultValue
  private var domainName = SmbSourceLogic.domainName.defaultValue
  private var loginName = SmbSourceLogic.loginName.defaultValue
  private var password = SmbSourceLogic.password.defaultValue
  
  //like local TailinSourceLogic
  private var filePattern = TailinSourceLogic.filePattern.defaultValue
  private var readMode = ReadMode.LINE.toString
  private var encoding = StandardCharsets.UTF_8.toString
  private var rotationPattern = TailinSourceLogic.rotationPattern.defaultValue
  private var fieldName = TailinSourceLogic.fieldName.defaultValue
  
  //not exposed in UI
  private var persistenceFile = TailinSourceLogic.persistenceFile.defaultValue
  
  
  
  
  var connection: Connection = null
  var share: DiskShare = null
  
  
  var dirWatcher: BaseDirWatcher = _
  
  var sendBuffer: SendBuffer = null
  var readPersistence: ReadPersistence = null
  
  def initialize(configuration: Configuration): Unit = {
    configure(configuration)
  }
  
  def configure(configuration: Configuration): Unit = {
    hostName = configuration.getValueOrDefault(SmbSourceLogic.hostName, hostName)
		shareName = configuration.getValueOrDefault(SmbSourceLogic.shareName, shareName)
		domainName = configuration.getValueOrDefault(SmbSourceLogic.domainName, domainName)
		loginName = configuration.getValueOrDefault(SmbSourceLogic.loginName, loginName)
		password = configuration.getValueOrDefault(SmbSourceLogic.password, password)
    
		//like local TailinSourceLogic
    filePattern = configuration.getValueOrDefault(TailinSourceLogic.filePattern, filePattern)
    readMode = configuration.getValueOrDefault(TailinSourceLogic.readMode, readMode)
    encoding = configuration.getValueOrDefault(TailinSourceLogic.encoding, encoding)
    rotationPattern = configuration.getValueOrDefault(TailinSourceLogic.rotationPattern, rotationPattern)
    fieldName = configuration.getValueOrDefault(TailinSourceLogic.fieldName, fieldName)
    persistenceFile = configuration.getValueOrDefault(TailinSourceLogic.persistenceFile, persistenceFile)
    
    
    var filePatternWithoutLeadingSlashes = filePattern
    while (filePatternWithoutLeadingSlashes.startsWith("/") || filePatternWithoutLeadingSlashes.startsWith("\\")) {
      filePatternWithoutLeadingSlashes = filePatternWithoutLeadingSlashes.substring(1)
    }
    
    var baseDir = FileMatchPattern.extractInvariableDir(filePatternWithoutLeadingSlashes) //start the first DirWatcher at the deepest level where no new sibling-directories can match the filePattern in the future
    baseDir match {
      case None =>
        log.warning("Could not parse the specified file pattern or could not find suitable parent directory to observe.")
        return
      case Some(baseDir: String) =>
        
        val _persistenceFile = new File(persistenceFile)
        _persistenceFile.createNewFile()
        
        for (i <- 1 to 50) {
          if (_persistenceFile.exists == false) {
            Thread.sleep(100)
          }
        }
        
        
        readPersistence = new ReadPersistence(completedPersistence = new RAMPersistenceContext(),
                                              committedPersistence = new FilePersistenceContext(_persistenceFile))
        
        val bufferSize = 1024
        
        val readSchedule = new ReadSchedule()
        val fileReaderProvider = new FileReaderProvider(rotationPattern, bufferSize, Charset.forName(encoding), ReadMode.withName(readMode))
        
        val fileReaderManager = new FileReaderManager(fileReaderProvider, readSchedule, readPersistence, rotationPattern)
        sendBuffer = new SendBuffer(fileReaderManager, readPersistence)
        
        val readSchedulerProvider = new WatcherProvider(readSchedule, rotationPattern, readPersistence)
        
        
        val client = new SMBClient()
        connection = client.connect(hostName)
        val authContext = new AuthenticationContext(loginName, password.toCharArray, "") //TODO domain
        val session = connection.authenticate(authContext)
        
        // Connect to Share
        share = session.connectShare(shareName).asInstanceOf[DiskShare]
        
        val dir = share.openDirectory(
            baseDir,
            EnumSet.of(AccessMask.GENERIC_READ),
            null,
            SMB2ShareAccess.ALL,
            SMB2CreateDisposition.FILE_OPEN,
            null
          )
        
        
        val smbFilePatternString = "\\\\" + hostName + "\\" + shareName + "\\" + filePatternWithoutLeadingSlashes
        dirWatcher = readSchedulerProvider.createDirWatcher(new SmbDir(dir), new FileMatchPattern(smbFilePatternString))
    }
  }
  
  
  
  
  override def onTimer(timerKey: Any) {
    dirWatcher.processChanges()
    
    if (!sendBuffer.isEmpty) {
      doPush()
    }
    else {
      scheduleOnce(timerKey = "poll", 1.second)
    }
  }
  
  
  private def doPush() {
    val fileReadDataOpt = sendBuffer.getNextElement
    
    fileReadDataOpt match {
      case None =>
        scheduleOnce(timerKey = "poll", 1.second)
      case Some(fileReadData) =>
      
      
      val outData = Dataset(
        metadata = MetaData(
          Label("io.logbee.keyscore.pipeline.contrib.tailin.source.BASE_FILE", TextValue(fileReadData.baseFile.absolutePath)),
          Label("io.logbee.keyscore.pipeline.contrib.tailin.source.WRITE_TIMESTAMP", NumberValue(fileReadData.writeTimestamp)),
        ),
        records = List(Record(
          fields = List(Field(
            fieldName,
            TextValue(fileReadData.string)
          ))
        ))
      )
      
      
      push(out, outData)
      readPersistence.commitRead(fileReadData.baseFile, FileReadRecord(fileReadData.readEndPos, fileReadData.writeTimestamp, fileReadData.newerFilesWithSharedLastModified))
    }
  }
  
  
  
  def onPull(): Unit = {
    
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
    
    if (share != null) {
      share.close()
    }
    
    if (connection != null) {
      connection.close()
    }
  }
}
