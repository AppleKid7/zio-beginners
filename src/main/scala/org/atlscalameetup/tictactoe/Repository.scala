package org.atlscalameetup.tictactoe

import zio.{Scope, Task, ZIO, ZLayer}

trait Repository:
  def getGame(id: String): Task[Option[GameState]]
  def putGame(id: String, game: GameState): Task[Unit]


def makeInMemoryRepository: Task[Repository] =
  for
    storageRef <- zio.Ref.make(Map.empty[String, GameState])
  yield new Repository:
    override def getGame(id: String): Task[Option[GameState]] =
      for
        storage <- storageRef.get
      yield
        storage.get(id)

    override def putGame(id: String, game: GameState): Task[Unit] =
      storageRef.update: storage =>
        storage + (id -> game)





trait Sqlite {
  def execute(q: String, args: Any*): Unit
  def close(): Unit
}

def makeFileRepository(filename: String): ZLayer[Scope, Throwable, Repository] = {
  def openSqlite: ZIO[Any, Throwable, Sqlite] = ???

  def closeSqlite(sqlite: Sqlite): ZIO[Any, Nothing, Unit] = ZIO.succeedBlocking(sqlite.close())

  ZLayer.scoped(
    for {
      sqliteConnection <- ZIO.acquireRelease(openSqlite)(closeSqlite)
    } yield new Repository:
      override def getGame(id: String): Task[Option[GameState]] =
        for {
          result <- ZIO.attemptBlockingIO(
            sqliteConnection.execute("SELECT * FROM game WHERE id = ?", id)
          )
        } yield {
          ??? // construct game from result
        }

      override def putGame(id: String, game: GameState): Task[Unit] =
        ZIO.attemptBlockingIO(
          sqliteConnection.execute("INSERT OR UPDATE game SET ...", game)
        )
  )
}