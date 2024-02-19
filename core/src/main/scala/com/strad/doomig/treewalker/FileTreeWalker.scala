package com.strad.doomig.treewalker

import cats.effect.*
import scala.jdk.CollectionConverters.*

class FileTreeWalker[F[_]](using f: Sync[F]) extends TreeWalker[F]:
  def list(dirName: String): F[List[String]] =
    val path = new java.io.File(dirName)
    f.delay(
      path.listFiles().toSeq.asInstanceOf[scala.collection.immutable.ArraySeq[java.io.File]].toList.map(x => x.toString)
    )
