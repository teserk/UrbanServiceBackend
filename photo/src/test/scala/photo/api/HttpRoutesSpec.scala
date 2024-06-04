package photo.api

import zio.http.{Body, Boundary, Form, FormField, Header, Headers, MediaType, Method, Request, Status, URL, Version}
import zio.stream._
import zio.test.TestResult.allSuccesses
import zio.test._
import zio._
import zio.nio._
import zio.nio.charset._
import zio.nio.file.{Files, Path}
import photo.service.PhotoServiceMock

object HttpRoutesSpec extends ZIOSpecDefault {

  def readImage(str: String): Task[ZStream[Any, Nothing, Byte]] =
    Files.readAllBytes(Path(s"photo/src/test/resources/${str}.jpeg")).map(ZStream.fromChunk[Byte](_))

  def post(path: String, identifier: String, boundary: Boundary, data: ZStream[Any, Nothing, Byte]): Task[Request] =
    Charset.Standard.utf8.encodeString(identifier)
      .map(identifierChunks => Request(
        url = URL(zio.http.Path.root / path),
        method = Method.POST,
        body = Body.fromStream(
          Form(
            FormField.binaryField(name = "nodeId", data = identifierChunks, mediaType = MediaType.application.`octet-stream`),
            FormField.StreamingBinary(
              name = "photo"
              , data = data
              , filename = Some(identifier)
              , contentType = MediaType.application.`octet-stream`
            ),
          )
            .multipartBytes(boundary),
          0L
        ).contentType(newMediaType = MediaType.multipart.`form-data`, newBoundary = boundary),
        headers = Headers(Header.ContentType(MediaType.multipart.`form-data`)),
        version = Version.Http_1_1
      )
      )

  def testFileUpload(filename: String): Task[TestResult] =
    (for {
      boundary <- Boundary.randomUUID
      content <- readImage(filename)
      request <- post("uploadPhoto", filename, boundary, content)
      response <- HttpRoutes.app.runZIO(request)
      body <- response.body.asString
    } yield allSuccesses(
      assertTrue(response.status == Status.Ok)
    )).provide(ZLayer.succeed(PhotoServiceMock.empty))

  def get(id: String): Task[Request] = ZIO.succeed {
    Request.get(
      url = URL(zio.http.Path.root / "photo").addQueryParam("nodeId", id)
    )
  }

  def testFileSend(id: String, photo: ZStream[Any, Nothing, Byte]): Task[TestResult] =
    (for {
      request <- get(id)
      response <- HttpRoutes.app.runZIO(request)
      body <- response.body.asStream
        .runCollect
      photo <- photo.runCollect
    } yield allSuccesses(
      assertTrue(response.status == Status.Ok),
      assertTrue(body == photo)
    )).provide(ZLayer.succeed(PhotoServiceMock(photo)))

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("HttpRoutes")(
      test("POST small image") {
        testFileUpload("small")
      },
      test("POST cat image") {
        testFileUpload("cat")
      },
      test("GET small image") {
        for {
          photo <- readImage("small")
          res <- testFileSend("small", photo)
        } yield res
      },
      test("GET cat image") {
        for {
          photo <- readImage("cat")
          res <- testFileSend("cat", photo)
        } yield res
      }
    )
}
