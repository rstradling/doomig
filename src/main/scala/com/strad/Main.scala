package com.strad

import cats.effect.{ExitCode, IO, IOApp}
import com.strad.doomig.{FileDiscoverer, Migration, MigrationService, UpMigration}

object Main extends IOApp:

  // This is your new "main"!
  def run(args: List[String]): IO[ExitCode] =
    val conf = new Conf(args)
    val upRegex = if (conf.upRegex.isEmpty) FileDiscoverer.defaultUpRegEx
    val downRegex = if (conf.downRegex.isEmpty) FileDiscoverer.defaultDownRegEx
    MigrationService.mkUpMigrationList(List.empty[UpMigration[String]])
    IO.pure(ExitCode.Success)
