package io.logbee.keyscore.cli.commands.cluster

import java.util.concurrent.Callable

import picocli.CommandLine.{Command, Parameters}

@Command(
  name = "list",
  aliases = Array("ls"),
  description = Array("Lists all clusters."))
class List extends Callable[Int] {

  override def call(): Int = {
    println(s"List clusters")
    0
  }
}
