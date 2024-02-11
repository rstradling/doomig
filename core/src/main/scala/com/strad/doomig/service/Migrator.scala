package com.strad.doomig.service

import com.strad.doomig.domain.{Migration, MigrationAction}
import doobie.Update0
trait Migrator[A]:
  def down(migration: Migration[A]): MigrationAction[A]
  def up(migration: Migration[A]): MigrationAction[A] 
  
object Migrator:
  enum Direction:
    case Up, Down
