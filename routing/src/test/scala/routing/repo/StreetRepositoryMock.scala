package routing.repo

import routing.model.{House, Street}
import zio.{Task, ZIO}

import java.util.UUID

case class StreetRepositoryMock(
                                 streets: Map[House, List[Street]]
                               ) extends StreetRepository {

  override def findById(id: UUID): Task[Option[Street]] =
    ZIO.succeed(streets.values.toList.flatten.find(street => street.id == id))

  override def findNeighbours(house: House): Task[List[Street]] =
    ZIO.succeed(streets.getOrElse(house, List.empty))

  override def add(street: Street): Task[Unit] = ZIO.unit
}

object StreetRepositoryMock {

  import HousesAndStreets._

  val simple: StreetRepositoryMock = {
    val streets: Map[House, List[Street]] = Map(
      firstHouse -> List(aStreet),
      secondHouse -> List(bStreet)
    )
    StreetRepositoryMock(streets)
  }

  val alternativeLong: StreetRepositoryMock = {
    val streets: Map[House, List[Street]] = Map(
      firstHouse -> List(aStreet, cStreetLong),
      secondHouse -> List(bStreet)
    )
    StreetRepositoryMock(streets)
  }

  val alternativeShort: StreetRepositoryMock = {
    val streets: Map[House, List[Street]] = Map(
      firstHouse -> List(aStreet, cStreetShort),
      secondHouse -> List(bStreet)
    )
    StreetRepositoryMock(streets)
  }

  val noPath: StreetRepositoryMock =
    StreetRepositoryMock(Map.empty)

  val triangle: StreetRepositoryMock = {
    val streets: Map[House, List[Street]] = Map(
      firstHouse -> List(aStreet),
      secondHouse -> List(bStreet),
      thirdHouse -> List(cStreetShortBackwards)
    )
    StreetRepositoryMock(streets)
  }

}
