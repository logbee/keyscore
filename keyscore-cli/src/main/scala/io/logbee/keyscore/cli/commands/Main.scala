package io.logbee.keyscore.cli.commands

import java.util.concurrent.Callable

import io.logbee.keyscore.cli.Cli
import io.logbee.keyscore.cli.commands.Main.DefaultVersionProvider
import io.logbee.keyscore.cli.util.AppInfo
import picocli.CommandLine.Model.CommandSpec
import picocli.CommandLine.{Command, IVersionProvider, ParameterException, Spec}

@Command(
  name = "keyscorectl",
  description = Array("Command-Line tool to control a KEYSCORE cluster."),
  mixinStandardHelpOptions = true,
  synopsisSubcommandLabel = "COMMAND",
  versionProvider = classOf[DefaultVersionProvider],
  subcommands = Array(
    classOf[Get],
    classOf[Login],
    classOf[Logout])
)
class Main extends Callable[Integer] {

  @Spec var spec: CommandSpec = _

  def call(): Integer = {
    throw new ParameterException(spec.commandLine(), "Missing required subcommand");
  }
}

object Main {
  class DefaultVersionProvider extends IVersionProvider {
    override def getVersion: Array[String] = {
      val appInfo = AppInfo.fromMainClass[Cli]
      Array(s"${appInfo.name} ${appInfo.version} ${appInfo.revision}")
    }
  }
}