package io.logbee.keyscore.cli.commands

import java.util.concurrent.Callable

import picocli.CommandLine.{Command, Option, Parameters}

@Command(
  name = "get",
  description = Array("Display one or many entities."))
class Get extends Callable[Int] {

  @Option(
    names = Array("-c", "--cluster"),
    description = Array("Cluster alias, otherwise 'default'."),
    defaultValue = "default")
  private var cluster: String = _

  @Parameters(
    index = "0",
    arity = "1"
  )
  private var entity: String = _

  override def call(): Int = {
    println(s"Get $entity from $cluster")
    0
  }
}
