package io.logbee.keyscore.pipeline.contrib.tailin.watch

import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Paths

import io.logbee.keyscore.pipeline.contrib.tailin.file.DirHandle
import io.logbee.keyscore.pipeline.contrib.tailin.file.FileHandle
import org.slf4j.LoggerFactory


object FileMatchPattern {
  
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
  private def getUnixLikePath(fullFilePattern: String): String = {
    if (fullFilePattern.isEmpty) return fullFilePattern
    
    var _fullFilePattern = fullFilePattern
      .replace("\\\\", "/").replace("//", "/") //replace "\\" at the start of SMB-paths with just a /
      .replace('\\', '/') //replace '\' as in Windows-like paths with '/'
    
    if (_fullFilePattern.matches("[A-Z]:.*")) {
      _fullFilePattern = _fullFilePattern.substring(2) //cut off "C:" or similar from Windows paths
    }
    
    _fullFilePattern
  }
}


class FileMatchPattern(fullFilePattern: String, exclusionPattern: String = "") {
  import FileMatchPattern.getUnixLikePath
  
  private lazy val log = LoggerFactory.getLogger(classOf[FileMatchPattern])
  
  val filePattern = {
    getUnixLikePath(
      if (getUnixLikePath(fullFilePattern).endsWith("/")) {
        //if the user specifies a directory, assume that they want all files in the directory
        //can't do this by just checking, if the specified path is a directory, because the same path could in the future lead to a file
        fullFilePattern + "*"
      } else {
        fullFilePattern
      }
    )
  }
  
  
  private val fileMatcher = FileSystems.getDefault.getPathMatcher("glob:" + filePattern)
  private val exclusionMatcher = FileSystems.getDefault.getPathMatcher("glob:" + getUnixLikePath(exclusionPattern))
  
  def matches(file: FileHandle): Boolean = {
    val path = Paths.get(getUnixLikePath(file.absolutePath))
    val matches = fileMatcher.matches(path)
    if (exclusionMatcher.matches(path))
      return false
    
    log.debug("Matching '{}' against '{}' and it matches{}.", path, filePattern, if (matches) "" else " not")
    
    matches
  }
  
  
  def isSuperDir(dir: DirHandle): Boolean = {

    val matches = {
      var tmpPattern = filePattern
      while (tmpPattern.endsWith("/")) { //remove trailing slashes, as PathMatcher doesn't work with a slash at the end
        tmpPattern = tmpPattern.substring(0, tmpPattern.length - 1)
      }


      var sections = tmpPattern.split('/').filterNot(_.isEmpty)
      if (sections.last.contains("**") == false) {
        sections = sections.dropRight(1) //drop the file-part (unnecessary to check and causes problems when it's "*")
      }
      
      val dirPath = Paths.get(getUnixLikePath(dir.absolutePath))
      for (i <- 0 to sections.length) {
        val dirPattern = sections.foldLeft("")((a: String, b: String) => a + "/" + b)
        
        val matcher = FileSystems.getDefault.getPathMatcher("glob:" + getUnixLikePath(dirPattern))
        if (matcher.matches(dirPath)) {
          return true
        }

        sections = sections.dropRight(1)
      }

      false
    }
    
    log.debug("Checking if '{}' is a potential super-dir for files matched by '{}' and it is{}.", getUnixLikePath(dir.absolutePath), getUnixLikePath(filePattern), if (matches) "" else " not")
    
    matches
  }
}
