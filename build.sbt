ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.3"


val http4sVersion = "0.23.26"



lazy val root = (project in file("."))
  .settings(
    name := "gridfs-example",
    libraryDependencies ++= Seq(
      "org.mongodb.scala" % "mongo-scala-driver_2.13" % "5.0.1",
      "org.http4s" %% "http4s-ember-client" % http4sVersion,
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "org.http4s" %% "http4s-dsl"          % http4sVersion,
      "org.typelevel" %% "cats-effect" % "3.5.4"
    ),
    scalacOptions ++= Seq(
      "-deprecation",
      "-encoding", "UTF-8",
      "-feature",
      "-unchecked"
    )
  )
