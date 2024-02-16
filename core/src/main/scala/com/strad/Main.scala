package com.strad

import cats.*
import cats.effect.{ExitCode, IO, IOApp}
import com.strad.doomig.service.Migrator
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Main extends IOApp:

  // This is your new "main"!
  def run(args: List[String]): IO[ExitCode] =
    val conf = new Conf(args)
    val dbConfig = conf.toDbConfig()
    val migratorConfig = conf.toMigratorConfig()
    val ret = Migrator.run(dbConfig, migratorConfig, Slf4jLogger.create[IO])
    ret.use { x =>
      val ok = x.filter(_ != 1).isEmpty
      if ok then IO.pure(ExitCode.Success)
      else IO.pure(ExitCode.Error)
    }
