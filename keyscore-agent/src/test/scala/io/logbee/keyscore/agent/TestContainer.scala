package io.logbee.keyscore.agent

import com.dimafeng.testcontainers.{ForAllTestContainer, GenericContainer}
import com.github.dockerjava.api.command.CreateContainerCmd
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.junit.JUnitRunner

/**
  * This is a simple Class for testing the TestingContainer Setup in Keyscore
  * It will pull the latest kafka container from quay and expose the ports
  *
  * This was temporarily located in the keyscore.agent package because of simplified testing purposes
  */
//@RunWith(classOf[JUnitRunner])
class TestContainer extends WordSpec with ForAllTestContainer with Matchers {
  override val container: GenericContainer = GenericContainer("quay.io/logbee/docker-kafka:latest").configure ({ c =>
    c.withExposedPorts(9092, 2181)
    c.withCreateContainerCmdModifier((cmd: CreateContainerCmd) => cmd.withHostName("keyscore-kafka"))
    c.withCreateContainerCmdModifier((cmd: CreateContainerCmd) => cmd.withName("keyscore-kafka"))
  })

  "A TestContainers" should {
    val upTime: Long = 60000

    s"be up ${upTime}ms" in {

      container.containerName should be ("/keyscore-kafka")
      println(s"The container has the following name: ${container.containerName}")

      container.exposedPorts should be (List(9092,2181))
      println(s"The container has the following ports exposed: ${container.exposedPorts}")

      Thread.sleep(upTime)
      container.stop()
    }
  }

}