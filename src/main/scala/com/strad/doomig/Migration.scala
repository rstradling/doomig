package com.strad.doomig

import doobie.Update0

trait Migration[A]:
    def migrationId: A

trait UpMigration[A] extends Migration[A]:
    def up: List[Update0]
trait DownMigration[A] extends Migration[A]:
    def down: List[Update0]
