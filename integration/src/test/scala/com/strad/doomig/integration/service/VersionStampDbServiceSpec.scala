package com.strad.doomig.integration.service

import cats.effect.*
import cats.implicits.*
import com.strad.doomig.domain.Svc
import com.strad.doomig.domain.DomainHelpers.toSvc
import com.strad.doomig.db.*
import munit.CatsEffectSuite

/** Must specify the following environment variables DB_USER DB_PASSWORD DB_DRIVER DB_URL
  */
class VersionStampDbServiceSpec extends CatsEffectSuite:
  test("CRUD operations work as intended"):
    def run[A](tableName: String)(fn: String => IO[A]): A =
      (for res <- Resource.eval(fn(tableName))
      yield res).allocated.unsafeRunSync()._1
    def run2[A](tableName: String, migration: Svc.Migration)(fn: (String, Svc.Migration) => IO[A]): A =
      (for res <- Resource.eval(fn(tableName, migration))
      yield res).allocated.unsafeRunSync()._1

    val repo = Db
      .getConnectionFromEnv()
      .map(VersionStampDbRepo[IO](_))
      .allocated
      .unsafeRunSync()
      ._1
    run("foo")(repo.dropTableIfExists)
    assertEquals(run("foo")(repo.doesTableExist), false)
    assertEquals(run("foo")(repo.createTableIfNotExist), 0)
    assertEquals(run("foo")(repo.doesTableExist), true)
    val migration = Svc.Migration("100", "Testing", "Testing code")
    assertEquals(run2[Int]("foo", migration)(repo.writeVersion), 1)
    val currentVersion = run("foo")(repo.fetchCurrentVersion)
    val migrationCurrentVersion = currentVersion.map(_.toSvc)
    assertEquals(migrationCurrentVersion, migration.some)
    assertEquals(run("foo")(repo.dropTableIfExists), 0)
    assertEquals(run("foo")(repo.fetchCurrentVersion), None)
