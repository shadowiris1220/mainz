package com.inossem
package elbing.actors.persistence

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext}
import ContextManageActor._
import akka.actor.typed.Behavior

object ContextManageActor {
  sealed trait Command

}

class ContextManageActor(context: ActorContext[Command]) extends AbstractBehavior[Command](context){
  override def onMessage(msg: Command): Behavior[Command] = ???
}