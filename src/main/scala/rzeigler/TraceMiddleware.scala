package rzeigler

import cats.implicits._
import cats.effect._
import cats.data._
import natchez.EntryPoint
import org.http4s._
import org.http4s.server._
import natchez._

final class TraceMiddleware[F[_]: Bracket[*[_], Throwable]](
    entry: EntryPoint[F]
) {

  type Ware =
    Middleware[F, ContextRequest[F, Span[F]], Response[F], Request[F], Response[F]]

  val middleware: Ware =
    (handler) =>
      Kleisli(req => {
        // Here we do a dance to extract the kernel if it exists
        val kernel = Kernel(req.headers.toList.map(h => (h.name.value -> h.value)).toMap)
        // Path makes an acceptable name
        val name = req.uri.path
        entry
          .continueOrElseRoot(name, kernel)
          .use(span => handler(ContextRequest(span, req)))
      })
}
