package com.strad.doomig

import cats.*
import cats.implicits.*
import cats.effect.Async
import doobie.Update0
import doobie.util.transactor.Transactor
import doobie.implicits.*

object MigrationService:
  enum Direction:
    case Up, Down

  def run[F[_]](transactor: Transactor[F], l: List[Update0])(using a: Async[F]): F[List[Int]] =
    l.traverse(_.run.transact(transactor))

  def mkUpMigrationList[A](items : List[UpMigration[A]])(using a: Order[A]): List[Update0] =
      items.sortWith(_.migrationId < _.migrationId).foldLeft(List.empty[Update0])((acc, x) => acc ++ x.up)

  def mkDownMigrationList[A](items: List[DownMigration[A]])(using a: Order[A]): List[Update0] =
    items.sortWith(_.migrationId > _.migrationId).foldLeft(List.empty[Update0])((acc, x) => acc ++ x.down)
