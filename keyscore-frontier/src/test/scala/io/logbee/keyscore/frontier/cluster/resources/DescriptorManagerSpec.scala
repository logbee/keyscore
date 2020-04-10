package io.logbee.keyscore.frontier.cluster.resources

import akka.pattern._
import akka.util.Timeout
import io.logbee.keyscore.commons.cluster.resources.DescriptorMessages._
import io.logbee.keyscore.model.descriptor.Descriptor
import io.logbee.keyscore.test.fixtures.ProductionSystemWithMaterializerAndExecutionContext
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._
import scala.language.postfixOps

class DescriptorManagerSpec extends ProductionSystemWithMaterializerAndExecutionContext with AnyFreeSpecLike with Matchers with ScalaFutures {

  implicit val timeout = Timeout(5 seconds)

  "A DescriptorManager" - {

    val descriptorManager = system.actorOf(DescriptorManager())

    "should manage descriptors" in {

      val uuidA = "a95158dd-b351-4173-a29d-9d61af4be7fa"
      val uuidB = "efebb94d-965a-4bce-8e0d-642e95314c56"

      val descriptorA = Descriptor(uuidA)
      val descriptorB = Descriptor(uuidB)

      descriptorManager ! StoreDescriptorRequest(descriptorA)
      descriptorManager ! StoreDescriptorRequest(descriptorB)

      whenReady((descriptorManager ? GetDescriptorRequest(uuidA)).mapTo[GetDescriptorResponse]) { response =>

        response.descriptor shouldBe 'defined
        response.descriptor.get.ref.uuid shouldBe uuidA
      }

      descriptorManager ! DeleteDescriptorRequest(uuidA)

      whenReady((descriptorManager ? GetDescriptorRequest(uuidA)).mapTo[GetDescriptorResponse]) { response =>

        response.descriptor shouldNot be ('defined)
      }
    }
  }
}
