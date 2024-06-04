package routing.repo

import routing.model.{House, Street}
import zio._
import zio.schema.{DeriveSchema, Schema}
import zio.sql.postgresql.PostgresJdbcModule

import java.util.UUID

trait StreetRepository {

  def findById(id: UUID): Task[Option[Street]]
  
  def findNeighbours(house: House): Task[List[Street]]

  def add(street: Street): Task[Unit]

}

object StreetRepository {

  def findById(id: UUID): RIO[StreetRepository, Option[Street]] =
    ZIO.serviceWithZIO[StreetRepository](_.findById(id))
  
  def findNeighbours(house: House): RIO[StreetRepository, List[Street]] =
    ZIO.serviceWithZIO[StreetRepository](_.findNeighbours(house))
  
  def add(street: Street): RIO[StreetRepository, Unit] =
    ZIO.serviceWithZIO[StreetRepository](_.add(street))

}

trait StreetPostgresTableDescription extends PostgresJdbcModule {

  implicit val streetSchema = DeriveSchema.gen[Street]

  val streets = defineTable[Street]("streets")

  val (streetId, firstHouseId, secondHouseId, length) = streets.columns

}