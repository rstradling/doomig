package com.strad.doomig.domain
import doobie.Update0

import java.time.Instant
case class Migration[A](version: A, name: String, description: String, modifiedDate: Instant)

case class MigrationAction[A](version: A, name: String, description: String, update: Update0, modifiedDate: Instant)
