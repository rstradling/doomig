package org.stradsw.doomig.logging

import cats.effect.*
import cats.implicits.*
import doobie.*
import doobie.util.log.{ExecFailure, LogEvent, ProcessingFailure, Success}
import org.typelevel.log4cats.SelfAwareStructuredLogger

class DoobieLogger[F[_]: Sync](logger: SelfAwareStructuredLogger[F]) extends doobie.util.log.LogHandler[F]:
  override def run(e: doobie.util.log.LogEvent): F[Unit] =
    for ret <- e match
        case Success(s, a, l, e1, e2) =>
          logger.info(s"""Successful Statement Execution:
               |  ${s}
               | arguments = [${a.mkString(", ")}]
               |   elapsed = ${e1.toMillis.toString} ms exec + ${e2.toMillis.toString} ms processing (${(e1 + e2).toMillis.toString} ms total)
              """.stripMargin)

        case ProcessingFailure(s, a, l, e1, e2, t) =>
          logger.error(t)(s"""Failed Resultset Processing:
               |  ${s}
               | arguments = [${a.mkString(", ")}]
               |   elapsed = ${e1.toMillis.toString} ms exec + ${e2.toMillis.toString} ms processing (${(e1 + e2).toMillis.toString} ms total)
               |   failure = ${t.getMessage}
              """.stripMargin)

        case ExecFailure(s, a, l, e1, t) =>
          logger.error(t)(s"""Failed Statement Execution:
               |  ${s}
               | arguments = [${a.mkString(", ")}]
               |   elapsed = ${e1.toMillis.toString} ms exec
               |   failure = ${t.getMessage}
              """.stripMargin)
    yield ret
