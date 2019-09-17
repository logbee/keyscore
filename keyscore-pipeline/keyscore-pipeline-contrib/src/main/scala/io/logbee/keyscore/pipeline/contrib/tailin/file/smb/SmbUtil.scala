package io.logbee.keyscore.pipeline.contrib.tailin.file.smb

import com.hierynomus.smbj.share.DiskShare
import org.slf4j.LoggerFactory

object SmbUtil {
  private lazy val log = LoggerFactory.getLogger(SmbUtil.getClass)
  
  /**
   * \\hostname\share\path\to\ -> path\to\
   */
  def relativePath(absolutePath: String)(implicit share: DiskShare): String = {
    if (absolutePath.startsWith(share.getSmbPath.toString)) {
      absolutePath.substring(share.getSmbPath.toString.length + 1)
    }
    else {
      absolutePath
    }
  }
  
  
  /**
   * path\to\ -> \\hostname\share\path\to\
   */
  def absolutePath(relativePath: String)(implicit share: DiskShare): String = {
    if (relativePath.startsWith(share.getSmbPath.toString) == false) {
      share.getSmbPath.toString + relativePath
    }
    else {
      relativePath
    }
  }
}
