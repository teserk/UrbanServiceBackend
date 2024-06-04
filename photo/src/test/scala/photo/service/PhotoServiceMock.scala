package photo.service
import zio.{Task, ZIO}
import zio.stream.ZStream

class PhotoServiceMock(photo: ZStream[Any, Nothing, Byte]) extends PhotoService {

  override def UploadPhoto(id: String, data: ZStream[Any, Nothing, Byte]): Task[Unit] = ZIO.unit

  override def FindPhoto(id: String): ZStream[Any, Throwable, Byte] = photo

}

object PhotoServiceMock {
  def apply(photo: ZStream[Any, Nothing, Byte]): PhotoServiceMock = new PhotoServiceMock(photo)

  val empty = new PhotoServiceMock(ZStream.range(1, 5).map(_.toByte))
}