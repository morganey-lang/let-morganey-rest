package trymorganey

import java.io.File
import java.security.SecureRandom
import java.time.Instant

import doobie.util.transactor.DriverManagerTransactor
import me.rexim.morganey.{Commands, ReplAutocompletion}
import me.rexim.morganey.ast._
import me.rexim.morganey.interpreter.{MorganeyRepl, ReplContext, ReplResult}
import me.rexim.morganey.module.ModuleFinder
import me.rexim.morganey.reduction.Computation
import org.http4s.MediaType._
import org.http4s._
import org.http4s.dsl._
import org.http4s.headers.`Set-Cookie`
import org.http4s.server._
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.twirl._
import trymorganey.db.SessionDAO._
import trymorganey.messages.{Autocomplete, Evaluate, MgnResponse}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import scalaz._
import scalaz.concurrent.Task

object TryMorganey extends ServerApp {
  private val assetDirectory    = new File("public")
  private val rnd               = new SecureRandom()
  private val sessionTimeout    = 15.minutes
  private val evaluationTimeout = 5.seconds
  private val serverModules     = new ModuleFinder(List(new File("std")))
  private val prelude           = Await.result(MorganeyRepl.evalNode(ReplContext(List(), serverModules),
                                    MorganeyLoading(Some("prelude"))).future, evaluationTimeout).context
  private val preludeSession    = Session(0L, Array.empty[Byte], 0L).update(prelude)

  private implicit val db = DriverManagerTransactor[Task](
    "org.h2.Driver", "jdbc:h2:mem:sessions;DB_CLOSE_DELAY=-1"
  )

  private def Fail[T](msg: String): Task[T] =
    Task.fail(sys.error(msg))

  private val nextSid: Task[Long] =
    for {
      sid    <- Task.delay(rnd.synchronized(rnd.nextLong()))
      exists <- session(sid).map(_.isDefined)
      result <- if (exists) nextSid else Task.now(sid)
    } yield result

  private def cookies(request: Request): List[Cookie] =
    request.headers.flatMap {
      case org.http4s.headers.Cookie(cookies) => cookies.values.list
      case _                                  => Nil
    }.toList

  private def cookieNamedSid(request: Request): Task[Option[Cookie]] =
    Task.now(cookies(request).find(_.name == "sid"))

  private def cookieHasSid(cookie: Cookie): Task[Option[Session]] =
    cookie.content match {
      case LongVar(sid) =>
        session(sid).attempt flatMap {
          case \/-(x) => Task.now(x)
          case -\/(_) => Fail("Unknown sid!")
        }
      case _ => Fail("Invalid sid!")
    }

  private val newSession: Task[Session] =
    for {
      sid     <- nextSid
      session =  preludeSession.copy(sid = sid, created = System.currentTimeMillis())
      _       <- insert(session)
    } yield session

  private def sidOf(request: Request): Task[Option[Session]] =
    cookieNamedSid(request) flatMap {
      _.fold(newSession map (Option(_)))(cookieHasSid)
    }

  private def replContext(session: Session): Task[ReplContext] =
    session.context(serverModules).fold(Fail[ReplContext]("Could not decode session state!"))(Task.now)

  private def taskFromComputation[T](computation: Computation[T]): Task[T] =
    Task.async { register =>
      computation.future.onComplete {
        case Success(x) => register(\/-(x))
        case Failure(e) => register(-\/(e))
      } (scala.concurrent.ExecutionContext.global)
    }

  private def evaluate(req: Request, session: Session, elem: Evaluate): Task[MgnResponse] = {
    def replResultToResponse(context: ReplContext, res: ReplResult[String]): Task[MgnResponse] = {
      val ReplResult(newContext, msg) = res
      val result = Task.now(MgnResponse.success(msg.toList: _*))
      val updater =
        if (context eq newContext) Task.now(())
        else update(Session(session.sid, Array.empty[Byte], 0L).update(newContext))
      updater.flatMap(_ => result)
    }

    def evalHelper(context: ReplContext) = {
      val computation = MorganeyRepl.evalLine(context, elem.term)
      for {
        evalResult <- taskFromComputation(computation).timed(evaluationTimeout).attempt
        response   <- evalResult.fold(t => Task.now(MgnResponse.error(t.getMessage)),
                        replResultToResponse(context, _))
      } yield response
    }

    for {
      context  <- replContext(session)
      response <- Commands.parseCommand(elem.term.trim) collect {
                    case (cmd, _) if !isCommandAllowed(cmd) =>
                      Task.now(MgnResponse.error(s"Command `$cmd` is not available in the online REPL!"))
                  } getOrElse evalHelper(context)
    } yield response
  }

  private def isCommandAllowed(command: String): Boolean =
    command != "exit"

  private def autocomplete(req: Request, session: Session, elem: Autocomplete): Task[MgnResponse] =
    for {
      context    <- replContext(session)
      candidates <- Task.delay(ReplAutocompletion.complete(elem.line, context)).timed(evaluationTimeout).attempt
      response   <- candidates.fold(t => Task.now(MgnResponse.error(t.getMessage)),
                      xs => Task.now(MgnResponse.success(xs: _*)))
    } yield response

  private def withSession[T: EntityDecoder](f: (Request, Session, T) => Task[MgnResponse], req: Request): Task[Response] =
    sidOf(req).attempt flatMap {
      case \/-(None)          => BadRequest("No session!")
      case \/-(Some(session)) =>
        req.decode[T] { ev =>
          f(req, session, ev).flatMap(Ok(_).withType(`application/json`))
        }
      case -\/(e)             => BadRequest(e.getMessage)
    }

  private def sidCookie(session: Session) =
    Cookie(
      name    = "sid",
      content = session.sid.toString,
      expires = Some(Instant.now().plusMillis(sessionTimeout.toMillis))
    )

  val service = HttpService {

    case req @ GET -> Root =>
      sidOf(req).attempt flatMap {
        case \/-(None)    => Ok(html.index())
        case \/-(Some(x)) => Ok(html.index()).putHeaders(`Set-Cookie`(sidCookie(x)))
        case -\/(e)       => BadRequest(e.getMessage)
      }

    case req @ GET -> "assets" /: path =>
      val file = new File(assetDirectory, path.toString)
      StaticFile.fromFile(file, Some(req))
        .fold(NotFound())(Task.now)

    case req @ POST -> Root / "evaluate" =>
      withSession(evaluate, req)

    case req @ POST -> Root / "autocomplete" =>
      withSession(autocomplete, req)

  }

  override def server(args: List[String]): Task[Server] = {
    BlazeBuilder
      .bindLocal(8080)
      .mountService(service, "/")
      .start
  }

}
