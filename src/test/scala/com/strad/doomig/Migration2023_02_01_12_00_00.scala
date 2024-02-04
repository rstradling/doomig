package com.strad.doomig

import doobie.Update0
import doobie.implicits.*

object Migration2023_02_01_12_00_00 extends Migration[String] {
  override def up: List[Update0] = List(sql"""CREATE TABLE FOO();""".update)

  override def down: List[Update0] = List(sql"""DROP TABLE FOO;""".update)

  override val migrationId: String = "2023_02_01_12_00_00"
}
