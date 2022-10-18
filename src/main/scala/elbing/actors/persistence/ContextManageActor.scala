package com.inossem
package elbing.actors.persistence

import elbing.actors.persistence.ContextManageActor._

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.pattern._
import akka.util.Timeout
import io.circe.generic.semiauto.deriveCodec
import io.circe.{Codec, Json}

import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}
import scala.util.chaining._

object ContextManageActor {
  sealed trait Command

  final case class Update(id: String, topicName: String, value: Json, replyTo: ActorRef[Updated]) extends Command

  final case class UpdateResult(success: Boolean) extends Command

  final case class Updated(currentVersion: Int)
  implicit val codec: Codec[Updated] = deriveCodec


  def apply(): Behavior[Command] = Behaviors.setup(new ContextManageActor(_))
}

class ContextManageActor(context: ActorContext[Command]) extends AbstractBehavior[Command](context) {
  var map: Map[String, ActorRef[ContextActor.Command]] = Map.empty

  override def onMessage(msg: Command): Behavior[Command] = msg match {
    case Update(id, topicName, value, replyTo) =>
      implicit val timeout: Timeout = 3.seconds
      val actor = if (map.contains(id)) {
        map(id)
      } else {
        context.spawn(ContextActor(id), id).tap(a => map = map + (id -> a))
      }
      context.ask[ContextActor.Command, ContextActor.UpdateResponse](actor, reply => ContextActor.Update(topicName, value, reply)) {
        case Failure(exception) => UpdateResult(false)
        case Success(value) =>
          replyTo ! Updated(value.version)
          UpdateResult(true)
      }
      Behaviors.same
    case UpdateResult(success) =>
      context.log.info("update result success ? {}", success)
      Behaviors.same
  }
}