package photo.api

import zio._
import zio.http._
import zio.json._
import zio.stream._
import photo.service._

import java.util.UUID

object HttpRoutes {
  private case class Identifier(value: String)

  private case class PhotoData(data: ZStream[Any, Nothing, Byte])

  private case class FormFieldsData(id: Option[Identifier], data: Option[PhotoData])

  val app: Routes[PhotoService, Response] = Routes(
    Method.POST / "uploadPhoto" ->
      handler { (req: Request) =>
        for {
          formStream <- req.body.asMultipartFormStream
            .mapError(ex =>
              Response(Status.InternalServerError, body = Body.fromString("failed to decode body as multipart"))
            )
          formData <- formStream
            .fields
            .collectZIO {
              case FormField.StreamingBinary("nodeId", _, _, _, data) =>
                data.run(ZSink.collectAll[Byte]).map(_.mkString).map(string => Identifier(string))
              case FormField.StreamingBinary("photo", _, _, _, data) =>
                ZIO.succeed(PhotoData(data))
            }
            .runFoldZIO(FormFieldsData(None, None))((result, field) => field match {
              case Identifier(value) => if (result.id.isEmpty)
                ZIO.succeed(FormFieldsData(Some(Identifier(value)), result.data))
              else ZIO.fail(new Exception("id specified more than once"))
              case PhotoData(data) => if (result.data.isEmpty)
                ZIO.succeed(FormFieldsData(result.id, Some(PhotoData(data))))
              else ZIO.fail(new Exception("photo specified more than once"))
            })
            .catchAll(err => ZIO.logError(err.getMessage) *> ZIO.fail(Response(Status.BadRequest)))
          response <- formData match {
            case FormFieldsData(Some(Identifier(id)), Some(PhotoData(data))) =>
              PhotoService.UploadPhoto(id, data).mapBoth(
                {
                  case SizeLimit => Response(Status.BadRequest, body = Body.fromString("Photo size is bigger than 10mb"))
                  case InternalError => Response(Status.InternalServerError)
                },
                _ => Response(Status.Ok)
              )
            case _ =>
              ZIO.fail(Response(Status.BadRequest, body = Body.fromString("missing nodeId or photo")))
          }
        } yield response
      },
    Method.GET / "healthcheck" -> handler { (_: Request) => Response.status(Status.Ok) },
    Method.GET / "photo" -> handler { (req: Request) =>
      val id = req.queryParam("nodeId").get
      for {
        env <- ZIO.environment[PhotoService]
        stream = PhotoService.FindPhoto(id).provideEnvironment(env)
        response <- ZIO.succeed(
          Response(
            Status.Ok,
            headers = Headers(Header.ContentType(MediaType.image.jpeg).untyped),
            body = Body.fromStreamChunked(stream)
          )
        )
      } yield response
    }.catchAll {
      case PhotoNotFound => Handler.fail(Response(Status.BadRequest, body = Body.fromString("Photo not found")))
      case InternalError => Handler.fail(Response(Status.InternalServerError))
    }
  )
}
