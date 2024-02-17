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
  private val directionDesc = "up or down; direction of the migration"
  private val folderDesc = "The directory to use to find the files.  Does not do it recursively"
  private val versionDesc = "Specifies the version to go up to or down to"
  private val dryRunDesc =
    "Will not actually run the schema updates but will check to make sure they syntax check.  This requires a db connection"
  private val fileRegexDesc =
    "Regex that MUST contain a group name called version and desc.  It also MUST match the direction"
  private val dbDriverDesc = "Database driver to use.  Right now only supports org.postgresql.Driver"
  val direction: ScallopOption[Migrator.Direction] =
    trailArg[Migrator.Direction](required = true, descr = directionDesc)(directionConverter)
  val sourceFolder: ScallopOption[String] = opt[String](required = true, short = 'f', descr = folderDesc)
  val destinationVersion: ScallopOption[String] = opt[String](required = false, short = 'v', descr = versionDesc)
  val dbTableName: ScallopOption[String] = opt[String](
    required = false,
    default = "doomig_version".some,
    short = 't',
    descr = "Table name of the migration state"
  )
  val dbUrl: ScallopOption[String] = opt[String](required = true, short = 'l', descr = "Database jdbc url")
  val dbUser: ScallopOption[String] = opt[String](required = true, short = 'u', descr = "user")
  val dbPassword: ScallopOption[String] = opt[String](required = true, short = 'p', descr = "password")
  val dbDriver: ScallopOption[String] =
    opt[String](required = false, default = "org.postgresql.Driver".some, short = 's', descr = dbDriverDesc)
  val dryRun: ScallopOption[Boolean] =
    opt[Boolean](required = false, default = true.some, short = 'd', descr = dryRunDesc)
  val fileRegex: ScallopOption[Regex] =
    opt[Regex](required = false, default = getDefaultRegEx(), short = 'r', descr = fileRegexDesc)
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
