package photo.service

import zio._
import zio.stream.ZStream

sealed trait PhotoServiceException extends Throwable
case object SizeLimit extends PhotoServiceException
case object InternalError extends PhotoServiceException
case object PhotoNotFound extends PhotoServiceException

trait PhotoService {

  def UploadPhoto(id: String, data: ZStream[Any, Nothing, Byte]): IO[PhotoServiceException, Unit]

  def FindPhoto(id: String): ZStream[Any, PhotoServiceException, Byte]

}

object PhotoService {

  def UploadPhoto(id: String, data: ZStream[Any, Nothing, Byte]): ZIO[PhotoService, PhotoServiceException, Unit] =
    ZIO.serviceWithZIO[PhotoService](_.UploadPhoto(id, data))

  def FindPhoto(id: String): ZStream[PhotoService, PhotoServiceException, Byte] =
    ZStream.serviceWithStream[PhotoService](_.FindPhoto(id))

}
