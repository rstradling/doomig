package com.strad.doomig.service

import cats.*
import cats.effect.*
import cats.implicits.*
import com.strad.doomig.domain.Migration
import doobie.util.transactor.Transactor
import doobie.*
import doobie.implicits.*

object VersionStampDbService:
  def apply[F[_]: Async](db: Transactor[F]): VersionStampService[F, String] =
    new VersionStampService[F, String]:
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

      override def dropTable(tableName: String): F[Int] =
        val s = s"""DROP TABLE IF EXISTS $tableName;"""
        Update[Unit](s, None).toUpdate0(()).run.transact(db)

      override def createTable(tableName: String): F[Int] =
        val s =
          s"""CREATE TABLE $tableName (
             version varchar(100) PRIMARY KEY,
             name varchar(250),
             description varchar(250));
             """

        Update[Unit](s, None).toUpdate0(()).run.transact(db)

      override def fetchCurrentVersion(tableName: String): F[Option[Migration[String]]] =
        val exists = doesTableExist(tableName)
        exists.flatMap { e =>
          if e then
            val select = fr"""SELECT version, name, description FROM""" ++ Fragment.const(tableName)
            select
              .query[Migration[String]]
              .option
              .transact(db)
          else Async[F].pure(None)
        }

      override def writeVersion(tableName: String, migration: Migration[String]): F[Int] =
        val s = fr"""INSERT INTO""" ++ Fragment.const(tableName)
        val v = fr"""VALUES (${migration.version}, ${migration.name}, ${migration.description});"""
        (s ++ v).update.run.transact(db)
