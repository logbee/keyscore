package io.logbee.keyscore.pipeline.contrib.tailin.file.smb

import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SmbUtilSpec extends AnyFreeSpec with Matchers with MockFactory {
  
  "An SmbUtil should" - {
    "translate an absolute path to a relative path" in {
      
      val share_getSmbPath_toString = """\\hostname\share"""
      
      SmbUtil.relativePath("""\\hostname\share\relative\path\""", share_getSmbPath_toString) shouldEqual """relative\path\"""
      SmbUtil.relativePath("""\\hostname\share\""", share_getSmbPath_toString) shouldEqual ""
      SmbUtil.relativePath("""\\hostname\share""", share_getSmbPath_toString) shouldEqual ""
    }
    
    "translate a relative path to an absolute path" in {
      
      val share_getSmbPath_toString = """\\hostname\share"""
      
      SmbUtil.absolutePath("""relative\path\""", share_getSmbPath_toString) shouldEqual """\\hostname\share\relative\path\"""
      SmbUtil.absolutePath("", share_getSmbPath_toString) shouldEqual """\\hostname\share\"""
    }
    
    "join paths correctly" in {
      SmbUtil.joinPath("""\\hostname\share""",   """relative\path""") shouldEqual """\\hostname\share\relative\path"""
      SmbUtil.joinPath("""\\hostname\share""",  """\relative\path""") shouldEqual """\\hostname\share\relative\path"""
      SmbUtil.joinPath("""\\hostname\share\""",  """relative\path""") shouldEqual """\\hostname\share\relative\path"""
      SmbUtil.joinPath("""\\hostname\share\""", """\relative\path""") shouldEqual """\\hostname\share\relative\path"""
      
      SmbUtil.joinPath("""\\hostname""", """share\""", """\relative""", """path""") shouldEqual """\\hostname\share\relative\path"""
    }
  }
}
