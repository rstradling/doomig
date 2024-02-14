package com.strad.doomig.domain

import doobie.Update0

import java.time.Instant

object Svc:
  sealed trait MigrationRoot
  case class Migration(version: String, name: String, description: String) extends MigrationRoot

  case class MigrationEmpty() extends MigrationRoot

object Dao:
  case class Migration(version: String, name: String, description: String, modifiedDate: Instant)

object DomainHelpers:
  extension (m: Svc.Migration) def toDao: Dao.Migration = Dao.Migration(m.version, m.name, m.description, Instant.now)
  extension (m: Dao.Migration) def toSvc: Svc.Migration = Svc.Migration(m.version, m.name, m.description)
