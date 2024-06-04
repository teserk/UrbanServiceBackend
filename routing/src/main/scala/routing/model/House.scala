package routing.model

import java.util.UUID

final case class House(id: UUID, longitude: Double, latitude: Double)

final case class HouseRequest(longitude: Double, latitude: Double)