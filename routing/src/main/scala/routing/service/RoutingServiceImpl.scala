package routing.service

import routing.model.{House, Street}
import routing.repo._
import zio._

import java.util.UUID

class RoutingServiceImpl(
                          streetRepo: StreetRepository,
                          houseRepo: HouseRepository
                        ) extends RoutingService {


  override def findRoute(begin: House, end: House): ZIO[StreetRepository with HouseRepository, RoutingServiceException, List[Street]] = ZIO.blocking {
    for {
      openSet <- Ref.make(Set(begin))
      closedSet <- Ref.make(Set.empty[UUID])
      cameFrom <- Ref.make(Map.empty[House, (Street, House)])
      gScore <- Ref.make(Map(begin -> 0.0))
      fScore <- Ref.make(Map(begin -> heuristic(begin, end)))
      route <- aStar(begin, end, openSet, closedSet, cameFrom, gScore, fScore)
    } yield route
  }

  private def heuristic(current: House, end: House): Double =
    math.sqrt(math.pow(current.longitude - end.longitude, 2) + math.pow(current.latitude - end.latitude, 2))

  private def reconstructPath(cameFrom: Map[House, (Street, House)], current: House): UIO[List[Street]] =
    ZIO.loop(current)(cur => cameFrom.contains(cur),
      cur => cameFrom(cur)._2)(cur => ZIO.succeed(cameFrom(cur)._1))
      .map(list => list.reverse)


  private def aStar(
                     current: House,
                     end: House,
                     openSetRef: Ref[Set[House]],
                     closedSetRef: Ref[Set[UUID]],
                     cameFromRef: Ref[Map[House, (Street, House)]],
                     gScoreRef: Ref[Map[House, Double]],
                     fScoreRef: Ref[Map[House, Double]]
                   ): ZIO[StreetRepository with HouseRepository, RoutingServiceException, List[Street]] = {
    for {
      openSet <- openSetRef.get
      closedSet <- closedSetRef.get
      gScore <- gScoreRef.get
      neighbours <- StreetRepository.findNeighbours(current).catchAll(_ => ZIO.fail(RouteInternalError))
      _ <- closedSetRef.update(_ + current.id)
      _ <- openSetRef.update(_ - current)
      _ <- ZIO.foreachDiscard(neighbours) { neighbor => {
        val tentativeGScore = gScore(current) + neighbor.length
        for {
          neighbourHouseOpt <- HouseRepository.findById(neighbor.secondHouseId).catchAll(_ => ZIO.fail(RouteInternalError))
          neighbourHouse <- ZIO.fromOption(neighbourHouseOpt).catchAll(_ => ZIO.fail(RouteInternalError))
          _ <-
            (cameFromRef.update(_.updated(neighbourHouse, (neighbor, current))) *>
            gScoreRef.update(_.updated(neighbourHouse, tentativeGScore)) *>
            fScoreRef.update(_.updated(neighbourHouse, tentativeGScore + heuristic(neighbourHouse, end))) *>
            openSetRef.update(_ + neighbourHouse)).when(!closedSet.contains(neighbourHouse.id) || tentativeGScore < gScore(current))
        } yield ()
      }.unless(closedSet.contains(neighbor.secondHouseId))
      }
      openSet <- openSetRef.get
      _ <- ZIO.fail(RouteNotExists).when(openSet.isEmpty)
      next <- if (current == end) ZIO.succeed(current) else selectNextNode(openSetRef, fScoreRef)
      result <- if (next == end) {
        for {
          cameFromUpd <- cameFromRef.get
          res <- reconstructPath(cameFromUpd, next)
        } yield res
      }
      else aStar(next, end, openSetRef, closedSetRef, cameFromRef, gScoreRef, fScoreRef)
    } yield result
  }

  private def selectNextNode(openSet: Ref[Set[House]], fScore: Ref[Map[House, Double]]): IO[RoutingServiceException, House] =
    for {
      fScores <- fScore.get
      open <- openSet.get
    } yield open.minBy(fScores)
}

object RoutingServiceImpl {
  val live: ZLayer[StreetRepository with HouseRepository, Throwable, RoutingService] =
    ZLayer.fromFunction((streetRepo: StreetRepository, houseRepo: HouseRepository) =>
      new RoutingServiceImpl(streetRepo, houseRepo))
}
