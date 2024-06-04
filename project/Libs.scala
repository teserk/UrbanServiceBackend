import sbt._

object V {
  val zio = "2.0.21"
  val zio_http = "0.0.5"
  val zio_http_newer_version = "3.0.0-RC7"
  val zio_sql = "0.1.1"
  val zio_json = "0.6.2"
  val zio_mock = "1.0.0-RC12"
  val pureconfig = "0.17.3"
  val jwt_zio_json = "10.0.0"
  val zio_nio = "2.0.2"
}


object Libs {

  val zio: List[ModuleID] = List(
    "dev.zio" %% "zio" % V.zio,
    "dev.zio" %% "zio-http" % V.zio_http,
    "dev.zio" %% "zio-sql-postgres" % V.zio_sql,
    "dev.zio" %% "zio-json" % V.zio_json,
    "dev.zio" %% "zio-test" % V.zio % Test,
    "dev.zio" %% "zio-test-sbt" % V.zio % Test,
    "dev.zio" %% "zio-mock" % V.zio_mock % Test,
    "dev.zio" %% "zio-nio" % V.zio_nio,
    "com.github.jwt-scala" %% "jwt-zio-json" % V.jwt_zio_json
  )

  val zioUpdated: List[ModuleID] = List(
    "dev.zio" %% "zio" % V.zio,
    "dev.zio" %% "zio-http" % V.zio_http_newer_version,
    "dev.zio" %% "zio-sql-postgres" % V.zio_sql,
    "dev.zio" %% "zio-json" % V.zio_json,
    "dev.zio" %% "zio-test" % V.zio % Test,
    "dev.zio" %% "zio-test-sbt" % V.zio % Test,
    "dev.zio" %% "zio-mock" % V.zio_mock % Test,
    "dev.zio" %% "zio-nio" % V.zio_nio,
  )

  val pureconfig: List[ModuleID] = List(
    "com.github.pureconfig" %% "pureconfig" % V.pureconfig
  )
}
