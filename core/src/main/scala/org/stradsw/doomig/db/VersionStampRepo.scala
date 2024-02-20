package org.stradsw.doomig.db

import cats.effect.Async
import doobie.util.transactor.Transactor
import org.stradsw.doomig.domain.Dao.Migration
import org.stradsw.doomig.domain.{Dao, Svc}

trait VersionStampRepo[F[_]]:
  def doesTableExist(tableName: String): F[Boolean]
  def fetchCurrentVersion(tableName: String): F[Option[Migration]]
  def writeVersion(tableName: String, migration: Svc.Migration): F[Int]
  def deleteVersion(tableName: String, version: String): F[Int]
  def createTableIfNotExist(tableName: String): F[Int]
  def dropTableIfExists(tableName: String): F[Int]

object VersionStampRepo:
  def apply[F[_]: Async](driver: String, db: Transactor[F]) =
    driver.toLowerCase() match
      case "org.postgresql.driver" => VersionStampPostgresRepo(db)
      case _                       => throw new RuntimeException("Unsupported driver")
