import cats.effect.{IO, IOApp, Resource}
import com.comcast.ip4s.*
import org.http4s.dsl.io.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.headers.{`Content-Length`, `Content-Type`}
import org.http4s.implicits.*
import org.http4s.server.Server
import org.http4s.{EntityEncoder, Headers, HttpRoutes, MediaType, Response, Status}
import org.mongodb.scala.bson.BsonObjectId
import org.mongodb.scala.gridfs.{GridFSBucket, GridFSFile}
import org.mongodb.scala.model.Filters
import org.mongodb.scala.{MongoClient, MongoDatabase, Observable, ObservableFuture}

import java.nio.ByteBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions

object Testic extends IOApp.Simple {

  val mongoClient: MongoClient = MongoClient("mongodb://localhost:27017")
  val database: MongoDatabase = mongoClient.getDatabase("test")
  val gridFSBucket: GridFSBucket = GridFSBucket(database)

  override def run: IO[Unit] = {

    val fileIO = IO.fromFuture {
      IO.delay {
        val filter = Filters.equal("_id", BsonObjectId("662e7b90c14d934276c9af2b"))
        //val filter = Filters.equal("filename", "photo.jpg")
        gridFSBucket.find(filter).headOption()
      }
    }

    for {
      file <- fileIO
      _ <- IO.println(file)
    } yield ()
  }
}

object PhotoApp extends IOApp.Simple {

  // MongoDB connection
  val mongoClient: MongoClient = MongoClient("mongodb://localhost:27017")
  val database: MongoDatabase = mongoClient.getDatabase("test")
  val gridFSBucket: GridFSBucket = GridFSBucket(database)

  // HTTP routes
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "photo" / s"${id}.jpeg" =>
      for {
        _ <- IO.println(s"-> $id")
        maybePhoto <- IO.fromFuture(
          IO.delay {
            val gridFsFileFuture = gridFSBucket.find(Filters.equal("_id", BsonObjectId(id))).headOption()
            gridFsFileFuture.flatMap {
              case Some(file) => {
                println(s"${file.getId}, ${file.getLength}, ${file.getFilename}")
                println()
                gridFSBucket.downloadToObservable(file.getId).toFuture()
              }
              case None => Future.successful {
                println("File not found")
                Seq()
              }
            }
          }
        )
        response <-  {
          //case Some(photo) => {
            val bytes = maybePhoto.flatMap(_.array()).toArray
            println(bytes.length)
            val headers = Headers(
              `Content-Length`.unsafeFromLong(bytes.length),
              `Content-Type`(MediaType.image.jpeg)
            )
            val responseBody = EntityEncoder[IO, Array[Byte]].toEntity(bytes).body
            IO(Response[IO](Status.Ok).withHeaders(headers).withBodyStream(responseBody))
          //}
          //case None => {
          //  println("NotFound()")
          //  NotFound()
          //}
        }
      } yield response

    case req @ POST -> Root / "photo" => for {
        // Read photo data from request body
        bytes <- req.body.compile.to(Array)
        // Upload photo to GridFS
        file = Observable(Seq(ByteBuffer.wrap(bytes)))
        _ <- IO.println(bytes.length)
        fileId <- IO.fromFuture(IO.delay(gridFSBucket.uploadFromObservable(filename = "photo.jpg", file).headOption()))
        response <- Ok(s"Photo uploaded with id: ${fileId.map(_.toHexString).getOrElse("NOT UPLOADED")}")
      } yield response
  }

  // HTTP Server
  private def server: Resource[IO, Server] = {
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(routes.orNotFound)
      .build
  }

  override def run: IO[Unit] = server.use(_ => IO.never)
}