package auth.repo

import auth.model.Customer
import zio.ZIO
import zio.stream.ZStream

import java.util.UUID

trait CustomerRepository {

  def findAll(): ZStream[Any, Throwable, Customer]

  def findByUsername(requestedLogin: String): ZIO[Any, Throwable, Option[Customer]]

  def add(customer: Customer): ZIO[Any, Throwable, Unit]

  def updateCustomer(customer: Customer): ZIO[Any, Throwable, Unit]

  def delete(id: UUID): ZIO[Any, Throwable, Unit]
}

object CustomerRepository {
  def findAll(): ZStream[CustomerRepository, Throwable, Customer] =
    ZStream.serviceWithStream[CustomerRepository](_.findAll())

  def add(customer: Customer): ZIO[CustomerRepository, Throwable, Unit] =
    ZIO.serviceWithZIO[CustomerRepository](_.add(customer))

  def update(customer: Customer): ZIO[CustomerRepository, Throwable, Unit] =
    ZIO.serviceWithZIO[CustomerRepository](_.updateCustomer(customer))

  def delete(id: UUID): ZIO[CustomerRepository, Throwable, Unit] =
    ZIO.serviceWithZIO[CustomerRepository](_.delete(id))

  def findByUsername(requestedLogin: String): ZIO[CustomerRepository, Throwable, Option[Customer]] =
    ZIO.serviceWithZIO[CustomerRepository](_.findByUsername(requestedLogin))
}
