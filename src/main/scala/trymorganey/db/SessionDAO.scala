package trymorganey.db

import doobie.imports._

import scalaz.concurrent.Task
import trymorganey.Session
import trymorganey.db.{SessionSQLStatements => DB}

import scala.concurrent.duration.Duration

object SessionDAO {

  def session(sid: Long)(implicit db: Transactor[Task]): Task[Option[Session]] =
    DB.session(sid).transact(db)

  def insert(session: Session)(implicit db: Transactor[Task]): Task[Unit] =
    DB.insert(session).run.transact(db).map(_ => ())

  def update(session: Session)(implicit db: Transactor[Task]): Task[Unit] =
    DB.update(session).run.transact(db).map(_ => ())

  def delete(sid: Long)(implicit db: Transactor[Task]): Task[Unit] =
    DB.delete(sid).run.transact(db).map(_ => ())

  def cleanUp(age: Duration)(implicit db: Transactor[Task]): Task[Unit] =
    DB.cleanUp(age).run.transact(db).map(_ => ())

  def createTable(implicit db: Transactor[Task]): Task[Unit] =
    DB.createTable().run.transact(db).map(_ => ())

}
