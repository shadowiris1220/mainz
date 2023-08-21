package com.inossem
package elbing

import elbing.actors.RootActor

import akka.actor.typed.ActorSystem
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object Application extends App {
  val logger = LoggerFactory.getLogger(classOf[Application.type])
  logger.info("begin elbing:")
  val config = ConfigFactory.load().getConfig("app")
  val appName = config.getString("name")
  ActorSystem(RootActor(), appName)
}
