package io.logbee.keyscore.pipeline.contrib.tailin.watch

import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Paths

import io.logbee.keyscore.pipeline.contrib.tailin.file.DirHandle
import io.logbee.keyscore.pipeline.contrib.tailin.file.FileHandle


object DirWatcherPattern {
  
  def extractInvariableDir(filePattern: String): Option[String] = {
    
    val variableIndex = findFirstVariableIndex(filePattern)
    
    var invariableString = filePattern
    if (variableIndex != -1) { //if the filePattern contains a variable part
      invariableString = filePattern.substring(0, variableIndex)
    }
    
    
    val lastSlashIndex = invariableString.lastIndexOf(File.separator) + 1
    
    if (lastSlashIndex == -1) {
      None
    }
    else {
      var result = invariableString.substring(0, lastSlashIndex)
      if (result.endsWith("/") == false) {
        result += "/"
      }
      Some(result)
    }
  }
  
  
  def findFirstVariableIndex(filePattern: String): Int = {
    
    def ifMinusOneThenMax(index: Integer): Integer = {
      if (index > -1) index else Integer.MAX_VALUE
    }
    
    var asteriskIndex = ifMinusOneThenMax(filePattern.indexOf('*'))
		var questionmarkIndex = ifMinusOneThenMax(filePattern.indexOf('?'))
  	var bracketIndex = ifMinusOneThenMax(filePattern.indexOf('[')) //glob supports patterns like [ab] and [a-z]
    
  	var firstIndex = Math.min(Math.min(asteriskIndex, questionmarkIndex), bracketIndex) //find smallest
  	
  	if (firstIndex == Integer.MAX_VALUE) -1 else firstIndex //if none were found, return -1
  }
  
  
  
  /**
   * Returns a transformed version of the given path that looks like a Unix-path.
   * 
   * This is useful for the Java PathMatcher API that can't work for example with SMB-paths.
   */
  private def getUnixLikePath(fullFilePattern: String): String = { //TODO try to make Java do this conversion by throwing it into a Path or something (this current naive approach fails for example for \-escaped spaces and such in UNIX-like paths
    fullFilePattern
      .replace("\\\\", "/") //replace "\\" at the start of SMB-paths with just a /
      .replace('\\', '/') //replace '\' as in Windows-like paths with '/'
  }
}


class DirWatcherPattern(fullFilePattern: String) {
  
  val filePattern = {
    if (fullFilePattern.endsWith(File.separator)) {
      //if the user specifies a directory, assume that they want all files in the directory
      //can't do this by just checking, if the specified path is a directory, because the same path could in the future lead to a file
      fullFilePattern + "*"
    } else {
      fullFilePattern
    }
  }
  
  
  private val fileMatcher = FileSystems.getDefault.getPathMatcher("glob:" + DirWatcherPattern.getUnixLikePath(filePattern))
  
  def matches(file: FileHandle): Boolean = {
    val path = Paths.get(DirWatcherPattern.getUnixLikePath(file.absolutePath))
    fileMatcher.matches(path)
  }
  
  def isSuperDir(dir: DirHandle): Boolean = { //TODO
    
    var tmpPattern = filePattern
    while (tmpPattern.endsWith("/")) { //remove trailing slashes, as PathMatcher doesn't work with a slash at the end
      tmpPattern = tmpPattern.substring(0, tmpPattern.length - 1)
    }
    
    
    var sections = tmpPattern.split('/').filterNot(_.isEmpty)
    if (sections.last.contains("**") == false) {
      sections = sections.dropRight(1) //drop the file-part (unnecessary to check and causes problems when it's "*")
    }
    
    val dirPath = Paths.get(dir.absolutePath)
    for (i <- 0 to sections.length) {
      val dirPattern = sections.foldLeft("")((a: String, b: String) => a + "/" + b)
      
      val matcher = FileSystems.getDefault.getPathMatcher("glob:" + DirWatcherPattern.getUnixLikePath(dirPattern))
      if (matcher.matches(dirPath)) {
        return true
      }
      
      sections = sections.dropRight(1)
    }
    
    false
  }
}
