package com.strad.doomig.integration.service

import cats.effect.*
import cats.implicits.*
import com.strad.doomig.domain.Migration
import com.strad.doomig.service.VersionStampDbService
import doobie.Transactor
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import munit.CatsEffectSuite

import java.time.Instant

class VersionStampDbServiceSpec extends CatsEffectSuite:
  def mkDbConnection(
    host: String,
    port: String,
    database: String,
    user: String,
    password: String
  ): Resource[IO, Transactor[IO]] =
    for
      ce <- ExecutionContexts.fixedThreadPool[IO](5)
      url = s"jdbc:postgresql://${host}:${port}/${database}"
      xa <- HikariTransactor.newHikariTransactor[IO]("org.postgresql.Driver", url, user, password, ce)
    yield xa
  test("CRUD operations work as intended"):
    def run[A](tableName: String)(fn: String => IO[A]): A =
      (for res <- Resource.eval(fn(tableName))
      yield res).allocated.unsafeRunSync()._1
    def run2[A](tableName: String, migration: Migration[String])(fn: (String, Migration[String]) => IO[A]): A =
      (for res <- Resource.eval(fn(tableName, migration))
      yield res).allocated.unsafeRunSync()._1

    val repo = mkDbConnection("localhost", "5432", "testing", "admin", "test123")
      .map(VersionStampDbService[IO](_))
      .allocated
      .unsafeRunSync()
      ._1
    run("foo")(repo.dropTable)
    val time = Instant.now()
    assertEquals(run("foo")(repo.doesTableExist), false)
    assertEquals(run("foo")(repo.createTable), 0)
    assertEquals(run("foo")(repo.doesTableExist), true)
    val migration = Migration[String]("100", "Testing", "Testing code", time)
    assertEquals(run2[Int]("foo", migration)(repo.writeVersion), 1)
    assertEquals(run("foo")(repo.fetchCurrentVersion), migration.some)
    assertEquals(run("foo")(repo.dropTable), 0)
    assertEquals(run("foo")(repo.fetchCurrentVersion), None)
