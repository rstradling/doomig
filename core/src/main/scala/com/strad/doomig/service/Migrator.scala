package com.strad.doomig.service

import cats.*
import cats.implicits.*
import cats.effect.*
import com.strad.doomig.config.MigratorConfig
import com.strad.doomig.db.{Db, DbConfig, VersionStampDbRepo}
import com.strad.doomig.domain.DomainHelpers.toSvc
import com.strad.doomig.logging.DoobieLogger
import com.strad.doomig.service.Migrator.Direction
import fs2.io.file.Path
import org.typelevel.log4cats.SelfAwareStructuredLogger

import scala.util.matching.Regex


object Migrator:
  enum Direction:
    case Up, Down

  def run(
    dbConfig: DbConfig,
    migratorConfig: MigratorConfig,
    logger: IO[SelfAwareStructuredLogger[IO]]
  ): Resource[IO, List[Int]] =
    for
      logger <- Resource.eval(logger)
      doobieLogger = new DoobieLogger[IO](logger)
      db <- Db.mkDbConnection(dbConfig, doobieLogger.some)
      repo = VersionStampDbRepo(db)
      _ <- Resource.eval(repo.createTableIfNotExist(migratorConfig.tableName))
      latestVersion <- Resource.eval(repo.fetchCurrentVersion(migratorConfig.tableName))
      migrationFiles <- Resource.eval(
        FileDiscoveryService
          .createMigrationFilesList[IO](
            Path(migratorConfig.sourceFolder),
            migratorConfig.fileRegex,
            latestVersion.map(_.toSvc),
            migratorConfig.destinationVersion,
            migratorConfig.direction
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
      res <- MigratorFileService.run(
        db,
        repo,
        migratorConfig.tableName,
        migratorConfig.sourceFolder,
        migrationFiles,
        migratorConfig.direction,
        migratorConfig.dryRun
      )
    yield res
