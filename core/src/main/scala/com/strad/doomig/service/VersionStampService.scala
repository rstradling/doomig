package com.strad.doomig.service

import com.strad.doomig.domain.Migration

trait VersionStampService:
  def doesTableExist[F[_]](tableName: String): F[Boolean]
  def fetchCurrentVersion[F[_], A](tableName: String): F[Option[Migration[A]]]
  def writeVersion[F[_], A](tableName: String, migration: Migration[A]): F[Unit]
  def createTable[F[_]](tableName: String): F[Unit]
