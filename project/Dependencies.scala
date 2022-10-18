import sbt._
import org.virtuslab.ash.AkkaSerializationHelperPlugin

object Dependencies {

  lazy val AkkaVersion = "2.6.14"
  lazy val AkkaHttpVersion = "10.2.10"

  lazy val akkaActor = Seq(
    "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
    "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test
  )

  lazy val akkaPersistence = Seq(
    "com.typesafe.akka" %% "akka-persistence-typed" % AkkaVersion,
    "com.typesafe.akka" %% "akka-persistence-testkit" % AkkaVersion % Test
  )
  lazy val akkaHttp = Seq(
    "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
    "de.heikoseeberger" %% "akka-http-circe" % "1.39.2"
  )

  lazy val akkaStream = Seq(
    "com.typesafe.akka" %% "akka-stream-typed" % AkkaVersion
  )

  lazy val akkaSerialization = Seq(
    AkkaSerializationHelperPlugin.annotation,
    AkkaSerializationHelperPlugin.circeAkkaSerializer
  )

  lazy val log = Seq(
    "ch.qos.logback" % "logback-core" % "1.4.4",
    "ch.qos.logback" % "logback-classic" % "1.4.4"
  )

  lazy val circe = Seq(
    "io.circe" %% "circe-generic" % "0.14.1"
  )


  lazy val akka = akkaActor ++ akkaPersistence ++ akkaHttp ++ akkaStream ++ akkaSerialization

  lazy val dependency = akka ++ log ++ circe
}
