package com.strad.doomig.domain
import doobie.Update0
case class Migration[A](version: A, name: String, description: String)

case class MigrationAction[A](version: A, name: String, description: String, update: Update0)
