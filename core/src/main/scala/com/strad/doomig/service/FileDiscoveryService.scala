package com.strad.doomig.service

import cats.*
import cats.effect.Async
import cats.implicits.*
import com.strad.doomig.domain.Svc
import com.strad.doomig.service.Migrator.Direction
import com.strad.doomig.service.Migrator.Direction.{Down, Up}
import fs2.io.file.{Files, Path}

import scala.util.matching.Regex

object FileDiscoveryService:
  val UpRegEx: Regex = """(?<version>\d{4}_\d{2}_\d{2}_\d{2}_\d{2}_\d{2})_up-(?<desc>.*)\.sql""".r
  val DownRegEx: Regex = """(?<version>\d{4}_\d{2}_\d{2}_\d{2}_\d{2}_\d{2})_down-(?<desc>.*)\.sql""".r

  def createMigrationFilesList[F[_]: Files: Async: MonadThrow](
    path: Path,
    regex: Regex,
    lastRanMigration: Option[Svc.Migration],
    destinationVersion: Option[String],
    direction: Direction
  ): F[List[Svc.Migration]] =
    val filter = regexFilter(regex)
    val window = lastRunFilter(lastRanMigration, destinationVersion, direction)
    val v = Files[F]
      .walk(path)
      .filter(filter)
      .map { x =>
        val fileName = x.names.last.toString
        regex.findFirstMatchIn(fileName) match
          case Some(a) =>
            val version = a.group("version")
            val desc = a.group("desc")
            Svc.Migration(version, fileName, desc)
          case None =>
            throw new RuntimeException("We have filtered all the values already so this should always match")
      }
      .filter(window)
      .compile
      .toVector
    v.map(items =>
      direction match
        case Down => items.sortWith(_.version > _.version).toList
        case Up   => items.sortWith(_.version < _.version).toList
    )

  private def lastRunFilter(
    lastRanMigration: Option[Svc.Migration],
    destinationVersion: Option[String],
    direction: Direction
  )(p: Svc.Migration) =
    (lastRanMigration, destinationVersion) match
      case (None, None) =>
        direction match
          case Down => false
          case Up   => true
      case (Some(dbVersion), Some(expectedVersion)) =>
        direction match
          case Down =>
            if p.version <= dbVersion.version && p.version > expectedVersion then true
            else false
          case Up =>
            if p.version > dbVersion.version && p.version <= expectedVersion then true
            else false
      case (Some(dbVersion), None) =>
        direction match
          case Down =>
            if p.version <= dbVersion.version then true
            else false
          case Up =>
            if p.version > dbVersion.version then true
            else false
      case (None, Some(expectedVersion)) =>
        direction match
          case Down =>
            if p.version > expectedVersion then true
            else false
          case Up =>
            if p.version <= expectedVersion then true
            else false
  private def regexFilter(regex: Regex)(p: Path) =
    val fileName = p.names.last.toString
    regex.findFirstMatchIn(fileName) match
      case Some(_) => true
      case None    => false
end FileDiscoveryService
