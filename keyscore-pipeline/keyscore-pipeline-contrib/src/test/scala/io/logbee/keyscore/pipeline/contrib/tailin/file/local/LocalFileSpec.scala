package io.logbee.keyscore.pipeline.contrib.tailin.file.local

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

import io.logbee.keyscore.pipeline.contrib.tailin.file.local.LocalFile.OpenLocalFile
import io.logbee.keyscore.pipeline.contrib.tailin.util.{SpecWithTempDir, TestUtil}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

import scala.util.{Failure, Success}

@RunWith(classOf[JUnitRunner])
class LocalFileSpec extends SpecWithTempDir with Matchers {
  
  implicit val charset = StandardCharsets.UTF_8
  
  
  "A LocalFile should" - {
    "return correct metadata" in {
      
      val fileName = "localFile.txt"
      val content = "fileContent"
      
      TestUtil.withOpenLocalFile(watchDir, fileName, content) {
        localFile =>
          localFile.name shouldBe fileName
          
          localFile.absolutePath shouldBe watchDir.resolve(fileName).toString
          
          localFile.parent shouldBe watchDir.toString + "/"
          
          val currentTime = System.currentTimeMillis
          assert(localFile.lastModified >= currentTime - 3 * 1000)
          assert(localFile.lastModified <= currentTime)
          
          localFile.length shouldBe charset.encode(content).limit
      }
    }
    
    
    "list its rotated files" in {
      
      val fileName = "localFile.txt"
      
      TestUtil.withOpenLocalFile(watchDir, fileName, "fileContent") { localFile =>
        TestUtil.withOpenLocalFile(watchDir, fileName + ".1", "fileContent1") { rotFile1 =>
          TestUtil.withOpenLocalFile(watchDir, fileName + ".2", "fileContent22") { rotFile2 =>
            val rotationPattern = fileName + ".[1-5]"

            var expectation = Seq(rotFile1, rotFile2)
            localFile.listRotatedFiles(rotationPattern).foreach(_.open {
              case Success(file: OpenLocalFile) =>
                expectation should contain oneElementOf Seq(file)
                expectation = expectation.filterNot(_ == file)
              case Success(_) => fail()
              case Failure(ex) => throw ex
            })
          }
        }
      }
    }
    
    
    "read its content into a buffer" in {
      
      val content = "fileContent"
      val encodedContent = charset.encode(content)
      
      TestUtil.withOpenLocalFile(watchDir, "localFile.txt", content) {
        localFile =>
          val buffer = ByteBuffer.allocate(encodedContent.limit)
          
          localFile.read(buffer, offset=0)
          
          new String(buffer.array()) shouldBe new String(encodedContent.array()).trim
      }
    }
    
    
    "read its content from an offset into a buffer" in {
      
      val content = "fileContent"
      val encodedContent = charset.encode(content)
      
      TestUtil.withOpenLocalFile(watchDir, "localFile.txt", content) {
        localFile =>
          val fileLength = encodedContent.limit
          val offset = fileLength / 2
          
          val buffer = ByteBuffer.allocate(fileLength - offset)
          
          localFile.read(buffer, offset)
          
          buffer.array shouldBe encodedContent.array
                                  .drop(offset)
                                  .dropRight(encodedContent.capacity - encodedContent.limit) //the resulting array has 0s from the buffer's limit to the end, which we drop here
      }
    }


    "should delete itself" in {

      val content = "fileContent"

      TestUtil.withOpenLocalFile(watchDir, "localFile.txt", content) { localFile =>
        localFile.lastModified should not equal 0

        localFile.delete()

        localFile.lastModified shouldEqual 0
      }
    }
  }
}
