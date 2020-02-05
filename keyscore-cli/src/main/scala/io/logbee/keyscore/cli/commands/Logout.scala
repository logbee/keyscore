package io.logbee.keyscore.cli.commands

import java.util.concurrent.Callable

import picocli.CommandLine.{Command, Option, Parameters}

@Command(
  name = "logout",
  description = Array("Login to a cluster."))
class Logout extends Callable[Int] {

  @Option(
    names = Array("-p", "--purge"),
    description = Array("Purge all cached data related to the specified cluster."))
  private var purge: Boolean = _

  @Parameters(
    description = Array("An alias to assign to this cluster."),
    arity = "1")
  private var alias: String = _

  override def call(): Int = {
    println(s"Logout from $alias ${if (purge) "and purge." else "."}")
    0
  }
}
