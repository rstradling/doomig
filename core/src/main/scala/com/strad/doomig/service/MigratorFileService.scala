package com.strad.doomig.service

import cats.*
import cats.effect.Async
import cats.implicits.*
import com.strad.doomig.domain.{Migration, MigrationAction}
import doobie.Update0
import doobie.implicits.*
import doobie.util.transactor.Transactor

class MigratorFileService extends Migrator[String]:

  def down(migration: Migration[String]): MigrationAction[String] = ???

  def up(migration: Migration[String]): MigrationAction[String] = ???
  def run[F[_]](transactor: Transactor[F], l: List[Update0])(using a: Async[F]): F[List[Int]] =
    l.traverse(_.run.transact(transactor))

// def mkUpMigrationList[A](items : List[Migration[A]])(using a: Order[A]): List[Update0] =
//     items.sortWith(_.migrationId < _.migrationId).foldLeft(List.empty[Update0])((acc, x) => acc ++ x.up)

// def mkDownMigrationList[A](items: List[Migration[A]])(using a: Order[A]): List[Update0] =
//   items.sortWith(_.migrationId > _.migrationId).foldLeft(List.empty[Update0])((acc, x) => acc ++ x.down)
