import Dependencies.{Auth, Helper, Photo, Routing}

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

ThisBuild / testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")

ThisBuild / scalacOptions += "-Ymacro-annotations"

ThisBuild / assemblyMergeStrategy := {
  case PathList("META-INF", xs@_*) =>
    xs map { _.toLowerCase } match {
      case "services" :: xs =>
        MergeStrategy.filterDistinctLines
      case _ => MergeStrategy.discard
    }
  case x =>
    val oldStrategy = (ThisBuild / assemblyMergeStrategy).value
    oldStrategy(x)
}

lazy val root = (project in file("."))
  .settings(
    name := "project-mipt"
  )
  .aggregate(
    auth,
    routing,
    helper,
    photo
  )
  .dependsOn(
    auth,
    routing,
    helper,
    photo
  )

lazy val auth = (project in file("auth"))
  .settings(
    name := "project-auth",
    libraryDependencies ++= Auth.dependencies
  )

lazy val routing = (project in file("routing"))
  .settings(
    name := "project-routing",
    libraryDependencies ++= Routing.dependencies
  )

lazy val helper = (project in file("helper"))
  .settings(
    name := "project-helper",
    libraryDependencies ++= Helper.dependencies
  )

lazy val photo = (project in file("photo"))
  .settings(
    name := "project-photo",
    libraryDependencies ++= Photo.dependencies
  )
