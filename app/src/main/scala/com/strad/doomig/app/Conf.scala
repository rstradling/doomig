package com.strad.doomig.app

import cats.*
import cats.implicits.*
import com.strad.doomig.config.MigratorConfig
import com.strad.doomig.db.DbConfig
import com.strad.doomig.service.Migrator.Direction
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
  val sourceFolder: ScallopOption[String] = opt[String](required = true)
  val destinationVersion: ScallopOption[String] = opt[String](required = false)
  val dbTableName: ScallopOption[String] = opt[String](required = true, default = "doomig_version".some)
  val dbUrl: ScallopOption[String] = opt[String](required = true)
  val dbUser: ScallopOption[String] = opt[String](required = true)
  val dbPassword: ScallopOption[String] = opt[String](required = true)
  val dbDriver: ScallopOption[String] = opt[String](required = true, default = "org.postgresql.Driver".some)
  val dryRun: ScallopOption[Boolean] = opt[Boolean](required = false, default = true.some)
  val fileRegex: ScallopOption[Regex] = opt[Regex](required = false, default = getDefaultRegEx())
  verify()

  def getDefaultRegEx(): Option[Regex] =
    direction.toOption match
      case Some(Direction.Up) =>
        FileDiscoveryService.UpRegEx.some
      case Some(Direction.Down) =>
        FileDiscoveryService.DownRegEx.some
      case None => None
  def toMigratorConfig(): MigratorConfig =
    val tableName = this.dbTableName.getOrElse("Table Name was not specified")
    val direction = this.direction.getOrElse(throw new RuntimeException("Unable to find a direction"))
    val dryRun = this.dryRun.toOption.get
    val regex = fileRegex.toOption.get
    MigratorConfig(
      tableName,
      direction,
      dryRun,
      regex,
      this.sourceFolder.toOption.get,
      this.destinationVersion.toOption
    )
  def toDbConfig(): DbConfig =
    DbConfig(
      this.dbDriver.toOption.get,
      this.dbUrl.toOption.get,
      this.dbUser.toOption.get,
      this.dbPassword.toOption.get
    )
end Conf
