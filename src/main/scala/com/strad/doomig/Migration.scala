package com.strad.doomig

import doobie.Update0

trait Migration[A]:
    def up: List[Update0]
    def down: List[Update0]
    def migrationId: A
