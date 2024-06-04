package routing

import routing.config._
import routing.api._
import routing.service._
import routing.repo.{HouseRepositoryImpl, StreetRepositoryImpl}
import zio.http.Server
import zio.sql.ConnectionPool
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault}

object RoutingMain extends ZIOAppDefault {
  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    for {
      _ <- ZIO.logInfo("Start RoutingMain")
      _ <- zio.http.Server.serve(HttpRoutes.app)
        .provide(
          Config.live,
          Server.live,
          Config.serverLive,
          Config.connectionPoolConfigLive,
          ConnectionPool.live,
          HouseRepositoryImpl.live,
          StreetRepositoryImpl.live,
          RoutingServiceImpl.live
        )
        .catchAll { error =>
          ZIO.logError("Failed to start routing service") *> ZIO.die(error)
        }
    } yield ()
  }
}