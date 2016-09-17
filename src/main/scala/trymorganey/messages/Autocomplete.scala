package trymorganey.messages

import org.http4s.circe._
import io.circe._
import io.circe.generic.auto._

object Autocomplete {
  implicit val decoder = jsonOf[Autocomplete]
}

case class Autocomplete(line: String)
