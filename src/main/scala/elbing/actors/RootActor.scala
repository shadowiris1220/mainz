package com.inossem
package elbing.actors

import elbing.actors.http.ServerActor

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import com.inossem.elbing.actors.persistence.ContextManageActor

object RootActor {
  sealed trait Command

  def apply(): Behavior[Command] = Behaviors.setup(new RootActor(_))
}

class RootActor(context: ActorContext[RootActor.Command]) extends AbstractBehavior[RootActor.Command](context) {
  private val persistenceManager = context.spawn(ContextManageActor(), "persistence-manager")
  private val server = context.spawn(ServerActor(persistenceManager), "http-server")

  override def onMessage(msg: RootActor.Command): Behavior[RootActor.Command] = Behaviors.ignore
}
