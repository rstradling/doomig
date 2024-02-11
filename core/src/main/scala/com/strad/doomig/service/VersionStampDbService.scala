package com.strad.doomig.service

import cats.*
import cats.effect.*
import cats.implicits.*
import com.strad.doomig.domain.Migration
import doobie.*
import doobie.implicits.*
import doobie.postgres.implicits.*
import doobie.util.meta.Meta.StringMeta.given

object VersionStampDbService:
  def apply[F[_], String](db: Transactor[F])(using e: Async[F]): VersionStampService =
    new VersionStampService:
      override def doesTableExist(tableName: String): F[Boolean] =
        val where: Fragment = fr"where TABLE_NAME = $tableName"
        sql"""SELECT EXISTS (
                   SELECT *
                    FROM INFORMATION_SCHEMA.TABLES
                    WHERE TABLE_NAME = $tableName
                  );"""
          .query[Boolean]
          .option
          .map(_.getOrElse(false))
          .transact(db)

      override def dropTable(tableName: String): F[Unit] =
        sql"""DROP TABLE $tableName;""".update.run

      override def createTable(tableName: String): F[Unit] =
        sql"""CREATE TABLE ${tableName} (
             version varchar(100) PRIMARY KEY,
             name varchar(250),
             description varchar(250)
           )""".update.run

      override def fetchCurrentVersion(tableName: String): F[Option[Migration[String]]] =
        sql"""SELECT version, name, description FROM ${tableName}"""
          .query[Migration[String]]
          .option
          .transact(db)

      override def writeVersion(tableName: String, migration: Migration[String]) =
        sql"""INSERT INTO ${tableName} VALUES (${migration.version}, ${migration.name}, ${migration.description});"""
