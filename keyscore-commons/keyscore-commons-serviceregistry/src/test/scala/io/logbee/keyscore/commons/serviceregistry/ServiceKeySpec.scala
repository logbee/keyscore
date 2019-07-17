package io.logbee.keyscore.commons.serviceregistry

import io.logbee.keyscore.commons.serviceregistry.ServiceRegistry.ServiceKey
import org.scalatest.{FreeSpec, Matchers}

class ServiceKeySpec extends FreeSpec with Matchers {

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
