package io.logbee.keyscore.pipeline.contrib.tailin.util

import org.scalatest.FreeSpec

import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.share.DiskShare

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
}