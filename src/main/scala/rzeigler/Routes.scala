package rzeigler

import cats._
import cats.data._
import cats.effect.Sync
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

import natchez._
import org.http4s.ContextRoutes
import org.http4s.Response
import org.http4s.Status

final class Routes[F[_]: Applicative: Defer](
    handler: Handler[Kleisli[F, Span[F], *]]
) extends Http4sDsl[F] {
  val routes: ContextRoutes[Span[F], F] = ContextRoutes.of[Span[F], F] {
    // Here we destructure the ContextRoute to get the span
    // Then we can immediately discharge the Kleisli of the handler and get our response
    case GET -> Root as span =>
      handler.hello.run(span).map(Response[F](Status.Ok).withEntity(_))
  }
}
