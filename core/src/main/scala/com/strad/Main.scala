package com.strad

import cats.*
import cats.implicits.*
import cats.syntax.*
import cats.effect.kernel.Resource
import cats.effect.{ExitCode, IO, IOApp}
import com.strad.doomig.db.{Db, DbConfig, VersionStampDbRepo}
import com.strad.doomig.domain.Svc
import com.strad.doomig.service.{FileDiscoveryService, MigratorFileService}
import com.strad.doomig.service.Migrator.Direction
import com.strad.doomig.domain.DomainHelpers.toSvc
import com.strad.doomig.logging.DoobieLogger
import doobie.LogHandler
import fs2.io.file.Path
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Main extends IOApp:

  // This is your new "main"!
  def run(args: List[String]): IO[ExitCode] =
    val conf = new Conf(args)
    val dbConfig = DbConfig(
      conf.dbDriver.toOption.get,
      conf.dbUrl.toOption.get,
      conf.dbUser.toOption.get,
      conf.dbPassword.toOption.get
    )
    val tableName = conf.dbTableName.getOrElse("Table Name was not specified")
    val direction = conf.direction.getOrElse(throw new RuntimeException("Unable to find a direction"))
    val regex = direction match
      case Direction.Up =>
        FileDiscoveryService.UpRegEx
      case Direction.Down =>
        FileDiscoveryService.DownRegEx
    val ret = for
      logger <- Resource.eval(Slf4jLogger.create[IO])
      doobieLogger = new DoobieLogger[IO](logger)
      db <- Db.mkDbConnection(dbConfig, doobieLogger.some)
      repo = VersionStampDbRepo(db)
      _ <- Resource.eval(repo.createTableIfNotExist(tableName))
      latestVersion <- Resource.eval(repo.fetchCurrentVersion(tableName))
      migrationFiles <- Resource.eval(
        FileDiscoveryService
          .createMigrationFilesList[IO](
            Path(conf.folder.toOption.get),
            regex,
            latestVersion.map(_.toSvc),
            conf.destinationVersion.toOption,
            direction
          )
      )
      _ <- Resource.eval(logger.info("Will run the following migrations"))
      _ <- Resource.eval(IO.delay(println("Will run the following migrations")))
      _ <- Resource.eval(
        migrationFiles.traverse(x => logger.info(s"   version: ${x.version} name: ${x.name}"))
      )
      _ <- Resource.eval(
        migrationFiles.traverse(x => IO.delay(println(s"   version: ${x.version} name: ${x.name}")))
      )
      res <- MigratorFileService.run(db, repo, tableName, conf.folder.toOption.get, migrationFiles, direction)
    yield res
    ret.use { x =>
      val ok = x.filter(_ != 1).isEmpty
      if ok then IO.pure(ExitCode.Success)
      else IO.pure(ExitCode.Error)
    }
end Main
