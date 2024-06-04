package routing.repo

import routing.model.{House, Street}
import zio.{Task, ZIO, ZLayer}
import zio.sql._

import java.util.UUID

final class StreetRepositoryImpl(
                                  pool: ConnectionPool
                                ) extends StreetRepository
  with StreetPostgresTableDescription {

  val driverLayer: ZLayer[Any, Nothing, SqlDriver] =
    ZLayer
      .make[SqlDriver](
        ZLayer.succeed(pool),
        SqlDriver.live
      )

  override def findById(id: UUID): Task[Option[Street]] = {
    val query = select(streetId, firstHouseId, secondHouseId, length)
      .from(streets)
      .where(streetId === id)

    ZIO.logInfo(s"Query to find street by id is ${renderRead(query)}") *>
      execute(query.to((Street.apply _).tupled))
        .provideSomeLayer(driverLayer)
        .runHead

  }

  override def findNeighbours(house: House): Task[List[Street]] = {
    val query = select(streetId, firstHouseId, secondHouseId, length)
      .from(streets)
      .where(
        firstHouseId === house.id
      )

    ZIO.logInfo(s"Query to find neighbours is ${renderRead(query)}") *>
      execute(query.to((Street.apply _).tupled))
        .provideSomeLayer(driverLayer)
        .runCollect
        .map(chunk => chunk.toList)
  }

  override def add(street: Street): Task[Unit] = {
    val query =
      insertInto(streets)(streetId, firstHouseId, secondHouseId, length)
        .values(
          street.id,
          street.firstHouseId,
          street.secondHouseId,
          street.length
        )

    ZIO.logInfo(s"Query to insert street is ${renderInsert(query)}") *>
      execute(query)
        .provideSomeLayer(driverLayer)
        .unit
  }
}

object StreetRepositoryImpl {
  val live: ZLayer[ConnectionPool, Throwable, StreetRepository] =
    ZLayer.fromFunction(new StreetRepositoryImpl(_))
}