package routing.api

import routing.service._
import routing.repo._
import routing.model._
import routing.model.JsonProtocol._
import zio._
import zio.test._
import zio.http._
import zio.http.model.Status
import zio.json._

object HttpRoutesSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("HttpRoutes")(
      test("should add House") {
        val mockHouse = HouseRepositoryMock.simple
        val mockStreet = StreetRepositoryMock.simple
        val mockRouting = RoutingServiceMock(List.empty)

        val houseRequest = HouseRequest(10, 10)
        (for {
          url <- ZIO.fromEither(URL.fromString("http://localhost:8081/addHouse"))
          request = Request.post(Body.fromString(houseRequest.toJson), url)
          response <- HttpRoutes.app.runZIO(request)
        } yield assertTrue(
          response.status == Status.Ok
        )).provide(
          ZLayer.succeed(mockHouse),
          ZLayer.succeed(mockStreet),
          ZLayer.succeed(mockRouting)
        )
      },
      test("should add Street") {
        val mockHouse = HouseRepositoryMock.simple
        val mockStreet = StreetRepositoryMock.simple
        val mockRouting = RoutingServiceMock(List.empty)

        val streetRequest = StreetRequest(HousesAndStreets.firstHouseID, HousesAndStreets.secondHouseID, 10)
        (for {
          url <-
            ZIO.fromEither(URL.fromString(
              s"http://localhost:8081/addStreet?from=${streetRequest.firstHouseId}&to=${streetRequest.secondHouseId}&length=${streetRequest.length}"))
          request = Request.post(Body.empty, url)
          response <- HttpRoutes.app.runZIO(request)
        } yield assertTrue(
          response.status == Status.Ok
        )).provide(
          ZLayer.succeed(mockHouse),
          ZLayer.succeed(mockStreet),
          ZLayer.succeed(mockRouting)
        )
      },
      test("should find route") {
        val mockHouse = HouseRepositoryMock.simple
        val mockStreet = StreetRepositoryMock.simple
        val mockRouting = RoutingServiceMock(List.empty)

        val streetRequest = StreetRequest(HousesAndStreets.firstHouseID, HousesAndStreets.secondHouseID, 10)
        (for {
          url <-
            ZIO.fromEither(URL.fromString(
              s"http://localhost:8081/findRoute?from=${streetRequest.firstHouseId}&to=${streetRequest.secondHouseId}"))
          request = Request.get(url)
          response <- HttpRoutes.app.runZIO(request)
        } yield assertTrue(
          response.status == Status.Ok
        )).provide(
          ZLayer.succeed(mockHouse),
          ZLayer.succeed(mockStreet),
          ZLayer.succeed(mockRouting)
        )
      }
    )
}
