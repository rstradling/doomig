package com.strad.doomig.treewalker

import cats.effect.*
import java.nio.file.{Files, Paths}
import scala.jdk.CollectionConverters.*
class ResourceTreeWalker[F[_]](using f: Sync[F]) extends TreeWalker[F]:
  def list(dirName: String): F[List[String]] =
    f.delay {
      val classLoader = getClass.getClassLoader
      val url = classLoader.getResource(dirName)
      if url != null then
        val path = Paths.get(url.toURI)
        val fileList = Files.list(path).iterator().asScala.toList
        fileList.map(_.toString)
      else List.empty[String]
    }
