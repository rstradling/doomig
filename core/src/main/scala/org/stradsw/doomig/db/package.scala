package org.stradsw.doomig.db

import cats.*
import cats.effect.{IO, Resource}
import cats.implicits.*
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import doobie.{LogHandler, Transactor}
import org.stradsw.doomig.logging.DoobieLogger

object Db:
  def mkDbConnection(
    dbConfig: DbConfig,
    logHandler: Option[LogHandler[IO]]
  ): Resource[IO, Transactor[IO]] =
    for
      ce <- ExecutionContexts.fixedThreadPool[IO](5)
      xa <- HikariTransactor
        .newHikariTransactor[IO](
          "org.postgresql.Driver",
          dbConfig.url,
          dbConfig.user,
          dbConfig.password,
          ce,
          logHandler
        )
    yield xa
  def getConnectionFromEnv(logHandler: Option[LogHandler[IO]]): Resource[IO, Transactor[IO]] =
    val url = sys.env.get("DB_URL").getOrElse(throw new RuntimeException("Must specify DB_URL"))
    val user = sys.env.get("DB_USER").getOrElse(throw new RuntimeException("Must specify DB_USER"))
    val password = sys.env.get("DB_PASSWORD").getOrElse(throw new RuntimeException("Must specify DB_PASSWORD"))
    val dbDriver = sys.env.get("DB_DRIVER").getOrElse(throw new RuntimeException("Must specify DB_DRIVER"))
    mkDbConnection(DbConfig(dbDriver, url, user, password), logHandler)
