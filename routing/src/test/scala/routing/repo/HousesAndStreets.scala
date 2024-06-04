package routing.repo

import routing.model.{House, Street}

import java.util.UUID

object HousesAndStreets {
  val firstHouseID: UUID = UUID.fromString("0000-00-00-00-000000")
  val secondHouseID: UUID = UUID.fromString("0000-00-00-00-000001")
  val thirdHouseID: UUID = UUID.fromString("0000-00-00-00-000002")
  val fourthHouseID: UUID = UUID.fromString("0000-00-00-00-000002")
  val firstHouse: House = House(firstHouseID, 0, 1)
  val secondHouse: House = House(secondHouseID, 1, 1)
  val thirdHouse: House = House(thirdHouseID, 1, 0)
  val fourthHouse: House = House(fourthHouseID, 10, 10)

  val aStreet: Street = Street(UUID.fromString("0000-00-00-00-000010"), firstHouseID, secondHouseID, 1)
  val bStreet: Street = Street(UUID.fromString("0000-00-00-00-000020"), secondHouseID, thirdHouseID, 1)
  val cStreetLong: Street = Street(UUID.fromString("0000-00-00-00-000030"), firstHouseID, thirdHouseID, 3)
  val cStreetShort: Street = Street(UUID.fromString("0000-00-00-00-000040"), firstHouseID, thirdHouseID, 1.5)
  val cStreetShortBackwards: Street = Street(UUID.fromString("0000-00-00-00-000040"), thirdHouseID, firstHouseID, 1.5)
}
