package io.logbee.keyscore.commons.serviceregistry

import io.logbee.keyscore.commons.serviceregistry.ServiceRegistry.ServiceKey
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class ServiceKeySpec extends AnyFreeSpec with Matchers {

  Seq(
    (ServiceKey[String]("MyService"), ServiceKey[String]("MyService")),
    (ServiceKey[Int]("MyService"), ServiceKey[Int]("MyService")),
    (ServiceKey[String]("MyService"), ServiceKey[Int]("MyService")),
  )
  .foreach { case (keyA, keyB) =>

    s"$keyA should be equals to $keyB" in {
      keyA shouldBe keyB
    }
  }

  Seq(
    (ServiceKey[String]("MyService"), ServiceKey[Int]("AnOtherService")),
    (ServiceKey[Int]("MyService"), ServiceKey[Int]("AnOtherService")),
  )
  .foreach { case (keyA, keyB) =>

    s"$keyA should not be equals to $keyB" in {
      keyA should not be keyB
    }
  }
}
