package trymorganey.db

import doobie.imports._
import trymorganey.Session

import scala.concurrent.duration.Duration

object SessionSQLStatements {

  def session(sid: Long): ConnectionIO[Option[Session]] =
    sql"""
      SELECT *
      FROM sessions
      WHERE sid = $sid
      LIMIT 1
    """.query[Session].option

  def insert(session: Session): Update0 =
    sql"""
      INSERT INTO sessions (sid, content, created)
      VALUES (${session.sid}, ${session.content}, ${session.created})
    """.update

  def update(session: Session): Update0 =
    sql"""
      UPDATE sessions
      SET content = ${session.content}, created = ${session.created}
      WHERE sid = ${session.sid}
    """.update

  def delete(sid: Long): Update0 =
    sql"""
      DELETE FROM session
      WHERE sid = $sid
    """.update

  def cleanUp(age: Duration): Update0 =
    sql"""
      DELETE FROM session
      WHERE created <= ${System.currentTimeMillis() - age.toMillis}
    """.update

  def createTable(): Update0 =
    sql"""
      CREATE TABLE IF NOT EXISTS sessions (
        sid     LONG       PRIMARY KEY,
        content MEDIUMBLOB NOT NULL,
        created LONG       DEFAULT CURRENT_TIMESTAMP
      )
    """.update

}
