package io.logbee.keyscore.pipeline.contrib.tailin.file.smb

import com.hierynomus.smbj.share.DiskShare
import org.slf4j.LoggerFactory

object SmbUtil {
  private lazy val log = LoggerFactory.getLogger(SmbUtil.getClass)
  
  /**
   * \\hostname\share\path\to\ -> path\to\
   */
  def relativePath(absolutePath: String)(implicit share: DiskShare): String = {
    relativePath(absolutePath, share.getSmbPath.toString)
  }
  
  /**
   * \\hostname\share\path\to\ -> path\to\
   */
  def relativePath(absolutePath: String, share_getSmbPath_toString: String): String = {
    if (absolutePath == share_getSmbPath_toString) { // absolutePath == \\hostname\share
      ""
    }
    else if (absolutePath.startsWith(share_getSmbPath_toString)) { // absolutePath == \\hostname\share\relative\path
      absolutePath.substring(share_getSmbPath_toString.length + 1)
    }
    else { // absolutePath == \relative\path
      absolutePath
    }
  }
  
  
  
  /**
   * path\to\ -> \\hostname\share\path\to\
   */
  def absolutePath(relativePath: String)(implicit share: DiskShare): String = {
    absolutePath(relativePath, share.getSmbPath.toString)
  }
  
  /**
   * path\to\ -> \\hostname\share\path\to\
   */
  def absolutePath(relativePath: String, share_getSmbPath_toString: String): String = {
    if (relativePath.startsWith(share_getSmbPath_toString) == false) {
      share_getSmbPath_toString + "\\" + relativePath
    }
    else {
      relativePath
    }
  }
  
  
  def joinPath(paths: String*): String = {
    paths.fold("") { (joinedPath, subPath) =>
      if (joinedPath.endsWith("\\") && subPath.startsWith("\\")) {
        joinedPath + subPath.substring(1)
      }
      else if (joinedPath.endsWith("\\") || subPath.startsWith("\\")) {
        joinedPath + subPath
      }
      else {
        joinedPath + "\\" + subPath
      }
    }
  }
}
