package com.strad.doomig.service

import cats.*
import cats.effect.*
import cats.implicits.*
import com.strad.doomig.config.MigratorConfig
import com.strad.doomig.db.{Db, DbConfig, VersionStampPostgresRepo, VersionStampRepo}
import com.strad.doomig.domain.DomainHelpers.toSvc
import com.strad.doomig.logging.DoobieLogger
import fs2.io.file.Path
import org.typelevel.log4cats.SelfAwareStructuredLogger

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
      repo = VersionStampRepo(dbConfig.driver, db)
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
      _ <- Resource.eval(
        migrationFiles.traverse(x => logger.info(s"   version: ${x.version} name: ${x.name}"))
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
