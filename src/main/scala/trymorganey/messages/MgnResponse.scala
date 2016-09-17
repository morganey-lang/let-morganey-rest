package trymorganey.messages

import org.http4s.circe._
import io.circe._
import io.circe.generic.auto._

object MgnResponse {
  implicit val encoder = jsonEncoderOf[MgnResponse]

  def error(elems: String*): MgnResponse =
    MgnResponse(true, elems.toList)

  def success(elems: String*): MgnResponse =
    MgnResponse(false, elems.toList)
}

case class MgnResponse(error: Boolean, messages: List[String])
