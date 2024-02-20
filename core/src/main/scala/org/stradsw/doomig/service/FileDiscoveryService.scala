package org.stradsw.doomig.service

import cats.*
import cats.effect.Async
import cats.implicits.*
import org.stradsw.doomig.domain.Svc
import org.stradsw.doomig.service.Migrator.Direction
import org.stradsw.doomig.service.Migrator.Direction.{Down, Up}
import org.stradsw.doomig.treewalker.FileTreeWalker

import scala.util.matching.Regex

object FileDiscoveryService:
  val UpRegEx: Regex = """(?<version>\d{4}_\d{2}_\d{2}_\d{2}_\d{2}_\d{2})_up-(?<desc>.*)\.sql""".r
  val DownRegEx: Regex = """(?<version>\d{4}_\d{2}_\d{2}_\d{2}_\d{2}_\d{2})_down-(?<desc>.*)\.sql""".r

  def createMigrationFilesList[F[_]: Async: MonadThrow](
    path: String,
    regex: Regex,
    lastRanMigration: Option[Svc.Migration],
    destinationVersion: Option[String],
    direction: Direction
  ): F[List[Svc.Migration]] =
    val treeWalker = new FileTreeWalker[F]()
    val window = lastRunFilter(lastRanMigration, destinationVersion, direction)
    val v: F[List[Svc.Migration]] = treeWalker
      .list(path)
      .map(x => x.filter(y => regexFilter(regex)(y)))
      .map { x =>
        x.map { y =>
          val fileName = (new java.io.File(y)).getName
          regex.findFirstMatchIn(fileName) match
            case Some(a) =>
              val version = a.group("version")
              val desc = a.group("desc")
              Svc.Migration(version, fileName, desc)
            case None =>
              throw new RuntimeException("We have filtered all the values already so this should always match")
        }
      }
      .map(x => x.filter(y => window(y)))
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
  private def regexFilter(regex: Regex)(p: String) =
    val fileName = p
    regex.findFirstMatchIn(fileName) match
      case Some(_) => true
      case None    => false
end FileDiscoveryService
