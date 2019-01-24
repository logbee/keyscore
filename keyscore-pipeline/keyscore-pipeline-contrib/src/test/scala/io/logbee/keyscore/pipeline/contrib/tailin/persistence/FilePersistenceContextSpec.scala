package io.logbee.keyscore.pipeline.contrib.tailin.persistence

import java.nio.file.Files
import java.nio.file.Path

import scala.reflect.runtime.universe.typeTag

import org.junit.runner.RunWith
import org.scalatest.BeforeAndAfter
import org.scalatest.FreeSpec
import org.scalatest.Matchers
import org.scalatest.ParallelTestExecution

import io.logbee.keyscore.pipeline.contrib.tailin.read.FileReadRecord
import io.logbee.keyscore.pipeline.contrib.tailin.util.TestUtil
import org.scalatest.junit.JUnitRunner


case class TestCaseClass(a: String, b: Integer)


@RunWith(classOf[JUnitRunner])
class FilePersistenceContextSpec extends FreeSpec with BeforeAndAfter with Matchers with ParallelTestExecution {

  var watchDir: Path = null

  before {
    watchDir = Files.createTempDirectory("watchTest")
    TestUtil.waitForFileToExist(watchDir.toFile)
  }

  after {
    TestUtil.recursivelyDelete(watchDir)
  }

  trait PersistenceFile {

    val persistenceFile = watchDir.resolve("persistence.json").toFile

    persistenceFile.createNewFile()
    TestUtil.waitForFileToExist(persistenceFile)

    val filePersistenceContext = new FilePersistenceContext(persistenceFile)
  }

  "A FilePersistenceContext" - {
    "should write and read" - {

      "one object correctly" in new PersistenceFile {

        filePersistenceContext.store("Hello", "World")

        val value = filePersistenceContext.load[String]("Hello")(typeTag[String])

        value shouldBe Some("World")
      }

      "multiple objects correctly" in new PersistenceFile {

        filePersistenceContext.store("One", "1")
        filePersistenceContext.store("Two", 2)

        val valueOne = filePersistenceContext.load[String]("One")(typeTag[String])
        val valueTwo = filePersistenceContext.load[Int]("Two")(typeTag[Int])

        valueOne shouldBe Some("1")
        valueTwo shouldBe Some(2)
      }

      "a case class correctly" in new PersistenceFile {

        filePersistenceContext.store("Key", TestCaseClass("The answer to everything: ", 42))

        val value = filePersistenceContext.load[TestCaseClass]("Key")(typeTag[TestCaseClass])

        value shouldBe Some(TestCaseClass("The answer to everything: ", 42))
      }

      "multiple case classes correctly" in new PersistenceFile {

        val key1 = "/path/to/file1"
        val value1 = FileReadRecord(123456789, 987654321)

        filePersistenceContext.store(key1, value1)

        val key2 = "/path/to/file2"
        val value2 = FileReadRecord(234567891, 198765432)
        filePersistenceContext.store(key2, value2)

        val loaded1 = filePersistenceContext.load[FileReadRecord](key1)(typeTag[FileReadRecord])
        val loaded2 = filePersistenceContext.load[FileReadRecord](key2)(typeTag[FileReadRecord])

        loaded1 shouldBe Some(value1)
        loaded2 shouldBe Some(value2)
      }
    }

    "should return None," - {

      "if the persistence file is empty" in new PersistenceFile {

        val value = filePersistenceContext.load[String]("non-existent key")(typeTag[String])

        value shouldBe None
      }
      
      "if no matching persistence record was found" in new PersistenceFile {

        filePersistenceContext.store("key1", "value1")
        filePersistenceContext.store("key2", "value2")

        val value = filePersistenceContext.load[String]("non-existent key")(typeTag[String])

        value shouldBe None
      }
    }

    "should update the value" - {

      "if the same key is written twice" in new PersistenceFile {

        filePersistenceContext.store("Key", "Value1")
        filePersistenceContext.store("Key", "Value2")

        val value = filePersistenceContext.load[String]("Key")(typeTag[String])

        value shouldBe Some("Value2")
      }

      "if the same key is written twice, with a different data type as value" in new PersistenceFile {

        filePersistenceContext.store("Key", "1")
        filePersistenceContext.store("Key", 2)

        val value = filePersistenceContext.load[Int]("Key")(typeTag[Int])

        value shouldBe Some(2)
      }
    }

    "should remove" - {

      "a record" in new PersistenceFile {

        val key = "Key"
        val value = "Value"

        filePersistenceContext.store(key, value)

        val loaded1 = filePersistenceContext.load[String](key)(typeTag[String])
        loaded1 shouldBe Some(value)

        filePersistenceContext.remove(key)

        val loaded2 = filePersistenceContext.load[String](key)(typeTag[String])
        loaded2 shouldBe None
      }

      "only the record with the correct key" in new PersistenceFile {

        val key1 = "Key1"
        val value1 = "Value1"

        val key2 = "Key2"
        val value2 = "Value2"

        filePersistenceContext.store(key1, value1)
        filePersistenceContext.store(key2, value2)

        filePersistenceContext.remove(key2)

        val loaded1 = filePersistenceContext.load[String](key1)(typeTag[String])
        val loaded2 = filePersistenceContext.load[String](key2)(typeTag[String])

        loaded1 shouldBe Some(value1)
        loaded2 shouldBe None
      }
    }
  }
}
