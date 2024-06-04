package auth.model

import zio.json._
import zio.schema.{DeriveSchema, Schema}

object JsonProtocol {
  implicit val customerDecoder: JsonDecoder[Customer] = DeriveJsonDecoder.gen[Customer]
  implicit val customerEncoder: JsonEncoder[Customer] = DeriveJsonEncoder.gen[Customer]

  implicit val customerSchema: Schema[Customer] = DeriveSchema.gen[Customer]

  implicit val credentialsDecoder: JsonDecoder[Credentials] = DeriveJsonDecoder.gen[Credentials]
  implicit val credentialsEncoder: JsonEncoder[Credentials] = DeriveJsonEncoder.gen[Credentials]

  implicit val credentialsSchema: Schema[Credentials] = DeriveSchema.gen[Credentials]
}
