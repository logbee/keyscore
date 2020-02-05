package io.logbee.keyscore.cli.commands

import java.util.concurrent.Callable

import picocli.CommandLine.{Command, Option, Parameters}

@Command(
  name = "login",
  description = Array("Login to a cluster."))
class Login extends Callable[Int] {

  @Option(
    names = Array("-u", "--username"),
    description = Array("Username."))
  private var username: String = _

  @Option(
    names = Array("-p", "--password"),
    description = Array("Password."))
  private var password: String = _

  @Parameters(
    description = Array("An alias to assign to this cluster."),
    arity = "1")
  private var alias: String = _

  @Parameters(
    description = Array("URL of the cluster to login."),
    arity = "1")
  private var url: String = _

  override def call(): Int = {
    println(s"Login to $url")
    0
  }
}
