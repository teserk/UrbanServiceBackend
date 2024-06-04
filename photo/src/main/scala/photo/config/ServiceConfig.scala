package photo.config
import pureconfig.ConfigSource
import pureconfig.generic.auto.exportReader
import zio.http.Server.Config
import zio.{ZLayer, http}

import java.net.InetSocketAddress

case class ServiceConfig(host: String, port: Int)

object ServiceConfig {
  private val source = ConfigSource.default.at("app").at("service-config")
  private val serviceConfig: ServiceConfig = source.loadOrThrow[ServiceConfig]

  val live: ZLayer[Any, Nothing, Config] = ZLayer.succeed {
    Config
      .default
      .binding(serviceConfig.host, serviceConfig.port)
  }
}
