package com.strad

import com.strad.doomig.MigrationService.Direction
import com.strad.doomig.MigrationService.Direction.Down
import com.strad.doomig.MigrationService.Direction.Up
import org.rogach.scallop.*

class Conf(arguments: List[String]) extends ScallopConf(arguments):
  implicit val directionConverter: ValueConverter[Direction] = singleArgConverter[Direction] { s =>
    s.toLowerCase() match {
      case "up" => Direction.Up
      case "down" => Direction.Down
      case _ => throw new RuntimeException("Need to specify up or down direction of the conversion")
    }
  }
  val direction: ScallopOption[Direction] = trailArg[Direction](required = true)(directionConverter)
  val folder: ScallopOption[String] = opt[String](required = true)
  val upRegex: ScallopOption[String] = opt[String](required = false)
  val downRegex: ScallopOption[String] = opt[String](required = false)
  verify()
