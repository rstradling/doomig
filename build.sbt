import sbt.*

ThisBuild / organization := "com.strad"
ThisBuild / scalaVersion := "3.3.1"
ThisBuild / versionScheme := Some("semver-spec")
val catsEffectVersion = "3.5.2"
val doobieVersion = "1.0.0-RC1"
val loggingVersion = "0.19.0"

lazy val root = (project in file("."))
  .aggregate(core, integration)

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
    "org.typelevel" %% "cats-effect-testing-specs2" % "1.4.0" % "test",
    "org.typelevel" %% "munit-cats-effect-3" % "1.0.7" % "test",
    "org.tpolecat" %% "doobie-munit" % doobieVersion % "test"
  )
)

lazy val core = (project in file("core"))
  .settings(
    name := "doomig",
    scalaVersion := "3.3.1",
    commonTestSettings,
    commonSettings,
    libraryDependencies ++= Seq(
      // "core" module - IO, IOApp, schedulers
      // This pulls in the kernel and std modules automatically.
      "org.typelevel" %% "cats-effect" % catsEffectVersion,
      // concurrency abstractions and primitives (Concurrent, Sync, Async etc.)
      "org.typelevel" %% "cats-effect-kernel" % catsEffectVersion,
      // standard "effect" library (Queues, Console, Random etc.)
      "org.typelevel" %% "cats-effect-std" % catsEffectVersion,
      "org.tpolecat" %% "doobie-core" % doobieVersion,
      "org.tpolecat" %% "doobie-hikari" % doobieVersion,
      "org.tpolecat" %% "doobie-postgres" % doobieVersion,
      "io.laserdisc" %% "log-effect-fs2" % loggingVersion,
      "co.fs2" %% "fs2-core" % "3.9.4",
      "co.fs2" %% "fs2-io" % "3.9.4",
      "org.log4s" %% "log4s" % "1.10.0",
      "org.rogach" %% "scallop" % "5.0.1"
    )
  )

lazy val integration = (project in file("integration"))
  .dependsOn(core)
  .settings(
    name := "integration",
    publish / skip := true,
    commonSettings,
    commonTestSettings
  )
