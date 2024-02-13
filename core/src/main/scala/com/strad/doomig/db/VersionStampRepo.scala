package com.strad.doomig.db

import com.strad.doomig.domain.{Dao, Svc}

trait VersionStampRepo[F[_]]:
  def doesTableExist(tableName: String): F[Boolean]
  def fetchCurrentVersion(tableName: String): F[Option[Dao.Migration]]
  def writeVersion(tableName: String, migration: Svc.Migration): F[Int]
  def createTableIfNotExist(tableName: String): F[Int]
  def dropTableIfExists(tableName: String): F[Int]
