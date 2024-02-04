ThisBuild / organization := "com.strad"
ThisBuild / scalaVersion := "3.3.1"
val catsEffectVersion = "3.5.2"
val doobieVersion = "1.0.0-RC1"

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    Defaults.itSettings,
    name := "doomig",
    libraryDependencies ++= Seq(
      // "core" module - IO, IOApp, schedulers
      // This pulls in the kernel and std modules automatically.
      "org.typelevel" %% "cats-effect" % catsEffectVersion,
      // concurrency abstractions and primitives (Concurrent, Sync, Async etc.)
      "org.typelevel" %% "cats-effect-kernel" % catsEffectVersion,
      // standard "effect" library (Queues, Console, Random etc.)
      "org.typelevel" %% "cats-effect-std" % catsEffectVersion,
      "org.tpolecat"%% "doobie-core" % doobieVersion,
      "org.typelevel" %% "cats-effect-testing-specs2" % "1.4.0" % "it,test",
      "org.typelevel" %% "munit-cats-effect-3" % "1.0.7" % "it,test",
      "org.tpolecat"    %% "doobie-munit"        % doobieVersion          % "it,test",
      "org.tpolecat"%% "doobie-postgres" % doobieVersion % "it,test",
    )
)
