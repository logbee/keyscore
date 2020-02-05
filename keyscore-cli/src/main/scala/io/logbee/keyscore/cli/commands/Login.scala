package io.logbee.keyscore.cli.commands

import java.util.concurrent.Callable

import picocli.CommandLine.{Command, Option, Parameters}

@Command(
  name = "login",
  description = Array("Login to a cluster."))
class Login extends Callable[Int] {

  @Option(
    names = Array("-u", "--username"),
    description = Array("Username."),
    required = true)
  private var username: String = _

  @Option(
    names = Array("-p", "--password"),
    description = Array("Password."),
    required = true,
    arity = "0..1",
    interactive = true)
  private var password: String = _

  @Parameters(
    description = Array("Cluster alias, otherwise 'default'."),
    arity = "0..1",
    defaultValue = "default")
  private var alias: String = _

  override def call(): Int = {
    println(s"Login to $alias as $username:$password")
    0
  }
}
