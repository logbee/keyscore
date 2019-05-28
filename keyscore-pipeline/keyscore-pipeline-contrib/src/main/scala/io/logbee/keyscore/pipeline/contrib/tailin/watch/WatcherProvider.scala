package io.logbee.keyscore.pipeline.contrib.tailin.watch

trait WatcherProvider[T, S] {
  
	def createDirWatcher(watchDir: T, matchPattern: DirWatcherPattern): DirWatcher
  
	def createFileEventHandler(file: S): FileEventHandler
}
