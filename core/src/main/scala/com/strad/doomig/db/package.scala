package com.strad.doomig.db

import cats.effect.{IO, Resource}
import doobie.Transactor
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts

object Db:
  def mkDbConnection(
    dbConfig: DbConfig
  ): Resource[IO, Transactor[IO]] =
    for
      ce <- ExecutionContexts.fixedThreadPool[IO](5)
      xa <- HikariTransactor
        .newHikariTransactor[IO]("org.postgresql.Driver", dbConfig.url, dbConfig.user, dbConfig.password, ce)
    yield xa
  def getConnectionFromEnv(): Resource[IO, Transactor[IO]] =
    val url = sys.env.get("DB_URL").getOrElse(throw new RuntimeException("Must specify DB_URL"))
    val user = sys.env.get("DB_USER").getOrElse(throw new RuntimeException("Must specify DB_USER"))
    val password = sys.env.get("DB_PASSWORD").getOrElse(throw new RuntimeException("Must specify DB_PASSWORD"))
    val dbDriver = sys.env.get("DB_DRIVER").getOrElse(throw new RuntimeException("Must specify DB_DRIVER"))
    mkDbConnection(DbConfig(dbDriver, url, user, password))
