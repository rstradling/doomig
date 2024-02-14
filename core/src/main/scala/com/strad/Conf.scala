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
  val destinationVersion: ScallopOption[String] = opt[String](required = false)
  val dbTableName: ScallopOption[String] = opt[String](required = true, default = "doomig_version".some)
  val dbUrl: ScallopOption[String] = opt[String](required = true)
  val dbUser: ScallopOption[String] = opt[String](required = true)
  val dbPassword: ScallopOption[String] = opt[String](required = true)
  val dbDriver: ScallopOption[String] = opt[String](required = true, default = "org.postgresql.Driver".some)
  val dryRun: ScallopOption[Boolean] = opt[Boolean](required = false, default = true.some)
  verify()
