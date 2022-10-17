package com.inossem
package elbing.actors.http

import elbing.actors.http.ServerActor._

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import com.typesafe.config.ConfigFactory
import akka.http.scaladsl.server.Directives._

import scala.util.{Failure, Success}

object ServerActor {
  sealed trait Command

  private final case class ServerStarted(port: Int) extends Command

  private final case class ServerFailed(ex: Throwable) extends Command

  def apply(): Behavior[Command] = Behaviors.setup(new ServerActor(_))
}

class ServerActor(context: ActorContext[Command]) extends AbstractBehavior(context) {
  implicit private val sys = context.system
  private val config = ConfigFactory.load().getConfig("app")
  private val port = config.getInt("port")

  private val homeRouter = path("home") {
    get {
      complete(StatusCodes.NoContent)
    }
  }
  private val binding = Http().newServerAt("0.0.0.0", port).bind(homeRouter)
  context.pipeToSelf(binding) {
    case Failure(exception) => ServerFailed(exception)
    case Success(value) => ServerStarted(value.localAddress.getPort)
  }

  override def onMessage(msg: Command): Behavior[Command] = msg match {
    case ServerStarted(port) =>
      context.log.info("server started at:{}", port)
      Behaviors.same
    case ServerFailed(ex) =>
      context.log.error("server start failed", ex)
      Behaviors.same
  }
}
