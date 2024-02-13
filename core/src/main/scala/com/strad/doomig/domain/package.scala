package com.strad.doomig.domain

import doobie.Update0

import java.time.Instant

object Svc:
  case class Migration(version: String, name: String, description: String)

object Dao:
  case class Migration(version: String, name: String, description: String, modifiedDate: Instant)

object DomainHelpers:
  extension (m: Svc.Migration) def toDao: Dao.Migration = Dao.Migration(m.version, m.name, m.description, Instant.now)
  extension (m: Dao.Migration) def toSvc: Svc.Migration = Svc.Migration(m.version, m.name, m.description)

