package routing.model

import java.util.UUID

final case class Street(id: UUID, firstHouseId: UUID, secondHouseId: UUID, length: Double)

final case class StreetRequest(firstHouseId: UUID, secondHouseId: UUID, length: Double)