package auth.api

import auth.api.HttpRoutesSpec.test
import auth.model._
import auth.repo._
import zio._
import zio.http._
import zio.http.model.{Header, Headers, Status}
import zio.json.{DecoderOps, DeriveJsonDecoder, JsonDecoder}
import zio.mock.Expectation
import zio.mock.Expectation._
import zio.stream.ZStream
import zio.test.Assertion._
import zio.test.{TestArrow, _}

import java.security.MessageDigest
import java.util.UUID

object HttpRoutesSpec extends ZIOSpecDefault {

  private def hashPassword(password: String): String = {
    val messageDigest = MessageDigest.getInstance("SHA-512")
    val hashBytes = messageDigest.digest(password.getBytes("UTF-8"))
    hashBytes.map("%02x".format(_)).mkString
  }
  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("RoutesSpec")(
      test("Should register new user") {
        val customer = Customer(UUID.fromString("0000-00-00-00-000000"), "foo", hashPassword("123"))
        val mock =
          CustomerRepositoryMock.Add(equalTo(customer), Expectation.value(())) and
            CustomerRepositoryMock.FindByUsername(equalTo("foo"), Expectation.value(None))
        (for {
          _ <- TestRandom.feedUUIDs(UUID.fromString("0000-00-00-00-000000"))
          url <- ZIO.fromEither(URL.fromString("http://localhost:8082/auth/register"))
          req = Request.post(Body.fromString("""{"login":"foo", "password":"123"}"""), url)
          response <- HttpRoutes.app.runZIO(req)
        } yield assertTrue(
          response.status == Status.Ok
        )).provide(mock)
      },

      test("Should not register existing user") {
        val customer = Customer(UUID.fromString("0000-00-00-00-000000"), "foo", hashPassword("123"))

        val mock =
            CustomerRepositoryMock.FindByUsername(equalTo("foo"), Expectation.value(Some(customer)))

        (for {
          _ <- TestRandom.feedUUIDs(UUID.fromString("0000-00-00-00-000000"))
          url <- ZIO.fromEither(URL.fromString("http://localhost:8082/auth/register"))
          req = Request.post(Body.fromString("""{"login":"foo", "password":"123"}"""), url)
          response <- HttpRoutes.app.runZIO(req).either
        } yield assertTrue(
          response.fold(
            left => left.get.status != Status.Ok,
            right => right.status != Status.Ok
          )
        )).provide(mock)
      },

      test("Should login user with right password") {
        val customer = Customer(UUID.fromString("0000-00-00-00-000000"), "foo", hashPassword("123"))

        val mock =
          CustomerRepositoryMock.FindByUsername(equalTo("foo"), Expectation.value(Some(customer)))

        (for {
          url <- ZIO.fromEither(URL.fromString("http://localhost:8082/auth/login"))
          req = Request.put(Body.fromString("""{"login":"foo", "password":"123"}"""), url)
          response <- HttpRoutes.app.runZIO(req)
        } yield assertTrue(
          response.status == Status.Ok
        )).provide(mock)
      },

      test("Should not login user with wrong password") {
        val customer = Customer(UUID.fromString("0000-00-00-00-000000"), "foo", hashPassword("123"))

        val mock =
          CustomerRepositoryMock.FindByUsername(equalTo("foo"), Expectation.value(Some(customer)))

        (for {
          url <- ZIO.fromEither(URL.fromString("http://localhost:8082/auth/login"))
          req = Request.put(Body.fromString("""{"login":"foo", "password":"totallynot123"}"""), url)
          response <- HttpRoutes.app.runZIO(req)
        } yield assertTrue(
          response.status == Status.Unauthorized
        )).provide(mock)
      },

      test("Should get customers with jwt token") {
        val customer = Customer(UUID.fromString("0000-00-00-00-000000"), "foo", hashPassword("123"))
        val customers = List(Customer(UUID.fromString("0000-00-00-00-000000"), "foo", hashPassword("123")),
          Customer(UUID.fromString("0000-00-00-00-000000"), "bar", hashPassword("123")),
          Customer(UUID.fromString("0000-00-00-00-000000"), "dip", hashPassword("123")))

        val mock =
          CustomerRepositoryMock.FindByUsername(equalTo("foo"), Expectation.value(Some(customer))) and
            CustomerRepositoryMock.FindAll(Expectation.value(ZStream.fromIterable(customers)))
        case class authToken(token: String)
        implicit val tokenDecoder: JsonDecoder[authToken] = DeriveJsonDecoder.gen[authToken]

        (for {
          urlPut <- ZIO.fromEither(URL.fromString("http://localhost:8082/auth/login"))
          urlGet <- ZIO.fromEither(URL.fromString("http://localhost:8082/customers"))

          loginRequest = Request.put(Body.fromString("""{"login":"foo", "password":"123"}"""), urlPut)
          responseToken <- HttpRoutes.app.runZIO(loginRequest)
          tokenJson <- responseToken.body.asString
          token <- ZIO.fromEither(tokenJson.fromJson[authToken])

          getCustomersRequest =
            Request.get(urlGet).copy(headers = Headers("Authorization", "Bearer " ++ token.token))
          response <- HttpRoutes.app.runZIO(getCustomersRequest)
          customers <- response.body.asString

        } yield assertTrue(
          response.status == Status.Ok &&
            customers.contains("foo") &&
            customers.contains("bar") &&
            customers.contains("dip")
        )).provide(mock)
      }
    )
}
