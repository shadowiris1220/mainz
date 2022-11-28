package com.inossem
package elbing.actors

import elbing.actors.http.ServerActor
import elbing.actors.persistence.ContextManageActor

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import com.typesafe.config.ConfigFactory

object RootActor {
  sealed trait Command

  def apply(): Behavior[Command] = Behaviors.setup(new RootActor(_))
}

class RootActor(context: ActorContext[RootActor.Command]) extends AbstractBehavior[RootActor.Command](context) {
  private val persistenceManager = context.spawn(ContextManageActor(), "persistence-manager")
  private val server = context.spawn(ServerActor(persistenceManager), "http-server")
  private val config = ConfigFactory.load()
  private val local = config.getBoolean("app.local")
  if (!local) {
    val system = context.system
    AkkaManagement(system).start()
    ClusterBootstrap(system).start()
  } else {
    context.log.info("actor will run in single node")
  }

  override def onMessage(msg: RootActor.Command): Behavior[RootActor.Command] = Behaviors.ignore
}
