package com.inossem
package elbing.actors

import elbing.actors.persistence.ContextActor

import akka.actor.ExtendedActorSystem
import org.virtuslab.ash.annotation.Serializer
import org.virtuslab.ash.circe.{CirceAkkaSerializer, Register, Registration}

@Serializer(classOf[CirceAkkaSerializable], Register.REGISTRATION_REGEX)
class CirceSerializer(actorSystem: ExtendedActorSystem) extends CirceAkkaSerializer[CirceAkkaSerializable](actorSystem) {
  override def identifier: Int = 9527

  override lazy val codecs: Seq[Registration[_ <: CirceAkkaSerializable]] = Seq(
    Register[ContextActor.Event],
    Register[ContextActor.Command],
    Register[ContextActor.CurrentState],
    Register[ContextActor.UpdateResponse]
  )
  override lazy val manifestMigrations: Seq[(String, Class[_])] = Nil
  override lazy val packagePrefix: String = "com.inossem.elbing.actors"
}
