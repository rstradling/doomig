package org.stradsw.doomig.service

import cats.*
import cats.effect.IO
import cats.implicits.*
import munit.CatsEffectSuite
import org.stradsw.doomig.domain.Svc
import org.stradsw.doomig.service.Migrator.Direction.{Down, Up}

class FileDiscovererSpec extends CatsEffectSuite:
  val path = "core/src/test/resources/migrations"
  test("File discovery for up files works correctly with no current version"):
    val expected = List(
      Svc.Migration("2023_02_01_12_00_00", "2023_02_01_12_00_00_up-Creating Foo.sql", "Creating Foo"),
      Svc.Migration("2024_02_01_12_00_00", "2024_02_01_12_00_00_up-Creating Bar.sql", "Creating Bar")
    )
    val items =
      FileDiscoveryService
        .createMigrationFilesList[IO](path, FileDiscoveryService.UpRegEx, None, None, Up)
        .unsafeRunSync()
    assertEquals(items, expected)
  test("Updating files when dbVersion is greater then the files should return an empty list to process"):
    val items =
      FileDiscoveryService
        .createMigrationFilesList[IO](
          path,
          FileDiscoveryService.UpRegEx,
          Svc.Migration("2025_01_01_01_00_00", "Don'tCare", "Don'tCare").some,
          None,
          Up
        )
        .unsafeRunSync()
    assertEquals(items, List.empty[Svc.Migration])
  test(
    "Updating files when dbVersion is not defined but expected version is way greater than the files, it should return all the items to process"
  ):
    val expected = List(
      Svc.Migration("2023_02_01_12_00_00", "2023_02_01_12_00_00_up-Creating Foo.sql", "Creating Foo"),
      Svc.Migration("2024_02_01_12_00_00", "2024_02_01_12_00_00_up-Creating Bar.sql", "Creating Bar")
    )
    val items =
      FileDiscoveryService
        .createMigrationFilesList[IO](
          path,
          FileDiscoveryService.UpRegEx,
          None,
          "2025_01_01_01_00_00".some,
          Up
        )
        .unsafeRunSync()
    assertEquals(items, expected)
  test(
    "Updating files when dbVersion is not defined and expected version is one of the files, it should return both items"
  ):
    val expected = List(
      Svc.Migration("2023_02_01_12_00_00", "2023_02_01_12_00_00_up-Creating Foo.sql", "Creating Foo"),
      Svc.Migration("2024_02_01_12_00_00", "2024_02_01_12_00_00_up-Creating Bar.sql", "Creating Bar")
    )
    val items =
      FileDiscoveryService
        .createMigrationFilesList[IO](
          path,
          FileDiscoveryService.UpRegEx,
          None,
          "2024_02_01_12_00_00".some,
          Up
        )
        .unsafeRunSync()
    assertEquals(items, expected)
  test(
    "Updating files when dbVersion is the max file and the ask is the max file, it should return 0 items"
  ):
    val items =
      FileDiscoveryService
        .createMigrationFilesList[IO](
          path,
          FileDiscoveryService.UpRegEx,
          Svc.Migration("2024_02_01_12_00_00", "Don'tCare", "Don'tCare").some,
          "2024_02_01_12_00_00".some,
          Up
        )
        .unsafeRunSync()
    assertEquals(items, List.empty[Svc.Migration])
  test(
    "Updating files when dbVersion is one of the files and expected version is one of the files, it should return one items"
  ):
    val expected = List(
      Svc.Migration("2024_02_01_12_00_00", "2024_02_01_12_00_00_up-Creating Bar.sql", "Creating Bar")
    )
    val items =
      FileDiscoveryService
        .createMigrationFilesList[IO](
          path,
          FileDiscoveryService.UpRegEx,
          Svc.Migration("2023_02_01_12_00_00", "Don'tCare", "Don'tCare").some,
          "2024_02_01_12_00_00".some,
          Up
        )
        .unsafeRunSync()
    assertEquals(items, expected)
  test(
    "Updating files when dbVersion is defined and is one of the files and expected version is not defined, it should return one item"
  ):
    val expected = List(
      Svc.Migration("2024_02_01_12_00_00", "2024_02_01_12_00_00_up-Creating Bar.sql", "Creating Bar")
    )
    val items =
      FileDiscoveryService
        .createMigrationFilesList[IO](
          path,
          FileDiscoveryService.UpRegEx,
          Svc.Migration("2023_02_01_12_00_00", "Don'tCare", "Don'tCare").some,
          None,
          Up
        )
        .unsafeRunSync()
    assertEquals(items, expected)
  test("File discovery for down files works correctly with no current version"):
    val items =
      FileDiscoveryService
        .createMigrationFilesList[IO](path, FileDiscoveryService.DownRegEx, None, None, Down)
        .unsafeRunSync()
    assertEquals(items, List.empty)
end FileDiscovererSpec
