package io.logbee.keyscore.cli.commands

import java.util.concurrent.Callable

import picocli.CommandLine.{Command, Option, Parameters}

@Command(
  name = "logout",
  description = Array("Logout from a cluster."))
class Logout extends Callable[Int] {

  @Option(
    names = Array("-p", "--purge"),
    description = Array("Purge all cached data related to the specified cluster."))
  private var purge: Boolean = _

  @Parameters(
    description = Array("Cluster alias, otherwise 'default'."),
    arity = "0..1",
    defaultValue = "default")
  private var alias: String = _

  override def call(): Int = {
    println(s"Logout from $alias ${if (purge) "and purge." else "."}")
    0
  }
}
