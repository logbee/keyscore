package io.logbee.keyscore.pipeline.contrib.tailin.file.smb

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

import com.hierynomus.mssmb2.SMBApiException
import org.scalatest.Matchers
import io.logbee.keyscore.pipeline.contrib.tailin.util.Manual_SpecWithSmbShare


class Manual_SmbFileSpec extends Manual_SpecWithSmbShare with Matchers {
  
  
  "An SmbFile should" - {
    val charset = StandardCharsets.UTF_8
    
    "return correct metadata" in withShare { implicit share =>
      
      val fileName = "smbTestFile.txt"
      val content = charset.encode("Hellö Wörld")
      
      withSmbFile(fileName, content, {
        smbFile =>
          smbFile.name shouldBe fileName
          smbFile.absolutePath shouldBe s"\\\\$hostName\\$shareName\\$fileName"
          smbFile.parent shouldBe s"\\\\$hostName\\$shareName\\"
          
          //the lastModified-time should be roughly around the current time (the server's system time may be different or it may take a moment to create the file)
          val currentTime = System.currentTimeMillis
          assert(smbFile.lastModified >= currentTime - 5 * 60 * 1000)
          assert(smbFile.lastModified <= currentTime + 5 * 60 * 1000)
          
          
          smbFile.length shouldBe content.limit
      })
    }
    
    
    
    "list its rotated files" ignore withShare { implicit share => //TODO
      
      val dir = "testDir" //TODO we don't function when we're at the root /  -> do we function in LocalFile?
      val fileName = "smbTestFile.txt"
      
      withSmbDir(dir, { _ =>
        withSmbFile(dir + "\\" + fileName, charset.encode("base file"), { smbFile =>
          withSmbFile(dir + "\\" + fileName + ".1", charset.encode("rotated file 1"), { rotFile1 =>
            withSmbFile(dir + "\\" + fileName + ".2", charset.encode("rotated file 22"), { rotFile2 =>
              
              val rotationPattern = fileName + ".[1-5]"
              
              smbFile.listRotatedFiles(rotationPattern) should contain allOf(rotFile1, rotFile2)
            })
          })
        })
      })
    }
    
    
    
    "read its content into a buffer" in withShare { implicit share =>
      
      val content = charset.encode("Hellö Wörld")
      
      withSmbFile("smbTestFile.txt", content, {
        smbFile =>
          val buffer = ByteBuffer.allocate(content.limit)
          
          smbFile.read(buffer, offset=0)
          
          buffer shouldBe content
      })
    }
    
    
    "read its content from an offset into a buffer" in withShare { implicit share =>
      
      val content = charset.encode("Hellö Wörld")
      
      withSmbFile("smbTestFile.txt", content, {
        smbFile =>
          val fileLength = content.limit
          val offset = fileLength / 2
          
          val buffer = ByteBuffer.allocate(fileLength - offset)
          
          smbFile.read(buffer, offset)
          
          buffer.array shouldBe content.array
                                  .drop(offset)
                                  .dropRight(content.capacity - content.limit) //the resulting array has 0s from the buffer's limit to the end, which we drop here
      })
    }
    
    
    "delete itself" in withShare { implicit share =>
      
      val content = charset.encode("Hellö Wörld")
      
      withSmbFile("smbTestFile.txt", content, { smbFile =>
        smbFile.delete()
        
        assertThrows[SMBApiException] {
          smbFile.lastModified
        }
      })
    }
  }
}
