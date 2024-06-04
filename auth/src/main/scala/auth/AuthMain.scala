package auth

import api.HttpRoutes
import config.Config
import repo.CustomerRepositoryImpl
import zio._
import zio.http.Server
import zio.sql.ConnectionPool
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault, http}

object AuthMain extends ZIOAppDefault {
  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    for {
      _ <- ZIO.logInfo("Start AuthMain")
      _ <- zio.http.Server.serve(HttpRoutes.app)
        .provide(
          Config.live,
          Server.live,
          Config.serverLive,
          CustomerRepositoryImpl.live,
          ConnectionPool.live,
          Config.connectionPoolConfigLive
        )
        .catchAll { error =>
          ZIO.logErrorCause("Failed to start auth service", Cause.fail(error)) *> ZIO.die(error)
        }
    } yield ()
  }
}