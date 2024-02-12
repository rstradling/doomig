package com.strad.doomig.service

import cats.*
import cats.implicits.*
import cats.syntax.*
import cats.effect.*
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
        sql"""DROP TABLE $tableName;""".update.run.transact(db)

      override def createTable(tableName: String): F[Int] =
        sql"""CREATE TABLE ${tableName} (
             version varchar(100) PRIMARY KEY,
             name varchar(250),
             description varchar(250)
           )""".update.run
          .transact(db)

      override def fetchCurrentVersion(tableName: String): F[Option[Migration[String]]] =
        sql"""SELECT version, name, description FROM ${tableName}"""
          .query[Migration[String]]
          .option
          .transact(db)

      override def writeVersion(tableName: String, migration: Migration[String]): F[Int] =
        sql"""INSERT INTO ${tableName} 
             VALUES (${migration.version}, ${migration.name}, ${migration.description});""".update.run
          .transact(db)
