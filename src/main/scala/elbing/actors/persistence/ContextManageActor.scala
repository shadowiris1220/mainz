package com.inossem
package elbing.actors.persistence

import elbing.actors.persistence.ContextManageActor._

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior, SupervisorStrategy}
import akka.util.Timeout
import io.circe.generic.semiauto.deriveCodec
import io.circe.{Codec, Json}

import scala.concurrent.duration.DurationInt
import scala.util.chaining._
import scala.util.{Failure, Success}

object ContextManageActor {
  sealed trait Command

  final case class Update(id: String, topicName: String, value: Json, replyTo: ActorRef[Updated]) extends Command

  final case class CommandResult(success: Boolean) extends Command

  final case class QueryState(id: String, replyTo: ActorRef[CurrentState]) extends Command

  final case class Updated(currentVersion: Int)

  final case class CurrentState(state: Option[Map[String,Json]])

  implicit val codec: Codec[Updated] = deriveCodec

  implicit val stateCodec: Codec[CurrentState] = deriveCodec


  def apply(): Behavior[Command] = Behaviors.supervise(Behaviors.setup(new ContextManageActor(_))).onFailure(SupervisorStrategy.restart)
}

class ContextManageActor(context: ActorContext[Command]) extends AbstractBehavior[Command](context) {
  var map: Map[String, ActorRef[ContextActor.Command]] = Map.empty
  implicit val timeout: Timeout = 10.seconds

  override def onMessage(msg: Command): Behavior[Command] = msg match {
    case Update(id, topicName, value, replyTo) =>
      val actor = loadActor(id)
      context.ask[ContextActor.Command, ContextActor.UpdateResponse](actor, reply => ContextActor.Update(topicName, value, reply)) {
        case Failure(exception) => CommandResult(false)
        case Success(value) =>
          replyTo ! Updated(value.version)
          CommandResult(true)
      }
      Behaviors.same
    case CommandResult(success) =>
      context.log.info("command result success ? {}", success)
      Behaviors.same

    case QueryState(id, replyTo) =>
      val actor = loadActor(id)
      context.ask[ContextActor.Command, ContextActor.CurrentState](actor, reply => ContextActor.QueryState(reply)) {
        case Failure(exception) => replyTo ! CurrentState(None)
          CommandResult(false)
        case Success(value) => replyTo ! CurrentState(Some(value.state))
          CommandResult(true)
      }
      Behaviors.same
  }

  private def loadActor(id: String) = {
    if (map.contains(id)) {
      map(id)
    } else {
      context.spawn(ContextActor(id), id).tap(a => map = map + (id -> a))
    }
  }
}