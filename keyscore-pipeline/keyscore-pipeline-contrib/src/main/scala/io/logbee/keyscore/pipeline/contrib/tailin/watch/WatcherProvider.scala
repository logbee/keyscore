package io.logbee.keyscore.pipeline.contrib.tailin.watch

import io.logbee.keyscore.pipeline.contrib.tailin.file.FileHandle


trait WatcherProvider[T] {
  
	def createDirWatcher(dirPath: T, matchPattern: DirWatcherPattern): DirWatcher
  
	def createFileEventHandler(file: FileHandle): FileEventHandler
}
