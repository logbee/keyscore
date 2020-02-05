package io.logbee.keyscore.cli.commands.cluster

import java.util.concurrent.Callable

import picocli.CommandLine.Model.CommandSpec
import picocli.CommandLine.{Command, Option, ParameterException, Parameters, Spec}

@Command(
  name = "cluster",
  description = Array("List, add or remove clusters."),
  synopsisSubcommandLabel = "COMMAND",
  subcommands = Array(
    classOf[Add],
    classOf[Remove],
    classOf[List]
  ))
class Cluster extends Callable[Int] {

  @Spec var spec: CommandSpec = _

  override def call(): Int = {
    throw new ParameterException(spec.commandLine(), "Missing required subcommand");
  }
}
