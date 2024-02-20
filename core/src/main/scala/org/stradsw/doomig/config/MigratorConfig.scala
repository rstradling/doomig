package org.stradsw.doomig.config

import org.stradsw.doomig.service.Migrator.Direction

import scala.util.matching.Regex

case class MigratorConfig(
  tableName: String,
  direction: Direction,
  dryRun: Boolean,
  fileRegex: Regex,
  sourceFolder: String,
  destinationVersion: Option[String]
)
