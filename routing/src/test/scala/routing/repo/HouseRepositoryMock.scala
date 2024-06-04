package routing.repo

import routing.model.House
import zio.{Task, ZIO}

import java.util.UUID


case class HouseRepositoryMock(
                                houses: List[House]
                              ) extends HouseRepository {
  override def findById(id: UUID): Task[Option[House]] =
    ZIO.succeed(houses.find(house => house.id == id))

  override def add(house: House): Task[Unit] = ZIO.unit
}

object HouseRepositoryMock {

  import HousesAndStreets._

  val simple: HouseRepositoryMock = {
    HouseRepositoryMock(List(firstHouse, secondHouse, thirdHouse))
  }

  val triangleWithUnreachable: HouseRepositoryMock = {
    HouseRepositoryMock(List(firstHouse, secondHouse, thirdHouse, fourthHouse))
  }
}