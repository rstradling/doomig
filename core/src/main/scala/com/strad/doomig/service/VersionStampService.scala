package com.strad.doomig.service

import com.strad.doomig.domain.Migration

trait VersionStampService[F[_], A]:
  def doesTableExist(tableName: String): F[Boolean]
  def fetchCurrentVersion(tableName: String): F[Option[Migration[A]]]
  def writeVersion(tableName: String, migration: Migration[A]): F[Int]
  def createTable(tableName: String): F[Int]
  def dropTable(tableName: String): F[Int]
