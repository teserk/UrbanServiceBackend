package routing.api

import routing.model._
import routing.model.JsonProtocol._
import routing.repo._
import routing.service._
import zio._
import zio.http._
import zio.http.model.{Header, Method, Status}
import zio.json._

import java.util.UUID

object HttpRoutes {

  val app: HttpApp[HouseRepository with StreetRepository with RoutingService, Response] =
    addHouse ++ addStreet ++ findRoute

  def addHouse: HttpApp[HouseRepository, Response] = Http.collectZIO[Request] {
    case req@Method.POST -> !! / "addHouse" =>
      for {
        house <- decodeHouseRequest(req)
        id <- Random.nextUUID
        _ <- HouseRepository.add(House(id, house.longitude, house.latitude))
          .catchAll(err => ZIO.logError(err.getMessage) *> ZIO.fail(Response.status(Status.InternalServerError)))
      } yield Response.text(id.toString).setStatus(Status.Ok)
  }

  def addStreet: HttpApp[StreetRepository, Response] = Http.collectZIO[Request] {
    case req@Method.POST -> !! / "addStreet" =>
      val first = req.url.queryParams.get("from").get.map(UUID.fromString).head
      val second = req.url.queryParams.get("to").get.map(UUID.fromString).head
      val length = req.url.queryParams.get("length").get.map(_.toDouble).head
      for {
        id <- Random.nextUUID
        _ <- StreetRepository.add(Street(id, first, second, length))
          .catchAll(err => ZIO.logError(err.getMessage) *> ZIO.fail(Response.status(Status.InternalServerError)))
      } yield Response.text(id.toString).setStatus(Status.Ok)
  }

  def findRoute: HttpApp[RoutingService with StreetRepository with HouseRepository, Response] = Http.collectZIO[Request] {
    case req@Method.GET -> !! / "findRoute" =>
      val first = req.url.queryParams.get("from").get.map(UUID.fromString).head
      val second = req.url.queryParams.get("to").get.map(UUID.fromString).head
      for {
        firstHouse <- HouseRepository.findById(first)
          .map(house => house.get)
          .catchAll(err => ZIO.fail(Response.status(Status.BadRequest)))
        secondHouse <- HouseRepository.findById(second)
          .map(house => house.get)
          .catchAll(err => ZIO.fail(Response.status(Status.BadRequest)))
        response <- RoutingService.findRoute(firstHouse, secondHouse)
          .either
          .map {
            case Left(err) => err match {
              case RouteNotExists => Response.json(RouteResponse(routeExists = false, List.empty).toJson)
              case RouteInternalError => Response.status(Status.InternalServerError)
            }
            case Right(houses) => Response.json(RouteResponse(routeExists = true, houses.map(street => street.id)).toJson)
          }
      } yield response
  }

  private def decodeHouseRequest(req: Request): IO[Response, HouseRequest] =
    req.body.asString.flatMap { bodyStr =>
      bodyStr.fromJson[HouseRequest] match {
        case Left(err) => ZIO.logError(err) *> ZIO.fail(Response.status(Status.BadRequest))
        case Right(credentials) => ZIO.succeed(credentials)
      }
    }.catchAll(err => ZIO.logError(err.toString) *> ZIO.fail(Response.status(Status.BadRequest)))

  case class RouteResponse(routeExists: Boolean, streets: List[UUID])
  implicit val routeResponseEncoder: JsonEncoder[RouteResponse] = DeriveJsonEncoder.gen[RouteResponse]
  implicit val routeResponseDecoder: JsonDecoder[RouteResponse] = DeriveJsonDecoder.gen[RouteResponse]
}
