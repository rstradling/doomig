package org.stradsw.doomig.integration.service

import cats.effect.*
import cats.implicits.*
import munit.CatsEffectSuite
import org.stradsw.doomig.db.*
import org.stradsw.doomig.domain.DomainHelpers.toSvc
import org.stradsw.doomig.domain.Svc

/** Must specify the following environment variables DB_USER DB_PASSWORD DB_DRIVER DB_URL
  */
class VersionStampDbServiceSpec extends CatsEffectSuite:
  test("CRUD operations work as intended"):
    val tableName = "version_stamp_db_service_spec"
    def run[A](tableName: String)(fn: String => IO[A]): A =
      (for res <- Resource.eval(fn(tableName))
      yield res).allocated.unsafeRunSync()._1
    def run2[A](tableName: String, migration: Svc.Migration)(fn: (String, Svc.Migration) => IO[A]): A =
      (for res <- Resource.eval(fn(tableName, migration))
      yield res).allocated.unsafeRunSync()._1

    val repo = Db
      .getConnectionFromEnv(None)
      .map(VersionStampPostgresRepo[IO](_))
      .allocated
      .unsafeRunSync()
      ._1
    run(tableName)(repo.dropTableIfExists)
    assertEquals(run(tableName)(repo.doesTableExist), false)
    assertEquals(run(tableName)(repo.createTableIfNotExist), 0)
    assertEquals(run(tableName)(repo.doesTableExist), true)
    val migration = Svc.Migration("100", "Testing", "Testing code")
    val migration2 = Svc.Migration("101", "Testing", "Testing code")
    assertEquals(run2[Int](tableName, migration)(repo.writeVersion), 1)
    assertEquals(run2[Int](tableName, migration2)(repo.writeVersion), 1)
    val currentVersion = run(tableName)(repo.fetchCurrentVersion)
    assertEquals(currentVersion.map(_.toSvc), migration2.some)
    assertEquals(run(tableName)(repo.dropTableIfExists), 0)
    assertEquals(run(tableName)(repo.fetchCurrentVersion), None)
