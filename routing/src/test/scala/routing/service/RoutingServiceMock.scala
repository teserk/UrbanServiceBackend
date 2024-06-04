package routing.service

import routing.model.{House, Street}
import routing.repo.{HouseRepository, StreetRepository}
import zio._

final case class RoutingServiceMock(route: List[Street]) extends RoutingService {

  override def findRoute(begin: House, end: House): ZIO[StreetRepository with HouseRepository, RoutingServiceException, List[Street]] =
    ZIO.succeed(route)

}
