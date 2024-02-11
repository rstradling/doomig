package com.strad.doomig.service

import cats.effect.Async
import fs2.io.file.{Files, Path}

import scala.util.matching.compat.Regex

object FileDiscoveryService:
  val defaultUpRegEx: Regex = """(?<version>\d{4}_\d{2}_\d{2}_\d{2}_\d{2}_\d{2})_up-(?<desc>.*)\.sql""".r
  val defaultDownRegEx: Regex = """(?<version>\d{4}_\d{2}_\d{2}_\d{2}_\d{2}_\d{2})_down-(?<desc>.*)\.sql""".r

  def createMigrationFiles[F[_]: Files: Async](path: Path, regex: Regex): fs2.Stream[F, Path] =
    val filter = regexFilter(regex)
    Files[F].walk(path).filter(filter)

  private def regexFilter(regex: Regex)(p: Path) =
    regex.findFirstMatchIn(p.fileName.toString) match
      case Some(_) => true
      case None    => false
