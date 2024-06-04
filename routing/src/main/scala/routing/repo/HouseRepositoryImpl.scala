package routing.repo

import routing.model.House
import zio.{Task, ZIO, ZLayer}
import zio.sql._

import java.util.UUID

final class HouseRepositoryImpl(
                                 pool: ConnectionPool
                               ) extends HouseRepository
  with HousePostgresTableDescription {

  val driverLayer: ZLayer[Any, Nothing, SqlDriver] =
    ZLayer
      .make[SqlDriver](
        ZLayer.succeed(pool),
        SqlDriver.live
      )

  override def findById(id: UUID): Task[Option[House]] = {
    val query = select(houseId, houseLongitude, houseLatitude)
      .from(houses)
      .where(houseId === id)

    ZIO.logInfo(s"Query to find house by id is ${renderRead(query)}") *>
      execute(query.to((House.apply _).tupled))
        .provideSomeLayer(driverLayer)
        .runHead

  }

  override def add(house: House): Task[Unit] = {
    val query =
      insertInto(houses)(houseId, houseLongitude, houseLatitude)
        .values(
          house.id,
          house.longitude,
          house.latitude
        )

    ZIO.logInfo(s"Query to insert house is ${renderInsert(query)}") *>
      execute(query)
        .provideSomeLayer(driverLayer)
        .unit
  }
}

object HouseRepositoryImpl {
  val live: ZLayer[ConnectionPool, Throwable, HouseRepository] =
    ZLayer.fromFunction(new HouseRepositoryImpl(_))
}
