package routing.repo

import routing.model._
import zio._
import zio.schema.DeriveSchema
import zio.stream.ZStream
import zio.sql.postgresql.PostgresJdbcModule

import java.util.UUID

trait HouseRepository {

  def findById(id: UUID): Task[Option[House]]

  def add(house: House): Task[Unit]

}

object HouseRepository {
  def findById(id: UUID): RIO[HouseRepository, Option[House]] =
    ZIO.serviceWithZIO[HouseRepository](_.findById(id))

  def add(house: House): RIO[HouseRepository, Unit] =
    ZIO.serviceWithZIO[HouseRepository](_.add(house))
}

trait HousePostgresTableDescription extends PostgresJdbcModule {

  implicit val houseSchema = DeriveSchema.gen[House]

  val houses = defineTable[House]("houses")

  val (houseId, houseLongitude, houseLatitude) = houses.columns
}