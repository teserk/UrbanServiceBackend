package photo.service

import zio._
import zio.stream._

import java.io.{File, FileNotFoundException}
import java.nio.file.Paths

class PhotoServiceImpl extends PhotoService {

  private val maxSize = 10 * 1024 * 1024

  override def UploadPhoto(id: String, data: ZStream[Any, Nothing, Byte]): IO[PhotoServiceException, Unit] =
    data
      .mapAccumZIO(0) {
        case (length, _) if length > maxSize => ZIO.fail(SizeLimit)
        case (length, byte) => ZIO.succeed((length + 1, byte))
      }
      .run(ZSink.fromFile(new File("/app/images", id ++ ".jpeg")).mapError(_ => InternalError)).unit

  override def FindPhoto(id: String): ZStream[Any, PhotoServiceException, Byte] =
    ZStream.fromPath(Paths.get("/app/images/" ++ id ++ ".jpeg")).mapError {
      case FileNotFoundException => PhotoNotFound
      case _ => InternalError
    }
}

object PhotoServiceImpl {

  val live: ZLayer[Any, Throwable, PhotoService] =
    ZLayer.fromFunction((_: Any) => new PhotoServiceImpl)
}