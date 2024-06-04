package photo

import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault}
import zio.http
import photo.api._
import photo.config.ServiceConfig
import photo.service.PhotoServiceImpl
import zio.http.Server

object PhotoMain extends ZIOAppDefault {

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    for {
      _ <- ZIO.logInfo("Starting PhotoMain")
      _ <- zio.http.Server.serve(HttpRoutes.app)
        .provide(
          Server.live,
          ServiceConfig.live,
          PhotoServiceImpl.live
        )
        .catchAll { error =>
          ZIO.logError("Failed to start photo service") *> ZIO.die(error)
        }
    } yield ()
}
