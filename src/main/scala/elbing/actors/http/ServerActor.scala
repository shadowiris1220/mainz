package com.inossem
package elbing.actors.http

import elbing.actors.http.ServerActor._
import elbing.actors.persistence.ContextManageActor
import elbing.domain.UpdateContext

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

object ServerActor {
  sealed trait Command

  private final case class ServerStarted(port: Int) extends Command

  private final case class ServerFailed(ex: Throwable) extends Command

  def apply(manager: ActorRef[ContextManageActor.Command]): Behavior[Command] = Behaviors.setup(new ServerActor(_, manager))
}

class ServerActor(context: ActorContext[Command], manager: ActorRef[ContextManageActor.Command]) extends AbstractBehavior(context) {
  implicit private val sys = context.system
  implicit private val ec = sys.executionContext
  private val config = ConfigFactory.load().getConfig("app")
  private val port = config.getInt("port")
  private val logger = context.log
  private implicit val timeout: Timeout = 10.seconds

  private val homeRouter = path("home") {
    get {
      complete(StatusCodes.NoContent)
    }
  }
  private val apiPath = pathPrefix("api")
  private val contextRouter = apiPath(
    path("context") {
      put {
        entity(as[UpdateContext]) { updateContext =>
          logger.info("updateContext:{}", updateContext)
          val f = manager ?[ContextManageActor.Updated] (actor => ContextManageActor.Update(updateContext.id, updateContext.topicName, updateContext.context, actor))
          complete(f)
        }
      } ~ get {
        parameter("id".as[String]) { id =>
          val f = manager ?[ContextManageActor.CurrentState] (actor => ContextManageActor.QueryState(id, actor))
          complete(f.map(_.state))
        }
      }
    }
  )

  private val router = homeRouter ~ contextRouter
  private val binding = Http().newServerAt("0.0.0.0", port).bind(router)
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
