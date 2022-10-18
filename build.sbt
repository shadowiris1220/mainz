ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

Compile / scalacOptions += "-P:serializability-checker-plugin:--disable-detection-generics"

lazy val root = (project in file("."))
  .enablePlugins(AkkaSerializationHelperPlugin)
  .settings(
    name := "Elbing",
    idePackagePrefix := Some("com.inossem"),
    libraryDependencies ++= Dependencies.dependency
  )
