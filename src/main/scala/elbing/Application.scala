package com.inossem
package elbing

import elbing.actors.RootActor

import akka.actor.typed.ActorSystem
import akka.persistence.jdbc.testkit.scaladsl.SchemaUtils
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object Application extends App {
  LoggerFactory.getLogger(classOf[Application.type])
  val config = ConfigFactory.load().getConfig("app")
  val appName = config.getString("name")
  implicit val actorSystem = ActorSystem(RootActor(), appName)
  SchemaUtils.createIfNotExists()
}
