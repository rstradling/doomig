package com.strad

import cats.*
import cats.implicits.*
import cats.syntax.*
import com.strad.doomig.service.{FileDiscoveryService, Migrator}
import org.rogach.scallop.*

import scala.util.matching.Regex

class Conf(arguments: List[String]) extends ScallopConf(arguments):
  implicit val directionConverter: ValueConverter[Migrator.Direction] = singleArgConverter[Migrator.Direction] { s =>
    s.toLowerCase() match
      case "up"   => Migrator.Direction.Up
      case "down" => Migrator.Direction.Down
      case _      => throw new RuntimeException("Need to specify up or down direction of the conversion")
  }
  implicit val regexConverter: ValueConverter[Regex] = singleArgConverter[Regex] { s =>
    new Regex(s)
  }
  val direction: ScallopOption[Migrator.Direction] = trailArg[Migrator.Direction](required = true)(directionConverter)
  val folder: ScallopOption[String] = opt[String](required = true)
  val upRegex: ScallopOption[Regex] =
    opt[Regex](required = false, default = FileDiscoveryService.defaultUpRegEx.some)(regexConverter)
  val downRegex: ScallopOption[Regex] =
    opt[Regex](required = false, default = FileDiscoveryService.defaultDownRegEx.some)(regexConverter)
  val destinationId: ScallopOption[String] = opt[String](required = false)
  verify()
