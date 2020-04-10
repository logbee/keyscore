package io.logbee.keyscore.pipeline.contrib.tailin

import java.nio.file.Paths
import java.time.Duration

import akka.stream.SourceShape
import com.typesafe.config.Config
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.pipeline.api.LogicParameters
import io.logbee.keyscore.pipeline.commons.CommonCategories
import io.logbee.keyscore.pipeline.commons.CommonCategories.CATEGORY_LOCALIZATION
import io.logbee.keyscore.pipeline.contrib.tailin.FileSourceLogicBase.Poll
import io.logbee.keyscore.pipeline.contrib.tailin.file.local.{LocalDir, LocalFile}
import io.logbee.keyscore.pipeline.contrib.tailin.watch.{BaseDirWatcher, FileMatchPattern}

import scala.concurrent.duration.FiniteDuration


object LocalFileSourceLogic extends Described {
  import io.logbee.keyscore.model.util.ToOption.T2OptionT

  override def describe = Descriptor(
    ref = "5a754cd3-e11d-4dfb-a484-a9f83cf3d795",
    describes = SourceDescriptor(
      name = classOf[LocalFileSourceLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(CommonCategories.SOURCE, Category("File")),
      parameters = Seq(
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
      icon = Icon.fromClass(classOf[LocalFileSourceLogic]),
      maturity = Maturity.Development
    ),
    localization = Localization.fromResourceBundle(
      bundleName = classOf[LocalFileSourceLogic].getName,
      Locale.ENGLISH, Locale.GERMAN
    ) ++ FileSourceLogicBase.LOCALIZATION ++ CATEGORY_LOCALIZATION
  )

  object Configuration {
    import scala.language.implicitConversions
    private implicit def convertDuration(duration: Duration): FiniteDuration = scala.concurrent.duration.Duration.fromNanos(duration.toNanos)
    
    def apply(config: Config): Configuration = {
      val sub = config.getConfig("keyscore.local-file-source")
      new Configuration(
        filePersistenceConfig = sub.getConfig("file-persistence-context"),
        pollInterval = sub.getDuration("poll-interval"),
        pollTimeout = sub.getDuration("poll-error-timeout"),
        readBufferSize = sub.getMemorySize("read-buffer-size").toBytes.toInt,
      )
    }
  }
  case class Configuration(filePersistenceConfig: Config, pollInterval: FiniteDuration, pollTimeout: Duration, readBufferSize: Int)
}

class LocalFileSourceLogic(parameters: LogicParameters, shape: SourceShape[Dataset]) extends FileSourceLogicBase[LocalDir, LocalFile](parameters, shape) {
  
  private val config = LocalFileSourceLogic.Configuration(system.settings.config)
  
  override val pollInterval: FiniteDuration = config.pollInterval
  override val readBufferSize: Int = config.readBufferSize
  override val filePersistenceConfig: Config = config.filePersistenceConfig
  override val processChangesErrorTimeout: Duration = config.pollTimeout
  override val persistenceFileIdentifier: String = classOf[LocalFileSourceLogic].getSimpleName
  
  var dirWatcher: BaseDirWatcher = _
  
  override def configure(configuration: Configuration): Unit = {
    super.configure(configuration)
    
    val invariableString = FileMatchPattern.extractInvariableDir(filePattern, java.io.File.separator) //start the first DirWatcher at the deepest level where no new sibling-directories can match the filePattern in the future
    if (invariableString.isEmpty
    || Paths.get(invariableString.get).toFile.isDirectory == false) {
        val message = "Could not parse the specified file pattern or could not find suitable parent directory to observe."
        log.error(message)
        fail(out, new IllegalArgumentException(message))
        return
    }
    
    val baseDir = Paths.get(invariableString.get)
    
    val exclusionPattern = "" //Currently unused, may be exposed in the UI in future
    val matchPattern = new FileMatchPattern[LocalDir, LocalFile](filePattern, exclusionPattern)
    
    dirWatcher = watcherProvider.createDirWatcher(LocalDir(baseDir), matchPattern)
  }
  
  
  override def onTimer(timerKey: Any): Unit = {
    dirWatcher.processChanges()
    
    if (!sendBuffer.isEmpty) {
      doPush()
    }
    else {
      scheduleOnce(Poll, config.pollInterval)
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
        scheduleOnce(Poll, config.pollInterval)
      }
    }
  }
}
