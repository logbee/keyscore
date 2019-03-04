package io.logbee.keyscore.pipeline.contrib.tailin.watch

import java.io.File
import java.nio.file.Path
import java.nio.file.Paths


object DirWatcherPattern {
  
  def extractInvariableDir(filePattern: String): Path = {
    
    val variableIndex = findFirstVariableIndex(filePattern)
    
    var invariableString = filePattern
    if (variableIndex != -1) { //if the filePattern contains a variable part
      invariableString = filePattern.substring(0, variableIndex)
    }
    
    
    val lastSlashIndex = invariableString.lastIndexOf(File.separator) + 1
    
    if (lastSlashIndex == -1) {
      null
    }
    else {
      invariableString = invariableString.substring(0, lastSlashIndex)
      
      val invariablePathDir = Paths.get(invariableString)
      if (invariablePathDir.toFile.isDirectory) {
        invariablePathDir
      }
      else { //path specified by user doesn't exist or is completely malformed input
        //TODO it might not exist yet, but the user will probably want it monitored when it starts to exist later
        null
      }
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
  
  
  
  def removeFirstDirPrefixFromMatchPattern(matchPattern: String): String = {
    
    if (matchPattern.startsWith("**")) {
      matchPattern //return same pattern because infinite directories can be matched with **
    }
    else {
      val slashIndex = matchPattern.indexOf(File.separator)
      
      if (slashIndex == -1) { //no slash found
        //we're already at the last directory to need dirWatchers
        ""
      }
      else {
        matchPattern.substring(slashIndex + 1)
      }
    }
  }
  
  
  def apply(fullFilePattern: String, depth: Int = 0): DirWatcherPattern = {
    
    //TODO if it's a Windows file-path with '\' in it, change those to '/' -> mind that a '\' might also appear in a Unix path, so we can't just swap them out unconditionally
    
    val fullPattern: String = {
      if (fullFilePattern.endsWith(File.separator)) {
        //if the user specifies a directory, assume that they want all files in the directory
        //can't do this by just checking, if the specified path is a directory, because the same path could in the future lead to a file
        fullFilePattern + "*"
      } else {
        fullFilePattern
      }
    }
    
    
    val subDirPattern = {
      var tmpPattern = fullPattern
      for (i <- 0 to depth) {
        tmpPattern = removeFirstDirPrefixFromMatchPattern(tmpPattern)
      }
      
      //remove trailing file-part
      val lastSlashIndex = tmpPattern.lastIndexOf(File.separator)
      if (lastSlashIndex > -1) {
        tmpPattern.substring(0, lastSlashIndex)
      }
      else {
        
        val indexOfDoubleAsterisk = tmpPattern.lastIndexOf("**")
        if (indexOfDoubleAsterisk > -1) {
          tmpPattern = tmpPattern.substring(0, indexOfDoubleAsterisk) + "**"
        }
        
        tmpPattern
      }
    }
                         
    new DirWatcherPattern(fullPattern, subDirPattern, depth)
  }
}
case class DirWatcherPattern(fullFilePattern: String, subDirPattern: String, depth: Int)
