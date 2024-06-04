package routing.model


import zio.json._
import zio.schema.{DeriveSchema, Schema}

object JsonProtocol {
  implicit val houseDecoder: JsonDecoder[House] = DeriveJsonDecoder.gen[House]
  implicit val houseEncoder: JsonEncoder[House] = DeriveJsonEncoder.gen[House]
  implicit val houseSchema: Schema[House] = DeriveSchema.gen[House]

  implicit val streetDecoder: JsonDecoder[Street] = DeriveJsonDecoder.gen[Street]
  implicit val streetEncoder: JsonEncoder[Street] = DeriveJsonEncoder.gen[Street]
  implicit val streetSchema: Schema[Street] = DeriveSchema.gen[Street]

  implicit val houseRequestDecoder: JsonDecoder[HouseRequest] = DeriveJsonDecoder.gen[HouseRequest]
  implicit val houseRequestEncoder: JsonEncoder[HouseRequest] = DeriveJsonEncoder.gen[HouseRequest]
  implicit val houseRequestSchema: Schema[HouseRequest] = DeriveSchema.gen[HouseRequest]

  implicit val streetRequestDecoder: JsonDecoder[StreetRequest] = DeriveJsonDecoder.gen[StreetRequest]
  implicit val streetRequestEncoder: JsonEncoder[StreetRequest] = DeriveJsonEncoder.gen[StreetRequest]
  implicit val streetRequestSchema: Schema[StreetRequest] = DeriveSchema.gen[StreetRequest]
}
