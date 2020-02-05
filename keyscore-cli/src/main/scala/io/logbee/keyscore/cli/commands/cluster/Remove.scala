package io.logbee.keyscore.cli.commands.cluster

import java.util.concurrent.Callable

import picocli.CommandLine.{Command, Parameters}

@Command(
  name = "remove",
  aliases = Array("rm"),
  description = Array("Remove a cluster."))
class Remove extends Callable[Int] {

  @Parameters(
    description = Array("Cluster alias, otherwise 'default'."),
    arity = "1")
  private var alias: String = _

  override def call(): Int = {
    println(s"Removing cluster $alias.")
    0
  }
}
