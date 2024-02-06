package com.strad.doomig

import cats.effect.IO
import fs2.io.file.Path
import munit.CatsEffectSuite

class FileDiscovererSpec extends CatsEffectSuite:
  test("File discovery for up files works correctly"):
    val path = "src/test/resources/migrations"
    val expected = List(Path(path + "/2024_02_01_12_00_00_up-Creating Bar.sql"),
      Path(path + "/2023_02_01_12_00_00_up-Creating Foo.sql"))
    val items = FileDiscoverer.getMigrationFiles[IO](Path(path), FileDiscoverer.defaultUpRegEx).compile.toList
    items.map(x => assertEquals(x, expected))

  test("File discovery for down files works correctly"):
    val path = "src/test/resources/migrations"
    val expected = List(Path(path + "/2023_02_01_12_00_00_down-Dropping Foo.sql"),
      Path(path + "/2024_02_01_12_00_00_down-Dropping Bar.sql"))
    val items = FileDiscoverer.getMigrationFiles[IO](Path(path), FileDiscoverer.defaultDownRegEx).compile.toList
    items.map(x => assertEquals(x, expected))
