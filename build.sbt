
ThisBuild / scalaVersion     := "2.12.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.acervera.akka"
ThisBuild / organizationHomepage := Some(url("http://www.acervera.com"))
ThisBuild / licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
ThisBuild / startYear := Some(2019)
ThisBuild / description := "A build tool for Scala."

lazy val akkaVersion = "2.5.23"

lazy val root = (project in file("."))
  .settings(
    name := "akka-persistence-nowhere",
    libraryDependencies ++=Seq(
      "com.typesafe.akka"   %% "akka-actor" % akkaVersion,
      "com.typesafe.akka"   %% "akka-persistence" % akkaVersion,
      "org.scalatest" %% "scalatest" % "3.0.8" % Test,
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
    )
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
