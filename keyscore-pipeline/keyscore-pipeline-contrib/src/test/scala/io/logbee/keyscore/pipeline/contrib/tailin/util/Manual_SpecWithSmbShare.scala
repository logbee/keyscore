package io.logbee.keyscore.pipeline.contrib.tailin.util

import java.nio.ByteBuffer
import java.util.EnumSet

import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.mserref.NtStatus
import com.hierynomus.msfscc.FileAttributes
import com.hierynomus.mssmb2.{SMB2CreateDisposition, SMB2CreateOptions, SMB2ShareAccess, SMBApiException}
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.share.DiskShare
import io.logbee.keyscore.pipeline.contrib.tailin.file.smb.{SmbDir, SmbFile, SmbUtil}
import io.logbee.keyscore.pipeline.contrib.tailin.file.smb.SmbDir.OpenSmbDir
import io.logbee.keyscore.pipeline.contrib.tailin.file.smb.SmbFile.OpenSmbFile
import org.scalatest.freespec.AnyFreeSpec

import scala.util.{Failure, Success}

/**
 * Semi-automatic test. Requires user-interaction and an SMB share.
 */
//no JUnitRunner, so that it doesn't get executed automatically by Gradle
class Manual_SpecWithSmbShare extends AnyFreeSpec {
  val client = new SMBClient()
  
  def env(name: String) = System.getenv("KEYSCORE_MANUAL_SMB_SPEC_" + name)
  
  val hostName =  env("HOST_NAME")
  val userName =  env("USER_NAME")
  val password =  env("PASSWORD")
  val domain =    env("DOMAIN")
  val shareName = env("SHARE_NAME")
  
  
  
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
  
  
  
  
  
  private def createFile(fileName: String, content: ByteBuffer)(implicit share: DiskShare): SmbFile = {
    
    val writeBuffer = content
    
    //the resulting array has 0s from the buffer's limit to the end, which we drop here
    val writeArray = writeBuffer.array.dropRight(writeBuffer.capacity - writeBuffer.limit())
    
    
    val actualSmbFile = share.openFile(
                          SmbUtil.relativePath(fileName),
                          EnumSet.of(AccessMask.GENERIC_ALL),
                          EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL),
                          SMB2ShareAccess.ALL,
                          SMB2CreateDisposition.FILE_CREATE,
                          EnumSet.noneOf(classOf[SMB2CreateOptions])
                        )
    actualSmbFile.write(writeArray, 0)
    actualSmbFile.close() //close the connection again (SmbFile opens its own connection)
    
    SmbFile(fileName, share)
  }
  
  
  
  def withSmbFile(fileName: String, content: ByteBuffer, testCode: SmbFile => Any)(implicit share: DiskShare) = {
    
    var smbFile: SmbFile = null
    
    try {
      smbFile = createFile(fileName, content)
      
      testCode(smbFile)
    }
    finally {
      try {
        share.rm(SmbUtil.relativePath(fileName))
      }
      catch {
        case ex: SMBApiException if ex.getStatus == NtStatus.STATUS_OBJECT_NAME_NOT_FOUND => //already deleted -> ignore
      }
    }
  }


  def withOpenSmbFile(fileName: String, content: ByteBuffer, testCode: OpenSmbFile => Any)(implicit share: DiskShare) = {

    var smbFile: SmbFile = null

    try {
      smbFile = createFile(fileName, content)
      
      smbFile.open {
        case Success(smbFile: OpenSmbFile) => testCode(smbFile)
        case Success(_) => fail()
        case Failure(ex) => throw ex
      }
    }
    finally {
      try {
        share.rm(SmbUtil.relativePath(fileName))
      }
      catch {
        case ex: SMBApiException if ex.getStatus == NtStatus.STATUS_OBJECT_NAME_NOT_FOUND => //already deleted -> ignore
      }
    }
  }
  
  
  
  
  
  protected def createDir(dirName: String)(implicit share: DiskShare): SmbDir = {
    
    val actualSmbDirectory = share.openDirectory(
      dirName,
      EnumSet.of(AccessMask.GENERIC_ALL),
      EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL),
      SMB2ShareAccess.ALL,
      SMB2CreateDisposition.FILE_CREATE,
      EnumSet.noneOf(classOf[SMB2CreateOptions])
    )
    actualSmbDirectory.close() //close the connection again (SmbDir opens its own connection)
    
    SmbDir(dirName, share)
  }
  
  
  
  def withSmbDir(dirName: String, testCode: SmbDir => Any)(implicit share: DiskShare) = {
    
    var smbDir: SmbDir = null
    try {
      smbDir = createDir(dirName)
      
      testCode(smbDir)
    }
    finally {
      share.rmdir(dirName, true)
    }
  }


  def withOpenSmbDir(dirName: String, testCode: OpenSmbDir => Any)(implicit share: DiskShare) = {
    var smbDir: SmbDir = null
    try {
      smbDir = createDir(dirName)
      
      smbDir.open {
        case Success(smbDir: OpenSmbDir) => testCode(smbDir)
        case Success(_) => fail()
        case Failure(ex) => throw ex
      }
    }
    finally {
      share.rmdir(dirName, true)
    }
  }
}
