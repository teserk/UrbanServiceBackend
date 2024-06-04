package routing.service

import routing.model._
import routing.repo._
import zio._
import zio.test._

object RoutingServiceSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("Routing")(
      test("Should find route in simple city") {
        import HousesAndStreets._

        val houseRepo = HouseRepositoryMock.simple
        val streetRepo = StreetRepositoryMock.simple

        (for {
          route <- RoutingService.findRoute(firstHouse, thirdHouse)
        } yield assertTrue(
          route == List(
            aStreet,
            bStreet
          )
        )).provide(
          ZLayer.succeed(houseRepo),
          ZLayer.succeed(streetRepo),
          RoutingServiceImpl.live
        )
      },
      test("Should find route in triangle long city") {
        import HousesAndStreets._

        val houseRepo = HouseRepositoryMock.simple
        val streetRepo = StreetRepositoryMock.alternativeLong

        (for {
          route <- RoutingService.findRoute(firstHouse, thirdHouse)
        } yield assertTrue(
          route == List(
            aStreet,
            bStreet
          )
        )).provide(
          ZLayer.succeed(houseRepo),
          ZLayer.succeed(streetRepo),
          RoutingServiceImpl.live
        )
      },
      test("Should find route in triangle short city") {
        import HousesAndStreets._

        val houseRepo = HouseRepositoryMock.simple
        val streetRepo = StreetRepositoryMock.alternativeShort

        (for {
          route <- RoutingService.findRoute(firstHouse, thirdHouse)
        } yield assertTrue(
          route == List(
            cStreetShort
          )
        )).provide(
          ZLayer.succeed(houseRepo),
          ZLayer.succeed(streetRepo),
          RoutingServiceImpl.live
        )
      },
      test("Should fail if there is no path") {
        import HousesAndStreets._

        val houseRepo = HouseRepositoryMock.simple
        val streetRepo = StreetRepositoryMock.noPath

        (for {
          exit <- RoutingService.findRoute(firstHouse, thirdHouse).exit
        } yield assertTrue(
          exit.isFailure
        ))
          .provide(
            ZLayer.succeed(houseRepo),
            ZLayer.succeed(streetRepo),
            RoutingServiceImpl.live
          )
      },
      test("Should return empty list when begin == end") {
        import HousesAndStreets._

        val houseRepo = HouseRepositoryMock.simple
        val streetRepo = StreetRepositoryMock.simple

        (for {
          route <- RoutingService.findRoute(firstHouse, firstHouse)
        } yield assertTrue(
          route == List.empty
        )).provide(
          ZLayer.succeed(houseRepo),
          ZLayer.succeed(streetRepo),
          RoutingServiceImpl.live
        )
      },
      test("Should fail if there is no path and the begin in triangle") {
        import HousesAndStreets._

        val houseRepo = HouseRepositoryMock.triangleWithUnreachable
        val streetRepo = StreetRepositoryMock.triangle

        (for {
          exit <- RoutingService.findRoute(firstHouse, fourthHouse).exit
        } yield assertTrue(
          exit == Exit.fail(RouteNotExists)
        ))
          .provide(
            ZLayer.succeed(houseRepo),
            ZLayer.succeed(streetRepo),
            RoutingServiceImpl.live
          )
      },
    )

}
