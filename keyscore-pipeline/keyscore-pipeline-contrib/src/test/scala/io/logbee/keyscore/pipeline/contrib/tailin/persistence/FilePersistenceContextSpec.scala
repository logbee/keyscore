package io.logbee.keyscore.pipeline.contrib.tailin.persistence

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import com.typesafe.config.ConfigFactory
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.FilePersistenceContext.PersistenceFormat
import io.logbee.keyscore.pipeline.contrib.tailin.read.FileReadRecord
import io.logbee.keyscore.pipeline.contrib.tailin.util.SpecWithTempDir
import org.junit.runner.RunWith
import org.scalatest.{Matchers, ParallelTestExecution}
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class FilePersistenceContextSpec extends SpecWithTempDir with Matchers with ParallelTestExecution {

  trait PersistenceFile {

    val persistenceDir = watchDir.resolve("storage/").toFile

    val config = ConfigFactory.parseString(
      s"""
         |enabled: "true",
         |persistence-dir: "${persistenceDir.getPath}",
         |""".stripMargin)

    val filePersistenceContext = FilePersistenceContext(
      FilePersistenceContext.Configuration(
        config,
        userEnabled=true,
        "persistence",
      )
    )

    val key1 = "/path/to/file1"
    val value1 = FileReadRecord(123456789, 987654321, 0)

    val key2 = "/path/to/file2"
    val value2 = FileReadRecord(234567891, 198765432, 0)
  }

  "A PersistenceFormat" - {

    "should be serializable" in {

      import org.json4s.DefaultFormats
      import org.json4s.native.Serialization._

      implicit val format = DefaultFormats

      val persistenceFormat = PersistenceFormat("/a/b", FileReadRecord(1, 2, 3))

      val serialized = write(persistenceFormat)
      val result = read[PersistenceFormat](serialized)

      result shouldBe persistenceFormat
    }
  }

  "A FilePersistenceContext" - {
    "should write and read" - {

      "multiple case classes correctly" in new PersistenceFile {

        filePersistenceContext.store(key1, value1)
        filePersistenceContext.store(key2, value2)

        val loaded1 = filePersistenceContext.load(key1)
        val loaded2 = filePersistenceContext.load(key2)

        loaded1 shouldBe Some(value1)
        loaded2 shouldBe Some(value2)
      }
    }

//    "should write and find keys with common prefix" in new PersistenceFile {
//
//      filePersistenceContext.store("One", "1")
//      filePersistenceContext.store("OneTwo", "12")
//      filePersistenceContext.store("Two", 2)
//
//      val keys = filePersistenceContext.findKeysWithPrefix("One")
//
//      keys should contain allOf ("One", "OneTwo")
//    }

    "should return None," - {

      "if the persistence file is empty" in new PersistenceFile {

        val value = filePersistenceContext.load("non-existent key")

        value shouldBe None
      }
      
      "if no matching persistence record was found" in new PersistenceFile {

        filePersistenceContext.store(key1, value1)
        filePersistenceContext.store(key2, value2)

        val value = filePersistenceContext.load("non-existent key")

        value shouldBe None
      }
    }

    "should update the value" - {

      "if the same key is written twice" in new PersistenceFile {

        filePersistenceContext.store(key1, value1)
        filePersistenceContext.store(key1, value2)

        val value = filePersistenceContext.load(key1)

        value shouldBe Some(value2)
      }
    }

    "should remove" - {

      "a record" in new PersistenceFile {

        val key = key1
        val value = value1

        filePersistenceContext.store(key, value)

        val loaded1 = filePersistenceContext.load(key)
        loaded1 shouldBe Some(value)

        filePersistenceContext.remove(key)

        val loaded2 = filePersistenceContext.load(key)
        loaded2 shouldBe None
      }

      "only the record with the correct key" in new PersistenceFile {

        filePersistenceContext.store(key1, value1)
        filePersistenceContext.store(key2, value2)

        filePersistenceContext.remove(key2)

        val loaded1 = filePersistenceContext.load(key1)
        val loaded2 = filePersistenceContext.load(key2)

        loaded1 shouldBe Some(value1)
        loaded2 shouldBe None
      }
    }

    "should not raise an Exception when the file persistence file is corrupted" in {

      val persistenceDir = watchDir.resolve("storage/").toFile
      val storage = new File(persistenceDir, "persistence")
      storage.mkdirs()

      Files.write(new File(storage, "test-file.json").toPath, "{}".getBytes(StandardCharsets.UTF_8))

      val config = ConfigFactory.parseString(
        s"""
           |enabled: "true",
           |persistence-dir: "${persistenceDir.getPath}",
           |""".stripMargin)

      val filePersistenceContext = FilePersistenceContext(
        FilePersistenceContext.Configuration(
          config,
          userEnabled=true,
          "persistence",
        )
      )
    }
  }
}
