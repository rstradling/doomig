package com.strad.doomig.service

import cats.*
import cats.effect.Async
import cats.effect.kernel.Resource
import cats.implicits.*
import com.strad.doomig.db.VersionStampRepo
import com.strad.doomig.domain.Svc
import doobie.implicits.*
import doobie.util.fragment.Fragment
import doobie.util.transactor.Transactor

import scala.io.BufferedSource

object MigratorFileService:

  def run[F[_]](
    db: Transactor[F],
    versionRepo: VersionStampRepo[F],
    tableName: String,
    dir: String,
    l: List[Svc.Migration]
  )(using
    a: Async[F]
  ): Resource[F, List[Int]] =
    val readAll = (fileName: String) =>
      for file <- Resource.make[F, BufferedSource](Async[F].blocking(scala.io.Source.fromFile(fileName)))(s =>
          Async[F].blocking(s.close())
        )
      yield file.mkString
    l.traverse { x =>
      for
        sql <- readAll(dir + "/" + x.name)
        fr = Fragment.const(sql)
        t <- Resource.eval(fr.update.run.transact(db))
        res <- Resource.eval(versionRepo.writeVersion(tableName, x))
      yield res
    }
