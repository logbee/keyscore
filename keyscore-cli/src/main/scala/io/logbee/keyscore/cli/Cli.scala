package io.logbee.keyscore.cli

import io.logbee.keyscore.cli.commands.Main
import picocli.CommandLine

object Cli extends App {
  new CommandLine(new Main()).execute(args: _*)
}

class Cli