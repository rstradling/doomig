package com.strad

import cats.effect.{ExitCode, IO, IOApp}
import com.strad.doomig.domain.Migration
import com.strad.doomig.service.FileDiscoveryService
import fs2.io.file.Path

import scala.util.matching.Regex

object Main extends IOApp:

  // This is your new "main"!
  def run(args: List[String]): IO[ExitCode] =
    val conf = new Conf(args)
      
    /* conf.direction.getOrElse(throw new RuntimeException("Must specify a direction")) match
      case MigrationService.Direction.Up =>

        val migrationFiles = FileDiscoveryService.createMigrationFiles[IO](Path(conf.folder.name), conf.upRegex.toOption.get)
        MigrationService.mkUpMigrationList(List.empty[UpMigration[String]])
      case MigrationService.Direction.Down =>
        val migrationFiles = FileDiscoveryService.createMigrationFiles[IO](Path(conf.folder.name), conf.downRegex.toOption.get)
        MigrationService.mkDownMigrationList(List.empty[DownMigration[String]])

     */
    IO.pure(ExitCode.Success)
