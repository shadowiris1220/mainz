package com.inossem
package elbing.actors.persistence

import elbing.actors.CirceAkkaSerializable

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.EventSourcedBehavior.{CommandHandler, EventHandler}
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import io.circe.generic.semiauto.deriveCodec
import io.circe.{Codec, Json}
import org.virtuslab.ash.circe.AkkaCodecs

object ContextActor {
  sealed trait Command extends CirceAkkaSerializable

  implicit lazy val codecCommand: Codec[Command] = deriveCodec
  implicit lazy val codecEvent: Codec[Event] = deriveCodec
  implicit lazy val codecActor: Codec[ActorRef[UpdateResponse]] = new AkkaCodecs {}.actorRefCodec

  final case class UpdateResponse(version: Int)

  final case class Update(topicName: String, value: Json, replyTo: ActorRef[UpdateResponse]) extends Command

  sealed trait Event extends CirceAkkaSerializable

  final case class Updated(topicName: String, value: Json) extends Event

  final case class State(currentContext: Map[String, Json], history: List[(String, Json)], version: Int)

  val commandHandler: ActorContext[Command] => CommandHandler[Command, Event, State] = (ctx) => (state, command) => {
    command match {
      case Update(topicName, value, replyTo) => Effect.persist(Updated(topicName, value)).thenReply[UpdateResponse](replyTo)(s => {
        ctx.log.info("the value is:{}", value.noSpaces)
        UpdateResponse(s.version)
      })
    }
  }

  val eventHandler: ActorContext[Command] => EventHandler[State, Event] = (ctx) => (state, event) => {
    event match {
      case Updated(topicName, value) =>
        State(
          state.currentContext + (topicName -> value),
          state.history :+ (topicName, value),
          state.version + 1
        )
    }
  }

  def apply(id: String) = Behaviors.setup[Command](ctx =>
    EventSourcedBehavior(
      persistenceId = PersistenceId.ofUniqueId(id),
      emptyState = State(Map.empty, List.empty, 0),
      commandHandler = commandHandler(ctx),
      eventHandler = eventHandler(ctx)
    ))
}
