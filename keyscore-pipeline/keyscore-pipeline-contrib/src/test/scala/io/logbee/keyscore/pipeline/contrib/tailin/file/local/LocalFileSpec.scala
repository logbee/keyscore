package io.logbee.keyscore.pipeline.contrib.tailin.file.local

import io.logbee.keyscore.pipeline.contrib.tailin.util.SpecWithTempDir
import org.scalatest.Matchers
import io.logbee.keyscore.pipeline.contrib.tailin.util.TestUtil
import java.nio.ByteBuffer
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.nio.charset.StandardCharsets
import io.logbee.keyscore.pipeline.contrib.tailin.file.LocalFile
import io.logbee.keyscore.pipeline.contrib.tailin.file.LocalFile.localFile2File
import org.scalactic.source.Position.apply

@RunWith(classOf[JUnitRunner])
class LocalFileSpec extends SpecWithTempDir with Matchers {
  
  val charset = StandardCharsets.UTF_8
  
  
  def withLocalFile(fileName: String, content: ByteBuffer, testCode: LocalFile => Any) = {
    
    val actualFile = TestUtil.createFile(watchDir, fileName, charset.decode(content).toString)
    
    val localFile = new LocalFile(actualFile)
    
    try {
      testCode(localFile)
    }
    finally {
      if (actualFile != null) {
        actualFile.delete()
      }
    }
  }
  
  
  
  "A LocalFile should" - {
    "return correct metadata" in {
      
      val fileName = "localFile.txt"
      val content = charset.encode("fileContent")
      
      withLocalFile(fileName, content, {
        localFile =>
          localFile.name shouldBe fileName
          
          localFile.absolutePath shouldBe watchDir.resolve(fileName).toString
          
          val currentTime = System.currentTimeMillis
          assert(localFile.lastModified >= currentTime - 3 * 1000)
          assert(localFile.lastModified <= currentTime)
          
          localFile.length shouldBe content.limit
      })
    }
    
    
    "list its rotated files" in {
      
      val fileName = "localFile.txt"
      
      withLocalFile(fileName, charset.encode("fileContent"), {
        localFile =>
          val rotFile1 = TestUtil.createFile(watchDir, fileName + ".1", "fileContent1")
          val rotFile2 = TestUtil.createFile(watchDir, fileName + ".2", "fileContent22")
          
          val rotationPattern = fileName + ".[1-5]"
          
          localFile.listRotatedFiles(rotationPattern) should contain allOf(rotFile1, rotFile2)
      })
    }
    
    
    "read its content into a buffer" in {
      
      val content = charset.encode("fileContent")
      
      withLocalFile("localFile.txt", content, {
        localFile =>
          val buffer = ByteBuffer.allocate(content.limit)
          
          localFile.read(buffer, offset=0)
          
          buffer shouldBe content
      })
    }
    
    
    "read its content from an offset into a buffer" in {
      
      val content = charset.encode("fileContent")
      
      withLocalFile("localFile.txt", content, {
        localFile =>
          val fileLength = content.limit
          val offset = fileLength / 2
          
          val buffer = ByteBuffer.allocate(fileLength - offset)
          
          localFile.read(buffer, offset)
          
          buffer.array shouldBe content.array
                                  .drop(offset)
                                  .dropRight(content.capacity - content.limit) //the resulting array has 0s from the buffer's limit to the end, which we drop here
      })
    }
  }
}
