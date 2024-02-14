package com.strad.doomig.service

import cats.*
import cats.effect.Async
import cats.effect.kernel.Resource
import cats.implicits.*
import com.strad.doomig.db.VersionStampRepo
import com.strad.doomig.domain.Svc
import com.strad.doomig.logging.DoobieLogger
import com.strad.doomig.service.Migrator.Direction
import com.strad.doomig.service.Migrator.Direction.{Down, Up}
import doobie.*
import doobie.implicits.*
import doobie.util.fragment.Fragment
import doobie.util.transactor.Transactor
import scala.io.BufferedSource

object MigratorFileService:

  def run[F[_]](
    db: Transactor[F],
    versionRepo: VersionStampRepo[F],
    tableName: String,
    directory: String,
    l: List[Svc.Migration],
    direction: Direction,
    dryRun: Boolean
  )(using
    a: Async[F]
  ): Resource[F, List[Int]] =
    val y = db.yolo
    import y.*
    val readAll = (fileName: String) =>
      for file <- Resource.make[F, BufferedSource](Async[F].blocking(scala.io.Source.fromFile(fileName)))(s =>
          Async[F].blocking(s.close())
        )
      yield file.mkString
    l.traverse { x =>
      for
        sql <- readAll(directory + "/" + x.name)
        fr = Fragment.const(sql)
        update = fr.update
        res <-
          if dryRun then Resource.eval(update.check.map(x => 1))
          else
            for
              t <- Resource.eval(update.run.transact(db))
              res <- direction match
                case Up   => Resource.eval(versionRepo.writeVersion(tableName, x))
                case Down => Resource.eval(versionRepo.deleteVersion(tableName, x.version))
            yield res
      yield res
    }
