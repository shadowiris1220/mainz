package com.inossem
package elbing.domain

import io.circe.{Decoder, Encoder, Json}
import io.circe.generic.semiauto._

case class UpdateContext(id: String, topicName: String, context: Json)

object UpdateContext{
  implicit val encoder: Encoder[UpdateContext] = deriveEncoder[UpdateContext]
  implicit val decoder: Decoder[UpdateContext] = deriveDecoder[UpdateContext]
}
