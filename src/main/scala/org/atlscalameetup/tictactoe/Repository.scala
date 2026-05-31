package org.atlscalameetup.tictactoe

import zio.{Scope, Task, ZIO, ZLayer}

trait Repository:
  def getGame(id: String): Task[Option[GameState]]
  def putGame(id: String, game: GameState): Task[Unit]

def makeInMemoryRepository: Task[Repository] =
  for storageRef <- zio.Ref.make(Map.empty[String, GameState])
  yield new Repository:
    override def getGame(id: String): Task[Option[GameState]] =
      for storage <- storageRef.get
      yield storage.get(id)

    override def putGame(id: String, game: GameState): Task[Unit] =
      storageRef.update: storage =>
        storage + (id -> game)

trait SqliteConnection {

  /** Blocks while executing the query */
  def execute(q: String, args: Any*): Unit

  /** Closes this SqliteConnection; blocks while file is closed */
  def close(): Unit
}

object SqliteConnection {

  /** blocks while opening the SQLite file */
  def open(filename: String): SqliteConnection = ???
}

def makeFileRepository(filename: String): ZLayer[Scope, Throwable, Repository] = {
  def openSqlite: ZIO[Any, Throwable, SqliteConnection] =
    ZIO.attemptBlocking(SqliteConnection.open(filename))

  def closeSqlite(conn: SqliteConnection): ZIO[Any, Nothing, Unit] =
    ZIO.succeedBlocking(conn.close())

  ZLayer.scoped(
    for {
      conn <- ZIO.acquireRelease(openSqlite)(closeSqlite)
    } yield new Repository:
      override def getGame(id: String): Task[Option[GameState]] =
        for {
          result <- ZIO.attemptBlockingIO(
            conn.execute("SELECT * FROM game WHERE id = ?", id)
          )
        } yield {
          ??? // construct game from result
        }

      override def putGame(id: String, game: GameState): Task[Unit] =
        ZIO.attemptBlockingIO(
          conn.execute("INSERT OR UPDATE game SET ...", game)
        )
  )
}
