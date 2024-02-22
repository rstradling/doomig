import sbt.*
import sbtrelease.ReleaseStateTransformations.*

ThisBuild / organization := "org.stradsw"
ThisBuild / scalaVersion := "3.3.1"
ThisBuild / versionScheme := Some("semver-spec")
ThisBuild / credentials += Credentials(Path.userHome / ".sbt" / "sonatype_credentials")
ThisBuild / publishMavenStyle := true
ThisBuild / publishArtifact / test := false
ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / homepage := Some(url("https://github.com/rstradling/doomig"))
ThisBuild / releaseProcess := Seq[ReleaseStep](
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
  ReleaseStep(action = Command.process("sonatypeReleaseAll", _)),
  pushChanges
)
// format: off
ThisBuild / publishTo := {
  val nexus = "https://central.sonatype.com/"
  //if (isSnapshot.value) Some("snapshots".at(nexus + "content/repositories/snapshots"))
  /*else*/ Some("releases".at(nexus))// + "service/local/staging/deploy/maven2"))
}
ThisBuild / releasePublishArtifactsAction := PgpKeys.publishSigned.value
// format: on
ThisBuild / licenses := Seq("Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/rstradling/doomig"),
    "scm:git:git@github.com:rstradling/doomig.git"
  )
)
ThisBuild / developers := List(
  Developer("rstradling", "Ryan Stradling", "ryanstradling@gmail.com", url("http:me.com"))
)

val catsEffectVersion = "3.5.2"
val doobieVersion = "1.0.0-RC5"
val loggingVersion = "0.19.0"

lazy val root = (project in file("."))
  .aggregate(core, integration, app)

lazy val commonSettings = Seq(
  // build.sbt, for Scala 3 project
  scalacOptions ++= Seq(
    "-new-syntax",
    "-indent",
    "-Xfatal-warnings",
    "-deprecation",
    "-feature",
    "-unchecked"
  )
  // scalacOptions ++= Seq("-indent", "-rewrite"),
)
lazy val commonTestSettings = Seq(
  libraryDependencies ++= Seq(
    "org.typelevel" %% "munit-cats-effect-3" % "1.0.7" % "test",
    "org.tpolecat" %% "doobie-munit" % doobieVersion % "test"
  )
)

lazy val app = (project in file("app"))
  .dependsOn(core)
  .settings(
    name := "doomig-app",
    commonSettings
  )

lazy val core = (project in file("core"))
  .settings(
    name := "doomig-core",
    commonTestSettings,
    commonSettings,
    libraryDependencies ++= Seq(
      // "core" module - IO, IOApp, schedulers
      // This pulls in the kernel and std modules automatically.
      "ch.qos.logback" % "logback-classic" % "1.1.2",
      "org.typelevel" %% "cats-effect" % catsEffectVersion,
      // concurrency abstractions and primitives (Concurrent, Sync, Async etc.)
      "org.typelevel" %% "cats-effect-kernel" % catsEffectVersion,
      // standard "effect" library (Queues, Console, Random etc.)
      "org.typelevel" %% "cats-effect-std" % catsEffectVersion,
      "org.tpolecat" %% "doobie-core" % doobieVersion,
      "org.tpolecat" %% "doobie-hikari" % doobieVersion,
      "org.tpolecat" %% "doobie-postgres" % doobieVersion,
      "org.typelevel" %% "log4cats-core" % "2.6.0",
      "org.typelevel" %% "log4cats-slf4j" % "2.6.0",
      "org.rogach" %% "scallop" % "5.0.1"
    )
  )

lazy val integration = (project in file("integration"))
  .dependsOn(core)
  .settings(
    name := "doomig-integration",
    publish / skip := true,
    commonSettings,
    commonTestSettings
  )
