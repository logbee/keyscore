package io.logbee.keyscore.pipeline.contrib.tailin.read

case class FileReadRecord(previousReadPosition: Long, previousReadTimestamp: Long, newerFilesWithSharedLastModified: Int)
