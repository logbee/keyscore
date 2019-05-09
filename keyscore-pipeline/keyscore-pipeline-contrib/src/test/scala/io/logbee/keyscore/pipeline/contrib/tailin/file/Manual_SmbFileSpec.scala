package io.logbee.keyscore.pipeline.contrib.tailin.file

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.EnumSet

import org.scalatest.Matchers

import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.msfscc.FileAttributes
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2CreateOptions
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.smbj.share.DiskShare

import io.logbee.keyscore.pipeline.contrib.tailin.util.Manual_SpecWithSmbShare


class Manual_SmbFileSpec extends Manual_SpecWithSmbShare with Matchers {
  
  
  def createFile(share: DiskShare, fileName: String, content: ByteBuffer): SmbFile = {
    
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
    
      try {
        val smbFile = createFile(share, fileName, content)
        
        testCode(smbFile)
      }
      finally {
        share.rm(fileName)
      }
  }
  
  
  
  "A SmbFile should" - {
    val charset = StandardCharsets.UTF_8
    
    "return correct metadata" in withShare { share =>
      
      val fileName = "smbTestFile.txt"
      val content = charset.encode("Hellö Wörld")
      
      withSmbFile(share, fileName, content, {
        smbFile =>
          smbFile.name shouldBe fileName
          smbFile.absolutePath shouldBe "\\\\" + hostName + "\\" + shareName + "\\" + fileName
          
          
          //the lastModified-time should be roughly around the current time (the server's system time may be different or it may take a moment to create the file)
          val currentTime = System.currentTimeMillis
          assert(smbFile.lastModified >= currentTime - 60 * 1000)
          assert(smbFile.lastModified <= currentTime + 60 * 1000)
          
          
          smbFile.length shouldBe content.limit
      })
    }
    
    
    
    "list its rotated files" in withShare { share => //TEST
      
      val dir = "testDir/" //TODO we don't function when we're at the root /  -> do we function in LocalFile?
      val fileName = "smbTestFile.txt"
      
      withSmbFile(share, dir + fileName, charset.encode("base file"), { smbFile =>
        withSmbFile(share, dir + fileName + ".1", charset.encode("rotated file 1"), { rotFile1 =>
          withSmbFile(share, dir + fileName + ".2", charset.encode("rotated file 22"), { rotFile2 =>
            
            val rotationPattern = fileName + ".[1-5]"
            
            smbFile.listRotatedFiles(rotationPattern) should contain allOf(rotFile1, rotFile2)
          })
        })
      })
    }
    
    
    
    "read its content into a buffer" in withShare { share =>
      
      val content = charset.encode("Hellö Wörld")
      
      withSmbFile(share, "smbTestFile.txt", content, {
        smbFile =>
          val buffer = ByteBuffer.allocate(content.limit)
          
          smbFile.read(buffer, offset=0)
          
          buffer shouldBe content
      })
    }
    
    
    "read its content from an offset into a buffer" in withShare { share =>
      
      val content = charset.encode("Hellö Wörld")
      
      withSmbFile(share, "smbTestFile.txt", content, {
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
  }
}
