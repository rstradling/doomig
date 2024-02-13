package com.strad.doomig.service

import com.strad.doomig.domain.{Migration, MigrationAction}
import doobie.Update0
import doobie.implicits.*

import java.time.Instant

object Migration2024_02_01_12_00_00 extends Migrator[String]:
  override def up(migration: Migration[String]): MigrationAction[String] =
    MigrationAction(migration.version, migration.name, migration.description, sql"""CREATE TABLE BAR();""".update, Instant.now())

  override def down(migration: Migration[String]): MigrationAction[String] =
    MigrationAction(migration.version, migration.name, migration.description, sql"""DROP TABLE FOO;""".update, Instant.now())
