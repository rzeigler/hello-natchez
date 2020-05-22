package rzeigler

import cats._
import cats.implicits._
import cats.effect._
import doobie._
import doobie.implicits._
import natchez.Trace

trait Handler[F[_]] {
  def hello: F[String]
}

object Handler {
  // We take the trace constraint here and don't see the Kleisli anywhere
  def apply[F[_]: Bracket[*[_], Throwable]: Trace](xa: Transactor[F]): Handler[F] =
    new Handler[F] {
      def hello: F[String] =
        Trace[F].span("hello") {
          sql"SELECT random()"
            .query[Double]
            .unique
            .map(d => s"Hello. Your magic number is $d")
            .transact(xa)
        }

    }
}
