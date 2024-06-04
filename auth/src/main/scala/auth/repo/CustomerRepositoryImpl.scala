package auth.repo

import auth.model.Customer
import zio.sql._
import zio.stream.ZStream
import zio.{ZIO, ZLayer}

import java.util.UUID

final class CustomerRepositoryImpl(
                                    pool: ConnectionPool
                                  ) extends CustomerRepository
  with PostgresTableDescription {

  val driverLayer: ZLayer[Any, Nothing, SqlDriver] =
    ZLayer
      .make[SqlDriver](
        ZLayer.succeed(pool),
        SqlDriver.live
      )

  override def findAll(): ZStream[Any, Throwable, Customer] = {
    val query = select(customerId, customerUsername, customerPassword).from(customers)

    execute(query)
      .provideSomeLayer(driverLayer)
      .map { case (id, username, password) => Customer(id, username, password) }
  }

  override def add(customer: Customer): ZIO[Any, Throwable, Unit] = {
    val query =
      insertInto(customers)(customerId, customerUsername, customerPassword)
        .values(
          (
            customer.id,
            customer.username,
            customer.password
          )
        )

    ZIO.logInfo(s"Query to insert customer is ${renderInsert(query)}") *>
      execute(query)
        .provideSomeLayer(driverLayer)
        .unit
  }

  override def updateCustomer(customer: Customer): ZIO[Any, Throwable, Unit] = {
    val query =
      update(customers)
        .set(customerUsername, customer.username)
        .set(customerPassword, customer.password)
        .where(customerId === customer.id)

    ZIO.logInfo(s"Query to update customer is ${renderUpdate(query)}") *>
      execute(query)
        .provideSomeLayer(driverLayer)
        .unit
  }

  override def delete(id: UUID): ZIO[Any, Throwable, Unit] = {
    val query =
      deleteFrom(customers)
        .where(customerId === id)

    ZIO.logInfo(s"Query to delete customer is ${renderDelete(query)}") *>
      execute(query)
        .provideSomeLayer(driverLayer)
        .unit
  }

  override def findByUsername(requestedUsername: String): ZIO[Any, Throwable, Option[Customer]] = {
    val query = select(customerId, customerUsername, customerPassword)
      .from(customers)
      .where(customerUsername === requestedUsername)

    ZIO.logInfo(s"Query to find by username is ${renderRead(query)}") *>
      execute(query.to((Customer.apply _).tupled))
        .provideSomeLayer(driverLayer)
        .runHead
  }
}

object CustomerRepositoryImpl {
  val live: ZLayer[ConnectionPool, Throwable, CustomerRepository] =
    ZLayer.fromFunction(new CustomerRepositoryImpl(_))
}
