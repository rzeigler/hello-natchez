package rzeigler

import cats._
import cats.data._
import cats.effect._
import cats.implicits._
import fs2.Stream
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import scala.concurrent.ExecutionContext.global
import org.http4s.HttpApp
import doobie.util._
import cats.effect.Blocker
import doobie.util.transactor.Transactor
import natchez.Span

import natchez.log._
import _root_.io.chrisdavenport.log4cats.slf4j.Slf4jLogger

object Server {

  // Shorter version for typing out signatures
  type TraceF[F[_], A] = Kleisli[F, Span[F], A]

  def stream[F[_]: ConcurrentEffect](
      implicit T: Timer[F],
      C: ContextShift[F]
  ): Stream[F, Nothing] = {
    // An entrypoint
    val entry = {
      import cats.implicits._
      // Logger a common effect so make implicit
      implicit val logger = Slf4jLogger.getLoggerFromName("rzeigler.Server")
      Log.entryPoint[F]("hello-natchez")
    }

    // First we make the transactor (pretending we are something like hikari)
    val xa: Resource[F, Transactor[F]] = Resource.pure[F, Transactor[F]](
      Transactor.fromDriverManager[F](
        "org.postgresql.Driver",
        "jdbc:postgresql://localhost/postgres",
        "postgres",
        "super-secret-password",
        Blocker.liftExecutionContext(
          ExecutionContexts.synchronous
        )
      )
    )

    // Then we lift the
    val xaInKleisli: Resource[F, Transactor[TraceF[F, *]]] =
      xa.map(_.mapK(Kleisli.liftK[F, Span[F]]))

    for {
      // Lift the resource into the stream
      xa <- Stream.resource(xaInKleisli)

      // Use our transactor in TraceF to make a Handler in TraceF
      handler: Handler[TraceF[F, *]] = Handler(xa)

      // Routes is different. It has knowledge of the span entrypoint via ContextRequest
      // So we have a Routes[F] that expects a Handler in TraceF
      httpApp: Routes[F] = new Routes(handler)

      // With Middlewares in place we are now back to a normal HttpApp[F]
      finalHttpApp: HttpApp[F] = new TraceMiddleware(entry).middleware(httpApp.routes.orNotFound)

      exitCode <- BlazeServerBuilder[F]
        .bindHttp(4040, "0.0.0.0")
        .withHttpApp(finalHttpApp)
        .serve
    } yield exitCode
  }.drain
}
