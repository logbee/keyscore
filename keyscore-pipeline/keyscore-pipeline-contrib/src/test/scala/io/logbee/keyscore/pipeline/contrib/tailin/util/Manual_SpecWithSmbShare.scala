package io.logbee.keyscore.pipeline.contrib.tailin.util

import java.nio.ByteBuffer
import java.util.EnumSet

import org.scalatest.FreeSpec

import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.msfscc.FileAttributes
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2CreateOptions
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.share.Directory
import com.hierynomus.smbj.share.DiskShare

import io.logbee.keyscore.pipeline.contrib.tailin.file.smb.SmbFile

/**
 * Semi-automatic test. Requires user-interaction and an SMB share.
 */
//no JUnitRunner, so that it doesn't get executed automatically by Gradle
class Manual_SpecWithSmbShare extends FreeSpec {
  val client = new SMBClient()
  
  val hostName = scala.io.StdIn.readLine("Host name: ")
  val userName = scala.io.StdIn.readLine("User name: ")
  val password = scala.io.StdIn.readLine("Password: ")
  println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n") //hide password from view
  val domain = scala.io.StdIn.readLine("Domain: ")
  val shareName = scala.io.StdIn.readLine("Share name: ")
  
  
  
  def withShare(testCode: DiskShare => Any) = {
    val connection = client.connect(hostName)
    try {
      val authContext = new AuthenticationContext(userName, password.toCharArray, domain)
      val session = connection.authenticate(authContext)
      
      // Connect to Share
      val share = session.connectShare(shareName).asInstanceOf[DiskShare]
      
      try {
        testCode(share)
      }
      finally {
        if (share != null)
          share.close()
      }
    }
    finally {
      if (connection != null)
        connection.close()
    }
  }
  
  
  
  
  
  private def createFile(share: DiskShare, fileName: String, content: ByteBuffer): SmbFile = {
    
    val writeBuffer = content
    
    //the resulting array has 0s from the buffer's limit to the end, which we drop here
    val writeArray = writeBuffer.array.dropRight(writeBuffer.capacity - writeBuffer.limit)
    
    
    val actualSmbFile = share.openFile(
                          fileName,
                          EnumSet.of(AccessMask.GENERIC_ALL),
                          EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL),
                          SMB2ShareAccess.ALL,
                          SMB2CreateDisposition.FILE_CREATE,
                          EnumSet.noneOf(classOf[SMB2CreateOptions])
                        )
    
    actualSmbFile.write(writeArray, 0)
    
    new SmbFile(actualSmbFile)
  }
  
  
  
  def withSmbFile(share: DiskShare, fileName: String, content: ByteBuffer, testCode: SmbFile => Any) = {
    
    var smbFile: SmbFile = null
    
    try {
      smbFile = createFile(share, fileName, content)
      
      testCode(smbFile)
    }
    finally {
      println("rmFile: " + fileName)
      
      if (smbFile != null)
        smbFile.tearDown()
    }
  }
  
  
  
  
  
  private def createDir(share: DiskShare, dirName: String): Directory = {
    
    share.openDirectory(
      dirName,
      EnumSet.of(AccessMask.GENERIC_ALL),
      EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL),
      SMB2ShareAccess.ALL,
      SMB2CreateDisposition.FILE_CREATE,
      EnumSet.noneOf(classOf[SMB2CreateOptions])
    )
  }
  
  
  
  def withSmbDir(share: DiskShare, dirName: String, testCode: Directory => Any) = {
    
    var smbDir: Directory = null
    try {
      smbDir = createDir(share, dirName)
      
      testCode(smbDir)
    }
    finally { //TODO calling rmdir will interrupt file-deletion, because non-empty dir being deleted, therefore cause nothing to be deleted
      println("rmDir: " + dirName)
      
      if (smbDir != null) {
        smbDir.close() //FIXME closing this should theoretically remove it (but this doesn't work), unless it's only upon closing the share
      }
      
//      share.rmdir(dirName, true)
    }
  }
}
