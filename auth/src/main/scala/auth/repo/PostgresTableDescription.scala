package auth.repo

import auth.model.Customer
import zio.schema.{DeriveSchema, Schema}
import zio.sql._
import zio.sql.macros.TableSchema
import zio.sql.postgresql.PostgresJdbcModule

trait PostgresTableDescription extends PostgresJdbcModule {

  implicit val customerSchema = DeriveSchema.gen[Customer]

  val customers = defineTable[Customer]("customers")

  val (customerId, customerUsername, customerPassword) = customers.columns
}
