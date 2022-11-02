package com.inossem
package elbing.actors.persistence

import elbing.actors.CirceAkkaSerializable

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
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
  implicit lazy val currentActor: Codec[ActorRef[CurrentState]] = new AkkaCodecs {}.actorRefCodec
  implicit lazy val stateCodec: Codec[State] = deriveCodec

  final case class UpdateResponse(version: Int)

  final case class CurrentState(state: Map[String, Json])

  final case class Update(topicName: String, value: Json, replyTo: ActorRef[UpdateResponse]) extends Command

  final case class QueryState(replyTo: ActorRef[CurrentState]) extends Command

  sealed trait Event extends CirceAkkaSerializable

  final case class Updated(topicName: String, value: Json) extends Event

  final case class State(currentContext: Map[String, Json], history: List[(String, Json)], version: Int)

  val commandHandler: ActorContext[Command] => CommandHandler[Command, Event, State] = (ctx) => (state, command) => {
    command match {
      case Update(topicName, value, replyTo) => Effect.persist(Updated(topicName, value)).thenReply[UpdateResponse](replyTo)(s => {
        ctx.log.info("the value is:{}", value.noSpaces)
        UpdateResponse(s.version)
      })
      case QueryState(replyTo) => replyTo ! CurrentState(state.currentContext)
        Effect.none
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

  val TypeKey: EntityTypeKey[Command] = EntityTypeKey[Command]("ContextActor")

  def apply(entityId: String, persistenceId: PersistenceId) = Behaviors.setup[Command](ctx => {
    ctx.log.info("Starting ContextActor:{}", entityId)
    EventSourcedBehavior(
      persistenceId = persistenceId,
      emptyState = State(Map.empty, List.empty, 0),
      commandHandler = commandHandler(ctx),
      eventHandler = eventHandler(ctx)
    )
  })
}
