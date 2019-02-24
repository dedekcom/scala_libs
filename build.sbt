name := "scala_libs"

version := "0.1"

scalaVersion := "2.12.4"

val jacksonVersion = "2.9.8"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.5" % Test,
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion
)