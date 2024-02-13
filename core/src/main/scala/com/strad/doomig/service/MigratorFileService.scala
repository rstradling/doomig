package com.strad.doomig.service

import cats.*
import cats.effect.Async
import cats.implicits.*
import com.strad.doomig.db.VersionStampRepo
import com.strad.doomig.domain.Svc
import doobie.implicits.*
import doobie.util.fragment.Fragment
import doobie.util.transactor.Transactor

object MigratorFileService:

  def run[F[_]](
    db: Transactor[F],
    versionRepo: VersionStampRepo[F],
    tableName: String,
    dir: String,
    l: List[Svc.Migration]
  )(using
    a: Async[F]
  ): F[List[Int]] =
    l.traverse { x =>
      // TODO: FIX RESOURCE LEAK
      val sql = scala.io.Source.fromFile(dir + "/" + x.name).mkString
      val fr = Fragment.const(sql)
      for
        t <- fr.update.run.transact(db)
        res <- versionRepo.writeVersion(tableName, x)
      yield res
    }
