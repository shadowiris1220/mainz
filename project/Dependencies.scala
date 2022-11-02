import sbt._
import org.virtuslab.ash.AkkaSerializationHelperPlugin

object Dependencies {

  lazy val AkkaVersion = "2.6.14"
  lazy val AkkaHttpVersion = "10.2.10"
  lazy val SlickVersion = "3.3.3"
  lazy val AkkaManagementVersion = "1.1.4"

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
    "de.heikoseeberger" %% "akka-http-circe" % "1.39.2",
    "com.typesafe.akka" %% "akka-http-spray-json" % "10.2.10"
  )

  lazy val akkaStream = Seq(
    "com.typesafe.akka" %% "akka-stream-typed" % AkkaVersion
  )

  lazy val akkaSerialization = Seq(
    AkkaSerializationHelperPlugin.annotation,
    AkkaSerializationHelperPlugin.circeAkkaSerializer
  )

  lazy val akkaPersistenceJDBC = Seq(
    "com.lightbend.akka" %% "akka-persistence-jdbc" % "5.0.1",
    "com.typesafe.akka" %% "akka-persistence-query" % AkkaVersion,
    "com.typesafe.slick" %% "slick" % SlickVersion,
    "com.typesafe.slick" %% "slick-hikaricp" % SlickVersion
  )

  lazy val akkaCluster = Seq(
    "com.lightbend.akka.management" %% "akka-management-cluster-http" % AkkaManagementVersion exclude("com.typesafe.akka", "akka-http-spray-json") exclude("com.typesafe.akka", "akka-http-core"),
    "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % AkkaManagementVersion,
    "com.lightbend.akka.discovery" %% "akka-discovery-kubernetes-api" % AkkaManagementVersion,
    "com.typesafe.akka" %% "akka-cluster-typed" % AkkaVersion
  )

  lazy val log = Seq(
    "ch.qos.logback" % "logback-core" % "1.4.4",
    "ch.qos.logback" % "logback-classic" % "1.4.4"
  )

  lazy val circe = Seq(
    "io.circe" %% "circe-generic" % "0.14.1"
  )

  lazy val leveldb = Seq(
    "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8"
  )

  lazy val postgres = Seq(
    "org.postgresql" % "postgresql" % "42.5.0"
  )


  lazy val akka = akkaActor ++ akkaPersistence ++ akkaHttp ++ akkaStream ++ akkaSerialization ++ akkaPersistenceJDBC ++ akkaCluster

  lazy val dependency = akka ++ log ++ circe ++ leveldb ++ postgres
}
