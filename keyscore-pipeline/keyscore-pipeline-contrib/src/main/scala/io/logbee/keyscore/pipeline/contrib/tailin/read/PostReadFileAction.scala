package io.logbee.keyscore.pipeline.contrib.tailin.read

sealed trait PostReadFileAction

object PostReadFileAction {

  case object None extends PostReadFileAction
  case object Delete extends PostReadFileAction
  case object Rename extends PostReadFileAction

  def fromString(value: String): PostReadFileAction = value match {
    case "None" => None
    case "Delete" => Delete
    case "Rename" => Rename
    case _ => throw new IllegalArgumentException(s"Unknown PostReadFileAction: '$value'. Possible values are: [$None, $Delete, $Rename].")
  }
}
