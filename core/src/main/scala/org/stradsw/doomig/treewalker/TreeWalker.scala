package org.stradsw.doomig.treewalker

trait TreeWalker[F[_]]:
  def list(dirName: String): F[List[String]]