package com.strad.doomig.service

import com.strad.doomig.service.{Migration2023_02_01_12_00_00, Migration2024_02_01_12_00_00}
import munit.CatsEffectSuite

// TODO: FIX ME
class MigrationServiceSpec extends CatsEffectSuite:
  def foo = ???
/*
class MigrationServiceSpec extends CatsEffectSuite:
  test("migration list up sorts correctly when already sorted in order"):
    val l = List(Migration2023_02_01_12_00_00, Migration2024_02_01_12_00_00)
    val expectedList = List("CREATE TABLE FOO();", "CREATE TABLE BAR();")
    val retList = MigrationService.mkUpMigrationList(l).map(_.sql)
    assertEquals(retList, expectedList)

  test("migration list up sorts correctly when already sorted opposite order"):
    val l = List(Migration2024_02_01_12_00_00, Migration2023_02_01_12_00_00)
    val expectedList = List("CREATE TABLE FOO();", "CREATE TABLE BAR();")
    val retList = MigrationService.mkUpMigrationList(l).map(_.sql)
    assertEquals(retList, expectedList)

  test("migration list down sorts correctly when already sorted opposite order"):
    val l = List(Migration2024_02_01_12_00_00, Migration2023_02_01_12_00_00)
    val expectedList = List("DROP TABLE BAR;", "DROP TABLE FOO;")
    val retList = MigrationService.mkDownMigrationList(l).map(_.sql)
    assertEquals(retList, expectedList)

  test("migration list down sorts correctly when already sorted opposite order"):
    val l = List(Migration2023_02_01_12_00_00, Migration2024_02_01_12_00_00)
    val expectedList = List("DROP TABLE BAR;", "DROP TABLE FOO;")
    val retList = MigrationService.mkDownMigrationList(l).map(_.sql)
    assertEquals(retList, expectedList)
 */
