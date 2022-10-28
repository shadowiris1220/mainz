ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

Compile / scalacOptions += "-P:serializability-checker-plugin:--disable-detection-generics"

Compile / scalacOptions += "-P:serializability-checker-plugin:--disable-detection-generic-methods"

Compile / scalacOptions += "-P:serializability-checker-plugin:--disable-detection-methods"

enablePlugins(JavaAppPackaging)
lazy val root = (project in file("."))
  .enablePlugins(AkkaSerializationHelperPlugin)
  .settings(
    name := "Elbing",
    idePackagePrefix := Some("com.inossem"),
    libraryDependencies ++= Dependencies.dependency,
    scriptClasspath := Seq("*"),
    Compile / mainClass := Some("com.inossem.elbing.Application"),
    packageDoc / publishArtifact := false
  )
