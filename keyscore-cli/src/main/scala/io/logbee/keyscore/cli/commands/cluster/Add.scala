package io.logbee.keyscore.cli.commands.cluster

import java.util.concurrent.Callable

import picocli.CommandLine.{Command, Option, Parameters}

@Command(
  name = "add",
  aliases = Array(),
  description = Array("Add a cluster."))
class Add extends Callable[Int] {

  @Option(
    names = Array("-f", "--force"),
    description = Array("Overwrites an already existing cluster with the specified alias."),
    defaultValue = "false")
  private var force: Boolean = _

  @Parameters(
    description = Array("An alias to assign to the cluster."),
    index = "0",
    arity = "1")
  private var alias: String = _

  @Parameters(
    description = Array("URL of the cluster."),
    index = "1",
    arity = "1")
  private var url: String = _

  override def call(): Int = {
    println(s"Adding cluster $alias $url (force=$force).")
    0
  }
}
