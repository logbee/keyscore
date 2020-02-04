package io.logbee.keyscore.cli

import io.logbee.keyscore.cli.AppInfo.fromMainClass
import scopt.OParser

object Cli extends App {

  case class Configuration(
    command: String = ""
  )

  val appInfo = fromMainClass[Cli]

  val builder = OParser.builder[Configuration]
  val parser = {
    import builder._
    OParser.sequence(
      programName("keyscorectl"),
      head(s"${appInfo.name} ${appInfo.version} ${appInfo.revision}"),
      help("help"),
      version("version"),
      cmd("login")
        .action((_, c) => c.copy(command = "login"))
        .text("Log in to a cluster.")
        .children(
          opt[String]("username")
            .abbr("-u")
            .text("Username")
            .optional(),
          opt[String]("password")
            .abbr("-p")
            .text("Password")
            .optional(),
          arg[String]("alias")
            .text("An alias for the cluster.")
            .required(),
          arg[String]("url")
            .text("The URL to connect. (https://example.com:4711).")
            .required(),
        ),
      cmd("get")
        .text("Get one or more entities.")
        .children(
          opt[String]("cluster")
            .abbr("c")
            .text("Cluster name/alias.")
            .required(),
          cmd("agents")
            .text("List all agents")
            .required(),
          cmd("pipelines")
            .abbr("pipes")
            .text("List all pipelines.")
            .required(),
        ),
    )
  }

  OParser.parse(parser, args, Configuration()) match {
    case Some(config) =>
    case _ =>
  }
}

class Cli