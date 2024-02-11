package com.strad.doomig.service

import com.strad.doomig.domain.{Migration, MigrationAction}
import doobie.Update0
import doobie.implicits.*

object Migration2023_02_01_12_00_00 extends Migrator[String]:
  override def up(migration: Migration[String]): MigrationAction[String] =
    MigrationAction(migration.version, migration.name, migration.desc, sql"""CREATE TABLE FOO();""".update)

  override def down(migration: Migration[String]): MigrationAction[String] =
    MigrationAction(migration.version, migration.name, migration.desc, sql"""DROP TABLE FOO;""".update)
