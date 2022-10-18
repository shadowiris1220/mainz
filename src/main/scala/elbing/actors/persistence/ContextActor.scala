package com.inossem
package elbing.actors.persistence

import elbing.actors.CirceAkkaSerializable

import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.EventSourcedBehavior.{CommandHandler, EventHandler}
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import io.circe.generic.semiauto.deriveCodec
import io.circe.{Codec, Json}

object ContextActor {
  sealed trait Command extends CirceAkkaSerializable

  implicit lazy val codecCommand: Codec[Command] = deriveCodec
  implicit lazy val codecEvent: Codec[Event] = deriveCodec

  final case class Update(topicName: String, value: Json) extends Command

  sealed trait Event extends CirceAkkaSerializable

  final case class Updated(topicName: String, value: Json) extends Event

  final case class State(currentContext: Map[String, Json], history: List[(String, Json)])

  val commandHandler: CommandHandler[Command, Event, State] = (state, command) => {
    command match {
      case Update(topicName, value) => Effect.persist(Updated(topicName, value))
    }
  }

  val eventHandler: EventHandler[State, Event] = (state, event) => {
    event match {
      case Updated(topicName, value) => State(
        state.currentContext + (topicName -> value),
        state.history :+ (topicName, value)
      )
    }
  }

  def apply(id: String) =
    EventSourcedBehavior(
      persistenceId = PersistenceId.ofUniqueId(id),
      emptyState = State(Map.empty, List.empty),
      commandHandler = commandHandler,
      eventHandler = eventHandler
    )
}
