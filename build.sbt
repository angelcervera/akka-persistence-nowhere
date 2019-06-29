
// scalastyle:off magic.number

import sbt.Keys.{publishMavenStyle, _}
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._
import sbtrelease.ReleasePlugin.autoImport._

// Release
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  publishArtifacts,
  setNextVersion,
  commitNextVersion,
  pushChanges
)

bintrayReleaseOnPublish := false
bintrayPackageLabels := Seq("scala", "akka", "akka-persistence")

scalaVersion := "2.12.8"
organization := "com.acervera.akka"
organizationHomepage := Some(url("http://www.acervera.com"))
licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
startYear := Some(2019)
description := "Akka persistence plugin writing to nowhere. It means, ignoring all."

pomExtra :=
  <url>https://github.com/angelcervera/osm4scala</url>
    <scm>
      <connection>scm:git:git://github.com/angelcervera/akka-persistence-nowhere.git</connection>
      <developerConnection>scm:git:ssh://git@github.com/angelcervera/akka-persistence-nowhere.git</developerConnection>
      <url>https://github.com/angelcervera/akka-persistence-nowhere</url>
    </scm>
    <developers>
      <developer>
        <id>angelcervera</id>
        <name>Angel Cervera Claudio</name>
        <email>angelcervera@silyan.com</email>
      </developer>
    </developers>

lazy val akkaVersion = "2.5.23"

lazy val root = (project in file("."))
  .settings(
    name := "akka-persistence-nowhere",

    publishArtifact := true, // Enable publish
    publishMavenStyle := true,
    publishArtifact in Test := false, // No publish test stuff

    // Bintray
    bintrayRepository := "maven",
    bintrayPackage := "akka-persistence-nowhere",

    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
      "org.scalatest" %% "scalatest" % "3.0.8" % Test,
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion
    )
  )
