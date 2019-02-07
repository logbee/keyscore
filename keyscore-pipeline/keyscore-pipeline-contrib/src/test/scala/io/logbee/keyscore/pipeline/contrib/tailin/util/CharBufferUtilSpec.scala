package io.logbee.keyscore.pipeline.contrib.tailin.util

import java.io.File
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardOpenOption

import org.scalatest.FreeSpec
import org.scalatest.Matchers


import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import io.logbee.keyscore.pipeline.contrib.tailin.read.FileReader.CharPos

@RunWith(classOf[JUnitRunner])
class CharBufferUtilSpec extends FreeSpec with Matchers {
  
  
  //list of charsets to test with
  //if you add a charset to this list, you need to create a file with the text "Hello Wörld", saved in the correct encoding, in the resource folder of this class
  val charsets = Seq(StandardCharsets.UTF_8, StandardCharsets.UTF_16, StandardCharsets.ISO_8859_1, Charset.forName("windows-1252"))
  
  

  def readBufferFromFile(file: File, charset: Charset, expectedValue: String) {

    val fileLength = file.length.asInstanceOf[Int]
    val byteBuffer: ByteBuffer = ByteBuffer.allocate(fileLength)
    var readChannel: FileChannel = null

    try {
      
      readChannel = Files.newByteChannel(file.toPath, StandardOpenOption.READ).asInstanceOf[FileChannel]
      
      byteBuffer.clear()
      readChannel.read(byteBuffer, 0)
      
      byteBuffer.flip()
      
      val charBuffer = charset.decode(byteBuffer)
      val decoded = CharBufferUtil.getBufferSectionAsString(charBuffer, CharPos(0), CharPos(charBuffer.length))
      
      decoded shouldBe expectedValue
    }
    finally {
      if (readChannel != null) {
        readChannel.close()
      }
    }
  }

  "CharBufferUtil" - {

    "should decode a buffer section encoded in" - {

      val files = charsets
        .map(charset => (charset, charset.toString))
        .map { case (charset, charsetName) => (charset, new File(getClass.getResource(s"${charsetName}_Sample").toURI)) }

      files.foreach{ case (charset, file) => 
        charset.toString in readBufferFromFile(file, charset, "Hello Wörld")
      }
    }
    
    
    "should find the start of the next line" - {
      
      charsets.foreach { case charset =>
        s"in charset $charset" - {
          
          val contents =          Seq("\n_", "_\n", "_\n_", "_\n\n\n\n\n_", "\r\n_", "\n_\n_")
          val startingPositions = Seq(  0,     1,      1,          1,          0,       0    )
          val expectedPositions = Seq(  1,     2,      2,          6,          2,       1    )
    
          val samples = contents
            .map(string => charset.decode(charset.encode(string)))
            .zip(startingPositions)
            .zip(expectedPositions)
            .zip(contents)
    
          samples.foreach { case (((buffer, startingPosition), expectedPosition), content) =>
    
            val escapedContent = content.replaceAllLiterally("\n", "\\n").replaceAllLiterally("\r", "\\r")
    
            s"""at expected position $expectedPosition when starting to search from position $startingPosition in a buffer that contains "$escapedContent"""" in {
    
              val returnedPosition = CharBufferUtil.getStartOfNextLine(buffer, CharPos(startingPosition))
    
              returnedPosition shouldBe CharPos(expectedPosition)
            }
          }
          
          
          
          "should throw an exception when starting to search for the start of the next line before the end of the previous line (not on a newline char)" in {
            
            assertThrows[IllegalArgumentException] {
              val string = "______\n_"
              val buffer = charset.decode(charset.encode(string))
              
              val startingPosition = 1
              
              val returnedPosition = CharBufferUtil.getStartOfNextLine(buffer, CharPos(startingPosition))
            }
          }
        }
      }
    }
  }
}
