package trymorganey.messages

import org.http4s.circe._
import io.circe._
import io.circe.generic.auto._

object Evaluate {
  implicit val decoder = jsonOf[Evaluate]
}

case class Evaluate(term: String)
