package io.logbee.keyscore.commons.replication.internal.proto

import akka.actor.ActorSystem
import akka.serialization.{Serialization, SerializationExtension, Serializers}
import akka.testkit.TestProbe
import io.logbee.keyscore.commons.replication.Term
import io.logbee.keyscore.commons.replication.internal.Replicator.{AppendEntry, CommitEntry}
import io.logbee.keyscore.test.fixtures.ToActorRef._
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfterAll, FreeSpec, Matchers}
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ReplicationMessageSerializerSpec extends FreeSpec with Matchers with BeforeAndAfterAll {

  implicit val system: ActorSystem = ActorSystem("replication-message-serializer-test")
  val serialization: Serialization = SerializationExtension(system)
  val actor = TestProbe("probe")

  "A ReplicationMessageSerializer" - {

    Seq(
      AppendEntry(index = 27, term = Term(3), previousTerm = Term(20), committedIndex = 11, payload = Array(1, 2, 3, 4), replyTo = actor),
      CommitEntry(index = 42, actor)
    )
    .foreach{message =>

      s"should serialize $message" in {

        val serializer = serialization.findSerializerFor(message)
        val bytes = serialization.serialize(message).get
        val manifest = Serializers.manifestFor(serialization.findSerializerFor(message), message)
        val result = serialization.deserialize(bytes, serializer.identifier, manifest).get

        (message, result) match {
          case (original: AppendEntry, result: AppendEntry) =>
            result.index shouldBe original.index
            result.term shouldBe original.term
            result.committedIndex shouldBe original.committedIndex
            result.previousTerm shouldBe original.previousTerm
            result.payload should contain theSameElementsAs original.payload
            result.replyTo shouldBe original.replyTo

          case (original, result) =>
             result shouldBe original
        }
      }
    }
  }

  override protected def afterAll(): Unit = {
    system.terminate()
  }
}

object ReplicationMessageSerializerSpec {
  case class ExampleCommand(command: String) extends Serializable
}
