package org.stradsw.doomig.db

import cats.*
import cats.effect.*
import cats.implicits.*
import doobie.*
import doobie.implicits.*
import doobie.postgres.implicits.*
import doobie.util.transactor.Transactor
import org.stradsw.doomig.domain.{Dao, Svc}

import java.time.Instant

object VersionStampPostgresRepo:
  def apply[F[_]: Async](db: Transactor[F]): VersionStampRepo[F] =
    new VersionStampRepo[F]:
      override def doesTableExist(tableName: String): F[Boolean] =
        sql"""SELECT EXISTS (
                   SELECT *
                    FROM INFORMATION_SCHEMA.TABLES
                    WHERE TABLE_NAME = $tableName
                  );"""
          .query[Boolean]
          .option
          .map(_.getOrElse(false))
          .transact(db)

      override def dropTableIfExists(tableName: String): F[Int] =
        val s = s"""DROP TABLE IF EXISTS $tableName;"""
        Update[Unit](s, None).toUpdate0(()).run.transact(db)

      override def createTableIfNotExist(tableName: String): F[Int] =
        val s =
          s"""CREATE TABLE IF NOT EXISTS $tableName (
             version varchar(100) PRIMARY KEY,
             name varchar(250),
             description varchar(250),
             modified_date timestamp with time zone);
             """

        Update[Unit](s, None).toUpdate0(()).run.transact(db)

      override def fetchCurrentVersion(tableName: String): F[Option[Dao.Migration]] =
        val exists = doesTableExist(tableName)
        exists.flatMap { e =>
          if e then
            val select = fr"""SELECT version, name, description, modified_date FROM""" ++ Fragment.const(tableName)
            val orderBy = fr"""ORDER BY version DESC LIMIT 1"""
            (select ++ orderBy)
              .query[Dao.Migration]
              .option
              .transact(db)
          else Async[F].pure(None)
        }

      override def writeVersion(tableName: String, migration: Svc.Migration): F[Int] =
        val s = fr"""INSERT INTO""" ++ Fragment.const(tableName)
        val v =
          fr"""VALUES (${migration.version}, ${migration.name}, ${migration.description}, ${Instant.now});"""
        (s ++ v).update.run.transact(db)

      override def deleteVersion(tableName: String, version: String): F[Int] =
        val s = fr"""DELETE FROM""" ++ Fragment.const(tableName)
        val where = fr"""WHERE version=${version}"""
        (s ++ where).update.run.transact(db)
end VersionStampPostgresRepo
