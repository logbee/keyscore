package io.logbee.keyscore.pipeline.contrib.tailin.file

import io.logbee.keyscore.pipeline.contrib.tailin.util.SpecWithTempDir
import org.scalatest.Matchers
import io.logbee.keyscore.pipeline.contrib.tailin.util.TestUtil
import org.scalatest.BeforeAndAfterAll
import java.nio.ByteBuffer

class LocalFileSpec extends SpecWithTempDir with Matchers with BeforeAndAfterAll {
  
  
  def withLocalFile(testCode: (File, java.io.File) => Any) = {
    val name = "localFile.txt"
    val actualFile = TestUtil.createFile(watchDir, name, "fileContent")
    
    val localFile = new LocalFile(actualFile)
    
    testCode(localFile, actualFile)
  }
  
  
  
  "A LocalFile should" - {
    "return its name" in withLocalFile {
      (localFile, actualFile) =>
        localFile.name shouldBe actualFile.getName
    }
    
    
    "return its full path" in withLocalFile {
      (localFile, actualFile) =>
        localFile.absolutePath shouldBe watchDir.resolve(actualFile.getName).toString
    }
    
    
    "return its length" in withLocalFile {
      (localFile, actualFile) =>
        localFile.length shouldBe actualFile.length
    }
    
    
    "return its lastModified" in withLocalFile {
      (localFile, actualFile) =>
        localFile.lastModified shouldBe actualFile.lastModified
    }
    
    
    "read its content into a buffer" in withLocalFile {
      (localFile, actualFile) =>
        val buffer = ByteBuffer.allocate(actualFile.length.asInstanceOf[Int])
        
        localFile.read(buffer, offset=0)
        
        val readString = new String(buffer.array)
        val expectedString = scala.io.Source.fromFile(actualFile).mkString
        
        readString shouldBe expectedString
    }
    
    
    "read its content from an offset into a buffer" in withLocalFile {
      (localFile, actualFile) =>
        
        val fileLength = actualFile.length.asInstanceOf[Int]
        val offset = fileLength / 2
        
        val buffer = ByteBuffer.allocate(fileLength - offset)
        
        localFile.read(buffer, offset)
        
        val readString = new String(buffer.array)
        val expectedString = scala.io.Source.fromFile(actualFile).mkString.substring(offset)
        
        readString shouldBe expectedString
    }
  }
}
