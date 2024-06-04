package routing.service

import routing.model._
import routing.repo.{HouseRepository, StreetRepository}
import zio._

import java.util.UUID

sealed trait RoutingServiceException extends Throwable
case object RouteNotExists extends RoutingServiceException
case object RouteInternalError extends RoutingServiceException

trait RoutingService {

  def findRoute(begin: House, end: House): ZIO[StreetRepository with HouseRepository, RoutingServiceException, List[Street]]

}

object RoutingService {

  def findRoute(begin: House, end: House)
  : ZIO[RoutingService with StreetRepository with HouseRepository, RoutingServiceException, List[Street]] =
    ZIO.serviceWithZIO[RoutingService](_.findRoute(begin, end))

}