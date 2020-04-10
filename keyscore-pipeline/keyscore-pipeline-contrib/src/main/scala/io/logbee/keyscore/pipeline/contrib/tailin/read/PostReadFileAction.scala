package io.logbee.keyscore.pipeline.contrib.tailin.read

import io.logbee.keyscore.pipeline.contrib.tailin.FileSourceLogicBase
import io.logbee.keyscore.pipeline.contrib.tailin.FileSourceLogicBase.RenameAppend
import io.logbee.keyscore.pipeline.contrib.tailin.file.FileHandle
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success}

sealed trait PostReadFileAction

object PostReadFileAction {
  private lazy val log = LoggerFactory.getLogger(classOf[PostReadFileAction])

  case object None extends PostReadFileAction
  case object Delete extends PostReadFileAction
  case object Rename extends PostReadFileAction

  def fromString(value: String): PostReadFileAction = value.toLowerCase match {
    case "" | "none" => None
    case "delete" => Delete
    case "rename" => Rename
    case _ => throw new IllegalArgumentException(s"Unknown PostReadFileAction: '$value'. Possible values are: [$None, $Delete, $Rename].")
  }
  
  
  type PostReadFileActionFunc = FileHandle => Unit
  
  
  def createFunc(postFileReadAction: PostReadFileAction, renamePostReadFileAction_append: RenameAppend, renamePostReadFileAction_string: String): PostReadFileActionFunc = {
    import FileSourceLogicBase.RenameAppend
    
    postFileReadAction match {
      case PostReadFileAction.None => noop
      
      case PostReadFileAction.Delete => delete
      
      case PostReadFileAction.Rename =>
       
        (renamePostReadFileAction_append, renamePostReadFileAction_string) match {
          case (RenameAppend.Before, prefix) if prefix.nonEmpty => renamePrepend(prefix)
          case (RenameAppend.After, suffix) if suffix.nonEmpty => renameAppend(suffix)
          case (_, _) => noop
        }
    }
  }
  
  private def noop(file: FileHandle): Unit = {}
  
  private def delete(file: FileHandle): Unit = {
    file.delete() match {
      case Success(_) => log.debug("Deleted file '{}'", file.absolutePath)
      case Failure(ex) => log.error(s"Could not delete file '$file'.", ex)
    }
  }
  
  private def renameAppend(suffix: String)(file: FileHandle): Unit = {
    file.open {
      case Success(openFile) => file.move(openFile.parent + openFile.name + suffix)
      case Failure(ex) => log.error(s"Could not rename file '$file'!", ex)
    }
  }
  
  private def renamePrepend(prefix: String)(file: FileHandle): Unit = {
    file.open {
      case Success(openFile) => file.move(openFile.parent + prefix + openFile.name)
      case Failure(ex) => log.error(s"Could not rename file '$file'!", ex)
    }
  }
}
