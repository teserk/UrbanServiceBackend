package auth.api

import auth.model._
import auth.model.JsonProtocol._
import auth.repo.CustomerRepository

import java.time.Clock
import java.security.MessageDigest
import zio._
import zio.http._
import zio.http.model.{Method, Status}
import zio.json._
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import zio.http.HttpAppMiddleware.bearerAuth

object HttpRoutes {

  private def decodeCredentials(req: Request): ZIO[Any, Throwable, Credentials] = {
    req.body.asString.flatMap { bodyStr =>
      bodyStr.fromJson[Credentials] match {
        case Left(err) => ZIO.fail(new RuntimeException(err))
        case Right(credentials) => ZIO.succeed(credentials)
      }
    }
  }

  val app: HttpApp[CustomerRepository, Response] = register ++ login ++ customers

  def register: HttpApp[CustomerRepository, Response] = Http.collectZIO[Request] {
    case req@Method.POST -> !! / "auth" / "register" =>
      for {
        credentials <- decodeCredentials(req)
          .catchAll(err => ZIO.fail(Response.status(Status.InternalServerError)))
        response <- CustomerRepository.findByUsername(credentials.login).either.flatMap {
          case Right(foundOption) => foundOption match {
            case Some(_) => ZIO.fail(Response.text("Пользователь с таким логином существует")
              .setStatus(Status.BadRequest))
            case None =>
              registerUser(credentials).as(Response.text("Успешная регистрация").setStatus(Status.Ok))
                .catchAll(err => ZIO.fail(Response.status(Status.InternalServerError)))
          }
          case Left(_) => ZIO.fail(Response.status(Status.InternalServerError))
        }
      } yield response
  }

  def login: HttpApp[CustomerRepository, Response] = Http.collectZIO[Request] {
    case req@Method.PUT -> !! / "auth" / "login" =>
      (for {
        credentials <- decodeCredentials(req)
        response <- CustomerRepository.findByUsername(credentials.login).either.map {
          case Right(foundOption) => foundOption match {
            case Some(customer) =>
              if (hashPassword(credentials.password) == customer.password) {
                val token = jwtEncode(credentials)
                Response.json(s"""{"token":"$token"}""")
              } else Response.status(Status.Unauthorized)
            case None => Response.status(Status.Unauthorized)
          }
          case Left(_) => Response.status(Status.Unauthorized)
        }
      } yield response)
        .catchAll(err => ZIO.logError(err.getMessage)
          *> ZIO.fail(Response.status(Status.InternalServerError)))
  }

  def customers: HttpApp[CustomerRepository, Response] = Http.collectZIO[Request] {
    case req@Method.GET -> !! / "customers" =>
      for {
        customers <- CustomerRepository
          .findAll()
          .runCollect
          .map(chunk => chunk.toArray)
          .tapBoth(e => ZIO.logError(e.getMessage), _ => ZIO.logInfo("got info"))
          .either
          .map {
            case Right(customers) => Response.json(customers.toJson)
            case Left(e) => Response.status(Status.InternalServerError)
          }
      } yield customers
  } @@ bearerAuth(decodeJwt(_).isDefined)

  private def hashPassword(password: String): String = {
    val messageDigest = MessageDigest.getInstance("SHA-512")
    val hashBytes = messageDigest.digest(password.getBytes("UTF-8"))
    hashBytes.map("%02x".format(_)).mkString
  }

  private def registerUser(credentials: Credentials): ZIO[CustomerRepository, Throwable, Unit] =
    for {
      id <- Random.nextUUID
      hash = hashPassword(credentials.password)
      _ <- CustomerRepository.add(Customer(id, credentials.login, hash))
    } yield ()

  private def jwtEncode(credentials: Credentials): String = {
    implicit val clock: Clock = Clock.systemUTC
    val json = s"""{"login": "${credentials.login}", "password": "${credentials.password}"}"""
    val claim = JwtClaim {
      json
    }.issuedNow.expiresIn(300)
    Jwt.encode(claim, sys.env.getOrElse("SECRET_KEY", "defaultSecretKey"), JwtAlgorithm.HS512)
  }

  private def decodeJwt(token: String): Option[JwtClaim] =
    Jwt.decode(token, sys.env.getOrElse("SECRET_KEY", "defaultSecretKey"), Seq(JwtAlgorithm.HS512)).toOption
}
